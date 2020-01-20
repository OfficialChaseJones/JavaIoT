import com.couchbase.client.java.*;
import com.couchbase.client.java.document.JsonDocument;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.PartitionReceiver;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.charset.Charset;
import java.net.URI;
import java.net.URISyntaxException;

public class BackendEndPoint {
    /*
        Code was taken from Microsoft examples and modified.

     */
    // az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}
    private static final String eventHubsCompatibleEndpoint = "sb://ihsuprodbyres073dednamespace.servicebus.windows.net/";

    // az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}
    private static final String eventHubsCompatiblePath = "iothub-ehub-javaiot-2797929-c3ae5eb46e";

    // az iot hub policy show --name service --query primaryKey --hub-name {your IoT Hub name}
    private static final String iotHubSasKey = "[REMOVED]";
    private static final String iotHubSasKeyName = "service";

    // Track all the PartitionReciever instances created.
    private static ArrayList<PartitionReceiver> receivers = new ArrayList<PartitionReceiver>();

    static Bucket couchbaseBucket;

    // Asynchronously create a PartitionReceiver for a partition and then start
    // reading any messages sent from the simulated client.
    private static void receiveMessages(EventHubClient ehClient, String partitionId)
            throws EventHubException, ExecutionException, InterruptedException {

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Create the receiver using the default consumer group.
        // For the purposes of this sample, read only messages sent since
        // the time the receiver is created. Typically, you don't want to skip any messages.
        ehClient.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
                EventPosition.fromEnqueuedTime(Instant.now())).thenAcceptAsync(receiver -> {
            System.out.println(String.format("Starting receive loop on partition: %s", partitionId));
            System.out.println(String.format("Reading messages sent since: %s", Instant.now().toString()));

            receivers.add(receiver);

            while (true) {
                try {
                    // Check for EventData - this methods times out if there is nothing to retrieve.
                    Iterable<EventData> receivedEvents = receiver.receiveSync(100);

                    // If there is data in the batch, process it.
                    if (receivedEvents != null) {
                        for (EventData receivedEvent : receivedEvents) {
                            String receivedMessage = new String(receivedEvent.getBytes(), Charset.defaultCharset());

                            System.out.println(String.format("Message received:\n %s",
                                    receivedMessage));

                            JsonDocument doc = JsonDocument.create(receivedMessage);
                            couchbaseBucket.upsert(doc);
                        }
                    }
                } catch (EventHubException e) {
                    System.out.println("Error reading EventData");
                }
            }
        }, executorService);
    }

    public static void main(String[] args)
            throws EventHubException, ExecutionException, InterruptedException, IOException, URISyntaxException {

        //Connect to couchbase:
        Cluster cluster = CouchbaseCluster.create();
        //Bucket bucket = cluster.openBucket();
        cluster.authenticate("user","password");
        couchbaseBucket = cluster.openBucket("Random-Numbers");

        //Connect to Azure IoT
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setEndpoint(new URI(eventHubsCompatibleEndpoint))
                .setEventHubName(eventHubsCompatiblePath)
                .setSasKeyName(iotHubSasKeyName)
                .setSasKey(iotHubSasKey);

        // Create an EventHubClient instance to connect to the
        // IoT Hub Event Hubs-compatible endpoint.
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final EventHubClient ehClient = EventHubClient.createSync(connStr.toString(), executorService);

        // Use the EventHubRunTimeInformation to find out how many partitions
        // there are on the hub.
        final EventHubRuntimeInformation eventHubInfo = ehClient.getRuntimeInformation().get();

        // Create a PartitionReciever for each partition on the hub.
        for (String partitionId : eventHubInfo.getPartitionIds()) {
            receiveMessages(ehClient, partitionId);
        }

        // Shut down cleanly.
        System.out.println("Press ENTER to exit.");
        System.in.read();
        System.out.println("Shutting down...");
        for (PartitionReceiver receiver : receivers) {
            receiver.closeSync();
        }
        cluster.disconnect();
        ehClient.closeSync();
        executorService.shutdown();
        System.exit(0);
    }
}

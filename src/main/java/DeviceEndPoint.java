import com.microsoft.azure.sdk.iot.device.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.google.gson.*;

public class DeviceEndPoint {
    /*
        Code was taken from Microsoft examples and modified.

     */

    // The device connection string to authenticate the device with your IoT hub.
    // Using the Azure CLI:
    // az iot hub device-identity show-connection-string --hub-name {YourIoTHubName} --device-id MyJavaDevice --output table
    private static String connString = "HostName=JavaIoT.azure-devices.net;DeviceId=ExampleDevice;SharedAccessKey=[REMOVED]";

    // Using the MQTT protocol to connect to IoT Hub
    private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
    private static DeviceClient client;


    // Print the acknowledgement received from IoT Hub for the telemetry message sent.
    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }

    private static class MessageSender implements Runnable {
        public void run() {
            try {
                // Initialize the simulated telemetry.
                Random rand = new Random();


                while (true) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();

                    JsonObject randomJson = new JsonObject();
                    randomJson.addProperty("RandomInteger", rand.nextInt(100));
                    randomJson.addProperty("version",System.getProperty("java.version"));
                    randomJson.addProperty("datetime",dateFormat.format(date));

                    String jsonString = randomJson.toString();
                    Message msg = new Message( jsonString);

                    System.out.println("Sending message: "+ jsonString);

                    Object lockobj = new Object();

                    // Send the message.
                    EventCallback callback = new EventCallback();
                    client.sendEventAsync(msg, callback, lockobj);

                    synchronized (lockobj) {
                        lockobj.wait();
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Finished.");
            }
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        // Connect to the IoT hub.
        client = new DeviceClient(connString, protocol);
        client.open();

        // Create new thread and start sending messages
        MessageSender sender = new MessageSender();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(sender);

        // Stop the application.
        System.out.println("Press ENTER dto exit.");
        System.in.read();
        executor.shutdownNow();
        client.closeNow();
    }
}

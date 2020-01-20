# JavaIoT
This is a weekend exploration of the Azure IoT Hub product.  Some code was taken from examples online and modified.

An Azure IoT Hub was created and Java endpoints were coded. 

One program "RunClient.bat" simulates a device writing json information to the IoT Hub.
The other program "RunBackend.bat" is an endpoint that reads those messages and writes them to Couchbase.


Steps taken:
1. Created IoT Hub in Azure named 'JavaIoT'
2. Create a device entry with powershell cmdlet (CreateDevice.ps1)
3. installed couchbase and set up a bucket called 'random-numbers'
4. Create java apps using IntelliJ/Maven.

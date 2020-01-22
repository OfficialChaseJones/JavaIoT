<#
   This script registers a new device in Azure IoT hub and then starts a process that submits communications on that device.

   Prerequesites:

        Install-Module -Name AzureIoT
        Install-Module -Name "AzureRM.IotHub"
        #Import-Module -Name AzureIoT

        Only need to connect once:
        Connect-AzureRMAccount
#>

#Install-Module  -Name "AzureRM.IotHub"

$IoTHubName = "JavaIoT"
$RandomNumber = Get-Random
$DeviceId = "ExampleDevice$RandomNumber"


$Hub = Get-AzureRmIotHub -Name $IoTHubName -ResourceGroupName "RG1"

$IoTHubKey = Get-AzureRmIotHubKey -ResourceGroupName "RG1" -Name $IoTHubName -KeyName "iothubowner"

$IoTHubConnectionString = "HostName="+$IoTHubName+".azure-devices.net;SharedAccessKeyName="+$IoTHubKey.KeyName+";SharedAccessKey="+$IoTHubKey.PrimaryKey

$NewDevice = Register-IoTDevice -iotConnString $IoTHubConnectionString -deviceId $DeviceId

Set-Location -Path "D:\PowerShellTools\JavaIoT\target"

$ConnectionString = "HostName=JavaIoT.azure-devices.net;DeviceId=$DeviceId;SharedAccessKey=$($NewDevice.DevicePrimaryKey)"

java -cp "D:\PowerShellTools\JavaIoT\target\lib\*;JavaIoT-1.0-SNAPSHOT.jar" DeviceEndPoint $ConnectionString >> "D:\out$DeviceId.txt"
<#
   Prerequesites:

        Install-Module -Name AzureIoT
        Install-Module -Name "AzureRM.IotHub"
        #Import-Module -Name AzureIoT

        Only need to connect once:
        Connect-AzureRMAccount
#>

#Install-Module  -Name "AzureRM.IotHub"

$IoTHubName = "JavaIoT"
$DeviceId = "ExampleDevice"


$Hub = Get-AzureRmIotHub -Name $IoTHubName -ResourceGroupName "RG1"

$IoTHubKey = Get-AzureRmIotHubKey -ResourceGroupName "RG1" -Name $IoTHubName -KeyName "iothubowner"

$IoTHubConnectionString = "HostName="+$IoTHubName+".azure-devices.net;SharedAccessKeyName="+$IoTHubKey.KeyName+";SharedAccessKey="+$IoTHubKey.PrimaryKey

Register-IoTDevice -iotConnString $IoTHubConnectionString -deviceId "ExampleDevice"
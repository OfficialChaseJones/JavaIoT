<#

    This script runs 20 instances of "CreateDeviceAndStartDeviceSimulator.ps1".

    More than that and it will chew up too much CPU.
    

#>

  # Define what each job does
  $ScriptBlock = {
    . "D:\PowerShellTools\JavaIoT\CreateDeviceAndStartDeviceSimulator.ps1" 
  }

  # Execute the jobs in parallel
 for ($i=0; $i -le 10; $i++)
 {
    Start-Job $ScriptBlock 
 }


  Get-Job

  #Start-Sleep 60

    Get-Job
# Wait for it all to complete

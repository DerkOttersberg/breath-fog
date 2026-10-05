param([int]$Seconds=20)
$ErrorActionPreference='Stop'
$qaRoot=Join-Path $PSScriptRoot '../fabric/run-qa/qa'
$batch='paired-'+(Get-Date -Format HHmmss)
& (Join-Path $PSScriptRoot qa-control.ps1) -Json '{"shader":"off","actors":7,"motion":0,"camera":"FIRST_PERSON","enabled":true}'
Start-Sleep -Seconds 12
foreach ($index in 0..3) {
    $label=$batch+'-'+$index
    $enabled=($index%2 -eq 1)
    & (Join-Path $PSScriptRoot qa-control.ps1) -Json (@{enabled=$enabled;measure=$label;seconds=$Seconds}|ConvertTo-Json)
    $deadline=(Get-Date).AddSeconds($Seconds+30)
    $result=Join-Path $qaRoot ($label+'.json')
    while (!(Test-Path -LiteralPath $result)) {
        if ((Get-Date) -gt $deadline) { throw 'QA client did not finish the frame-time sample.' }
        Start-Sleep -Milliseconds 500
    }
    Get-Content -LiteralPath $result
}
& (Join-Path $PSScriptRoot qa-control.ps1) -Json '{"enabled":true}'

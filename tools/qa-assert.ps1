param(
    [int]$Seconds=6,
    [switch]$ExpectZero,
    [switch]$ExpectBreath,
    [int]$ExpectedEmitters=-1
)
$ErrorActionPreference='Stop'
$statusPath=Join-Path $PSScriptRoot '../fabric/run-qa/qa/status.json'
$samples=[Collections.Generic.List[object]]::new()
$deadline=(Get-Date).AddSeconds($Seconds)
while ((Get-Date) -lt $deadline) {
    try { $samples.Add((Get-Content -LiteralPath $statusPath -Raw | ConvertFrom-Json)) } catch { }
    Start-Sleep -Milliseconds 200
}
if ($samples.Count -lt 2 -or $samples[-1].ticks -le $samples[0].ticks) { throw 'QA status is stale; no active client was sampled.' }
$particles=($samples | Measure-Object -Property particles -Maximum).Maximum
$emitters=($samples | Measure-Object -Property emitters -Maximum).Maximum
if (@($samples | Where-Object {$_.error}).Count) { throw 'The QA helper reported an error.' }
if ($emitters -gt 24 -or $particles -gt 256) { throw "Budget exceeded: $emitters emitters, $particles particles." }
if ($ExpectZero -and $particles -ne 0) { throw 'Expected all breath particles to be removed or expired.' }
if ($ExpectBreath -and $particles -le 0) { throw 'Expected visible emission in the sampling interval.' }
if ($ExpectedEmitters -ge 0 -and @($samples | Where-Object {$_.emitters -ne $ExpectedEmitters}).Count) { throw "Expected $ExpectedEmitters emitters throughout the sample." }
@{seconds=$Seconds;maximumParticles=$particles;maximumEmitters=$emitters;last=$samples[-1]} | ConvertTo-Json -Depth 8

param([Parameter(Mandatory=$true)][string]$Json)
$ErrorActionPreference='Stop'
$qaRoot=Join-Path $PSScriptRoot '../fabric/run-qa/qa'
if (!(Test-Path -LiteralPath $qaRoot)) { throw 'Start the opt-in QA client first.' }
$request=$Json | ConvertFrom-Json -AsHashtable
if (!$request.ContainsKey('id')) { $request.id=[guid]::NewGuid().ToString() }
$temporary=Join-Path $qaRoot 'control.tmp'
$destination=Join-Path $qaRoot 'control.json'
[IO.File]::WriteAllText($temporary,($request | ConvertTo-Json -Depth 12))
[IO.File]::Move($temporary,$destination,$true)
$request.id

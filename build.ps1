param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GradleArguments = @('clean','check','build'))
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$jdkFolder = Join-Path $projectRoot '.toolchains'
$jdk = Get-ChildItem -LiteralPath $jdkFolder -Directory -ErrorAction SilentlyContinue | Where-Object {Test-Path -LiteralPath (Join-Path $_.FullName 'bin\javac.exe')} | Select-Object -First 1
if (!$jdk) {
    Write-Host 'Preparing a project-local Java 25 toolchain. System Java settings stay unchanged.'
    New-Item -ItemType Directory -Force -Path $jdkFolder | Out-Null
    $assets = Invoke-RestMethod -Uri 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows'
    $package = $assets[0].binary.package
    $archive = Join-Path $jdkFolder 'temurin25.zip'
    Invoke-WebRequest -Uri $package.link -OutFile $archive
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $package.checksum) { throw 'Java download checksum mismatch' }
    Expand-Archive -LiteralPath $archive -DestinationPath $jdkFolder -Force
    $jdk = Get-ChildItem -LiteralPath $jdkFolder -Directory | Where-Object {Test-Path -LiteralPath (Join-Path $_.FullName 'bin\javac.exe')} | Select-Object -First 1
}
$previousJava = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $jdk.FullName
    Push-Location $projectRoot
    try { & (Join-Path $projectRoot 'gradlew.bat') @GradleArguments; if ($LASTEXITCODE -ne 0) { throw "Build failed (exit $LASTEXITCODE)" } }
    finally { Pop-Location }
} finally { $env:JAVA_HOME = $previousJava }

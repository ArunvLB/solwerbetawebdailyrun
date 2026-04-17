$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Tools = Join-Path $Root "tools"
$Downloads = Join-Path $Tools "downloads"
$LocalRepo = Join-Path $Root ".m2\\repository"

New-Item -ItemType Directory -Force -Path $Downloads | Out-Null
New-Item -ItemType Directory -Force -Path $LocalRepo | Out-Null

# Portable installs inside repo to avoid system-wide setup
$MavenVersion = "3.9.8"
$MavenZip = Join-Path $Downloads "apache-maven-$MavenVersion-bin.zip"
$MavenDir = Join-Path $Tools "apache-maven-$MavenVersion"
$MavenUrls = @(
  "https://downloads.apache.org/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip",
  "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
)

$JdkZip = Join-Path $Downloads "temurin21-jdk.zip"
$BundledJdkDir = Join-Path $Tools "jdk-21"
$InstalledJdkDir = "C:\Program Files\Java\jdk-21"
$JdkDir = if (Test-Path $InstalledJdkDir) { $InstalledJdkDir } else { $BundledJdkDir }
# Adoptium "latest 21 GA" binary endpoint (windows x64, hotspot, normal)
$JdkUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"

function Download-IfMissing([string]$Url, [string]$Path) {
  if (Test-Path $Path) { return }
  Write-Host "Downloading: $Url"
  Invoke-WebRequest -Uri $Url -OutFile $Path -UseBasicParsing
}

function Download-FromMirrors([string[]]$Urls, [string]$Path) {
  if (Test-Path $Path) { return }
  $errors = @()
  foreach ($u in $Urls) {
    try {
      Download-IfMissing -Url $u -Path $Path
      if (Test-Path $Path) { return }
    } catch {
      $errors += $_.Exception.Message
    }
  }
  throw ("Failed to download. Tried:`n- " + ($Urls -join "`n- ") + "`nErrors:`n- " + ($errors -join "`n- "))
}

function Expand-IfMissing([string]$ZipPath, [string]$TargetDir) {
  if (Test-Path $TargetDir) { return }
  Write-Host "Extracting: $ZipPath"
  Expand-Archive -Path $ZipPath -DestinationPath (Split-Path -Parent $TargetDir) -Force
}

Download-FromMirrors -Urls $MavenUrls -Path $MavenZip
Expand-IfMissing -ZipPath $MavenZip -TargetDir $MavenDir

if (-not (Test-Path $InstalledJdkDir)) {
  Download-IfMissing -Url $JdkUrl -Path $JdkZip
}

if (($JdkDir -eq $BundledJdkDir) -and -not (Test-Path $BundledJdkDir)) {
  Write-Host "Extracting: $JdkZip"
  $tmp = Join-Path $Tools "jdk-tmp"
  if (Test-Path $tmp) { Remove-Item -Recurse -Force $tmp }
  New-Item -ItemType Directory -Force -Path $tmp | Out-Null
  Expand-Archive -Path $JdkZip -DestinationPath $tmp -Force
  $extracted = Get-ChildItem -Directory $tmp | Select-Object -First 1
  if ($null -eq $extracted) { throw "JDK zip extracted but no directory found." }
  Move-Item -Path $extracted.FullName -Destination $BundledJdkDir
  Remove-Item -Recurse -Force $tmp
}

$env:JAVA_HOME = $JdkDir
$env:PATH = (Join-Path $env:JAVA_HOME "bin") + ";" + (Join-Path $MavenDir "bin") + ";" + $env:PATH
$env:MAVEN_OPTS = "-Dmaven.repo.local=$LocalRepo"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
& (Join-Path $MavenDir "bin\\mvn.cmd") -version

Write-Host "Bootstrap complete. Run: scripts\\run-tests.ps1"

param(
  [string]$Env = "QA",
  [string]$Browser = "chrome",
  [object]$Headless = $false
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

$LocalRepo = Join-Path $Root ".m2\\repository"
New-Item -ItemType Directory -Force -Path $LocalRepo | Out-Null

if ([string]::IsNullOrWhiteSpace($env:MAVEN_OPTS)) {
  $env:MAVEN_OPTS = "-Dmaven.repo.local=$LocalRepo"
} elseif ($env:MAVEN_OPTS -notmatch "maven\\.repo\\.local") {
  $env:MAVEN_OPTS = "-Dmaven.repo.local=$LocalRepo " + $env:MAVEN_OPTS
}

function To-Bool([object]$value) {
  if ($value -is [bool]) { return $value }
  if ($null -eq $value) { return $false }
  $s = $value.ToString().Trim().ToLowerInvariant()
  if ($s -in @("1","true","yes","y","on")) { return $true }
  if ($s -in @("0","false","no","n","off")) { return $false }
  throw "Invalid -Headless value '$value'. Use true/false."
}

$HeadlessBool = To-Bool $Headless

$MavenCmd = Join-Path $Root "tools\\apache-maven-3.9.8\\bin\\mvn.cmd"

# Ensure Java + Maven are available for this process.
if (-not (Test-Path $MavenCmd)) {
  & (Join-Path $PSScriptRoot "bootstrap-windows.ps1")
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\\java.exe"))) {
  & (Join-Path $PSScriptRoot "bootstrap-windows.ps1")
}

Push-Location $Root
try {
  & $MavenCmd clean test -Denv=$Env -Dbrowser=$Browser -Dheadless=$HeadlessBool
} finally {
  Pop-Location
}

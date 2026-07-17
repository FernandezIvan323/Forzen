#Requires -Version 5.1
<#
.SYNOPSIS
  Build a Windows installer for Forzen (end users double-click; no terminal).

.DESCRIPTION
  For developers only. Packages a clean app image with jpackage.
  Requires JDK 21+ with jpackage and JavaFX (e.g. Liberica Full / jdk+fx).

.EXAMPLE
  .\scripts\build-installer.ps1
  .\scripts\build-installer.ps1 -Type exe
  .\scripts\build-installer.ps1 -Type msi -Version 1.1.0
#>
param(
    [ValidateSet("exe", "msi", "both")]
    [string]$Type = "both",
    [string]$Version = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not $Version) {
    $pom = Get-Content (Join-Path $Root "pom.xml") -Raw
    if ($pom -match "<version>([^<]+)</version>") {
        $Version = $Matches[1]
    } else {
        $Version = "1.1.0"
    }
}

Write-Host "==> Forzen installer build v$Version ($Type)" -ForegroundColor Green

# 1) Package jar
Write-Host "==> mvn package"
& mvn -q package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Maven package failed" }

$jarName = "forzen-$Version.jar"
$jarPath = Join-Path $Root "target\$jarName"
if (-not (Test-Path $jarPath)) {
    throw "Expected jar not found: $jarPath"
}

# 2) Clean staging dir (only the app jar)
$stage = Join-Path $Root "target\jpackage-input"
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-Item $jarPath (Join-Path $stage $jarName)

$icon = Join-Path $Root "src\main\resources\icons\forzen.ico"
$iconArgs = @()
if (Test-Path $icon) {
    $iconArgs = @("--icon", $icon)
} else {
    Write-Warning "Icon not found at $icon — building without custom icon"
}

$out = Join-Path $Root "target\installer"
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

$jpackage = Get-Command jpackage -ErrorAction SilentlyContinue
if (-not $jpackage) {
    throw "jpackage not found. Install a full JDK 21+ (Liberica Full / jdk+fx recommended)."
}

function Invoke-ForzenJpackage([string]$pkgType) {
    $args = @(
        "--type", $pkgType,
        "--input", $stage,
        "--main-jar", $jarName,
        "--main-class", "com.forzen.App",
        "--name", "Forzen",
        "--app-version", $Version,
        "--vendor", "Forzen Project",
        "--description", "Lupa de pantalla para baja vision",
        "--copyright", "MIT Forzen Project",
        "--dest", $out,
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut",
        "--win-per-user-install",
        "--java-options", "--add-modules=javafx.controls,javafx.swing"
    ) + $iconArgs

    Write-Host "==> jpackage --type $pkgType"
    & jpackage @args
    if ($LASTEXITCODE -ne 0) { throw "jpackage $pkgType failed" }
}

$types = if ($Type -eq "both") { @("exe", "msi") } else { @($Type) }
foreach ($t in $types) {
    Invoke-ForzenJpackage $t
}

# Rename to friendly Setup names
Get-ChildItem $out -File | ForEach-Object {
    $ext = $_.Extension
    $friendly = "Forzen-Setup-$Version$ext"
    $dest = Join-Path $out $friendly
    if ($_.Name -ne $friendly) {
        if (Test-Path $dest) { Remove-Item $dest -Force }
        Rename-Item $_.FullName $friendly
        Write-Host "    -> $friendly"
    }
}

Write-Host ""
Write-Host "Done. Installers in: $out" -ForegroundColor Green
Get-ChildItem $out | Format-Table Name, Length, LastWriteTime
Write-Host "End users: double-click Forzen-Setup-*.exe — no commands needed."

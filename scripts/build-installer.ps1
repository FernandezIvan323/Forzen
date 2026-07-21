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

$jarPath = Join-Path $Root "target\forzen-$Version.jar"
if (-not (Test-Path $jarPath)) {
    $found = Get-ChildItem (Join-Path $Root "target") -Filter "forzen-*.jar" |
        Where-Object { $_.Name -notlike "original-*" } |
        Sort-Object Length -Descending |
        Select-Object -First 1
    if (-not $found) { throw "No forzen-*.jar found in target/" }
    $jarPath = $found.FullName
    Write-Host "Using jar: $($found.Name) (pom version may differ from -Version $Version)"
}
$jarName = "forzen-$Version.jar"

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

# Stable product-family UUID for Windows upgrades (keep forever across versions).
# Without this, each build can look like a different product and the wizard may
# re-enter install UI / leave Start Menu under "Unknown".
$WinUpgradeUuid = "B8E4F2C1-9A3D-4E7B-8F1C-6D2A5E9B0C47"

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
        "--about-url", "https://FernandezIvan323.github.io/Forzen/",
        "--dest", $out,
        "--win-dir-chooser",
        "--win-menu",
        "--win-menu-group", "Forzen",
        "--win-shortcut",
        "--win-per-user-install",
        "--win-upgrade-uuid", $WinUpgradeUuid,
        "--win-help-url", "https://FernandezIvan323.github.io/Forzen/download.html",
        "--java-options", "--add-modules=javafx.controls,javafx.swing"
    ) + $iconArgs

    Write-Host "==> jpackage --type $pkgType (upgrade-uuid=$WinUpgradeUuid)"
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

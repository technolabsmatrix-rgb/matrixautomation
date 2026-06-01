param(
    [string]$ConfigFile
)

$pomPath = Join-Path $PSScriptRoot '..\pom.xml'
$pomPath = [System.IO.Path]::GetFullPath($pomPath)

$content = Get-Content $pomPath -Raw
$updated = $content -replace '<testSuiteFile>[^<]+</testSuiteFile>', "<testSuiteFile>$ConfigFile</testSuiteFile>"
Set-Content $pomPath $updated -Encoding UTF8

Write-Host "pom.xml updated -> $ConfigFile"
Write-Host "Running: mvn test"

Set-Location (Split-Path $pomPath)
mvn test

# Compile and test the backend samples with nothing but a JDK (21+).
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory out | Out-Null
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName } | Set-Content out/sources.txt
javac --release 21 -Xlint:all -Werror -d out '@out/sources.txt'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp out showcase.TestRunner
exit $LASTEXITCODE

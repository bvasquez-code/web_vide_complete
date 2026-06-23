param(
    [Parameter(Mandatory = $true)]
    [string]$RootPath,

    [string]$OutputPath = "",

    [string]$CategoryCod = "",

    [string]$CreationUser = "IMPORT",

    [string[]]$Extensions = @(".mp4", ".webm", ".mkv", ".avi", ".mov", ".m4v")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$runStamp = Get-Date -Format "yyyyMMdd_HHmmss"
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = "database/inserts/generated_hidden_video_catalog_$runStamp.sql"
}

$baseScript = Join-Path $PSScriptRoot "generate_video_catalog_sql.ps1"
& $baseScript `
    -RootPath $RootPath `
    -OutputPath $OutputPath `
    -CategoryCod $CategoryCod `
    -CreationUser $CreationUser `
    -Extensions $Extensions `
    -OnlyHidden

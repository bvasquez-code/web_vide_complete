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

function ConvertTo-SqlString {
    param([AllowNull()][string]$Value)
    if ([string]::IsNullOrEmpty($Value)) {
        return "NULL"
    }
    return "'" + $Value.Replace("'", "''") + "'"
}

function ConvertTo-VideoPathValue {
    param([string]$Path)
    return $Path.Replace("\", "/")
}

function Get-StableCode {
    param(
        [string]$Prefix,
        [string]$Value
    )
    $sha = [System.Security.Cryptography.SHA256]::Create()
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value.ToUpperInvariant().Trim())
    $hash = $sha.ComputeHash($bytes)
    $hex = [System.BitConverter]::ToString($hash).Replace("-", "")
    $suffixLength = 16 - $Prefix.Length
    return ($Prefix + $hex.Substring(0, $suffixLength)).ToUpperInvariant()
}

function Normalize-NameKey {
    param([string]$Name)
    $normalized = $Name.Trim().ToUpperInvariant()
    $normalized = [System.Text.RegularExpressions.Regex]::Replace($normalized, "\s+", " ")
    return $normalized
}

function Split-Actors {
    param([string]$ActorText)
    $clean = $ActorText.Trim()
    $clean = [System.Text.RegularExpressions.Regex]::Replace($clean, "\s+", " ")
    $clean = [System.Text.RegularExpressions.Regex]::Replace($clean, "\s+(?i:and)\s+", ",")
    $clean = [System.Text.RegularExpressions.Regex]::Replace($clean, "\s+y\s+", ",", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
    return $clean.Split(",", [System.StringSplitOptions]::RemoveEmptyEntries) |
        ForEach-Object { $_.Trim() } |
        Where-Object { $_.Length -gt 0 }
}

function Parse-VideoFileName {
    param([string]$BaseName)
    $match = [System.Text.RegularExpressions.Regex]::Match($BaseName, "^\s*(?<actors>.+?)\s+-\s+(?<title>.+?)\s*$")
    if (-not $match.Success) {
        return $null
    }
    $actors = @(Split-Actors -ActorText $match.Groups["actors"].Value)
    $title = $match.Groups["title"].Value.Trim()
    if ($actors.Count -eq 0 -or [string]::IsNullOrWhiteSpace($title)) {
        return $null
    }
    return [pscustomobject]@{
        Actors = $actors
        Title = $title
    }
}

$runStamp = Get-Date -Format "yyyyMMdd_HHmmss"
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = "database/inserts/generated_video_catalog_$runStamp.sql"
}

$resolvedRoot = (Resolve-Path -LiteralPath $RootPath).Path
$videoFiles = Get-ChildItem -LiteralPath $resolvedRoot -Recurse -File |
    Where-Object { $Extensions -contains $_.Extension.ToLowerInvariant() } |
    Sort-Object FullName

$actorsByKey = [ordered]@{}
$videos = New-Object System.Collections.Generic.List[object]
$unclassified = New-Object System.Collections.Generic.List[string]
$unclassifiedTagName = "sin clasificar"
$unclassifiedTagCod = Get-StableCode -Prefix "TAG" -Value (Normalize-NameKey -Name $unclassifiedTagName)

foreach ($file in $videoFiles) {
    $parsed = Parse-VideoFileName -BaseName $file.BaseName
    $actorCodes = New-Object System.Collections.Generic.List[string]

    if ($null -eq $parsed) {
        $unclassified.Add((ConvertTo-VideoPathValue -Path $file.FullName))
        $title = $file.BaseName.Trim()
        $tagCodes = @($unclassifiedTagCod)
    } else {
        foreach ($actorName in $parsed.Actors) {
            $actorKey = Normalize-NameKey -Name $actorName
            if (-not $actorsByKey.Contains($actorKey)) {
                $actorsByKey[$actorKey] = [pscustomobject]@{
                    ActorCod = Get-StableCode -Prefix "ACT" -Value $actorKey
                    Name = $actorName.Trim()
                }
            }
            $actorCodes.Add($actorsByKey[$actorKey].ActorCod)
        }
        $title = $file.BaseName.Trim()
        $tagCodes = @()
    }

    $videoCod = Get-StableCode -Prefix "VID" -Value $file.FullName
    $videos.Add([pscustomobject]@{
        VideoCod = $videoCod
        Title = $title
        SourceValue = ConvertTo-VideoPathValue -Path $file.FullName
        ActorCodes = @($actorCodes)
        TagCodes = @($tagCodes)
    })
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("-- SQL generado por scripts/generate_video_catalog_sql.ps1")
$lines.Add("-- Correlativo: $runStamp")
$lines.Add("-- RootPath: $(ConvertTo-VideoPathValue -Path $resolvedRoot)")
$lines.Add("-- Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$lines.Add("")

foreach ($actor in $actorsByKey.Values) {
    $lines.Add("INSERT INTO ``actor`` (``ActorCod``, ``Name``, ``Description``, ``ImageUrl``, ``CreationUser``, ``Status``)")
    $lines.Add("SELECT $(ConvertTo-SqlString $actor.ActorCod), $(ConvertTo-SqlString $actor.Name), NULL, NULL, $(ConvertTo-SqlString $CreationUser), 'A'")
    $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``actor`` WHERE ``ActorCod`` = $(ConvertTo-SqlString $actor.ActorCod));")
    $lines.Add("")
}

if ($unclassified.Count -gt 0) {
    $lines.Add("INSERT INTO ``tag`` (``TagCod``, ``Name``, ``CreationUser``, ``Status``)")
    $lines.Add("SELECT $(ConvertTo-SqlString $unclassifiedTagCod), $(ConvertTo-SqlString $unclassifiedTagName), $(ConvertTo-SqlString $CreationUser), 'A'")
    $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``tag`` WHERE ``Name`` = $(ConvertTo-SqlString $unclassifiedTagName));")
    $lines.Add("")
}

foreach ($video in $videos) {
    $shortDescription = $video.Title
    $lines.Add("INSERT INTO ``video`` (``VideoCod``, ``Title``, ``ShortDescription``, ``LongDescription``, ``ThumbnailUrl``, ``SourceType``, ``SourceValue``, ``Duration``, ``ViewCount``, ``PublishDate``, ``CreationUser``, ``Status``)")
    $lines.Add("SELECT $(ConvertTo-SqlString $video.VideoCod), $(ConvertTo-SqlString $video.Title), $(ConvertTo-SqlString $shortDescription), $(ConvertTo-SqlString $video.Title), NULL, 'PATH', $(ConvertTo-SqlString $video.SourceValue), NULL, 0, NULL, $(ConvertTo-SqlString $CreationUser), 'A'")
    $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``video`` WHERE ``VideoCod`` = $(ConvertTo-SqlString $video.VideoCod));")
    $lines.Add("")

    if (-not [string]::IsNullOrWhiteSpace($CategoryCod)) {
        $lines.Add("INSERT INTO ``video_category_rel`` (``VideoCod``, ``CategoryCod``, ``IsPrimary``, ``CreationUser``, ``Status``)")
        $lines.Add("SELECT $(ConvertTo-SqlString $video.VideoCod), $(ConvertTo-SqlString $CategoryCod), 'Y', $(ConvertTo-SqlString $CreationUser), 'A'")
        $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``video_category_rel`` WHERE ``VideoCod`` = $(ConvertTo-SqlString $video.VideoCod) AND ``CategoryCod`` = $(ConvertTo-SqlString $CategoryCod));")
        $lines.Add("")
    }

    foreach ($actorCod in $video.ActorCodes) {
        $lines.Add("INSERT INTO ``video_actor_rel`` (``VideoCod``, ``ActorCod``, ``CreationUser``, ``Status``)")
        $lines.Add("SELECT $(ConvertTo-SqlString $video.VideoCod), $(ConvertTo-SqlString $actorCod), $(ConvertTo-SqlString $CreationUser), 'A'")
        $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``video_actor_rel`` WHERE ``VideoCod`` = $(ConvertTo-SqlString $video.VideoCod) AND ``ActorCod`` = $(ConvertTo-SqlString $actorCod));")
        $lines.Add("")
    }

    foreach ($tagCod in $video.TagCodes) {
        $lines.Add("INSERT INTO ``video_tag_rel`` (``VideoCod``, ``TagCod``, ``CreationUser``, ``Status``)")
        if ($tagCod -eq $unclassifiedTagCod) {
            $lines.Add("SELECT $(ConvertTo-SqlString $video.VideoCod), t.``TagCod``, $(ConvertTo-SqlString $CreationUser), 'A'")
            $lines.Add("FROM ``tag`` t")
            $lines.Add("WHERE t.``Name`` = $(ConvertTo-SqlString $unclassifiedTagName)")
            $lines.Add("AND NOT EXISTS (SELECT 1 FROM ``video_tag_rel`` WHERE ``VideoCod`` = $(ConvertTo-SqlString $video.VideoCod) AND ``TagCod`` = t.``TagCod``);")
        } else {
            $lines.Add("SELECT $(ConvertTo-SqlString $video.VideoCod), $(ConvertTo-SqlString $tagCod), $(ConvertTo-SqlString $CreationUser), 'A'")
            $lines.Add("WHERE NOT EXISTS (SELECT 1 FROM ``video_tag_rel`` WHERE ``VideoCod`` = $(ConvertTo-SqlString $video.VideoCod) AND ``TagCod`` = $(ConvertTo-SqlString $tagCod));")
        }
        $lines.Add("")
    }
}

if ($unclassified.Count -gt 0) {
    $lines.Add("-- Archivos importados con tag 'sin clasificar' porque no cumplen el patron 'actor1, actor2 y actor3 - pelicula':")
    foreach ($path in $unclassified) {
        $lines.Add("-- $path")
    }
}

$outputDirectory = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}

$lines | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "SQL generado: $OutputPath"
Write-Host "Videos procesados: $($videos.Count)"
Write-Host "Actores detectados: $($actorsByKey.Count)"
Write-Host "Videos sin clasificar: $($unclassified.Count)"

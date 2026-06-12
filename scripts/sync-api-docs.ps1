param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'

function Convert-ToSlug {
    param([string]$Value)
    $slug = $Value.Trim().ToLowerInvariant() -replace '[^a-z0-9가-힣]+', '-'
    $slug = $slug.Trim('-')
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return 'api'
    }
    return $slug
}

function Resolve-SnippetPath {
    param(
        [string]$SourceFile,
        [string]$IncludePath,
        [string]$SnippetsRoot
    )

    $path = $IncludePath -replace '\{snippets\}', $SnippetsRoot
    if ([System.IO.Path]::IsPathRooted($path)) {
        return $path
    }
    return [System.IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $SourceFile) $path))
}

function Convert-AdocSnippetToMarkdown {
    param(
        [string]$SnippetPath,
        [string]$Title
    )

    if (-not (Test-Path -LiteralPath $SnippetPath)) {
        return @("### $Title", '', '_Snippet not generated yet._', '')
    }

    $content = Get-Content -LiteralPath $SnippetPath -Raw
    $content = $content -replace '(?m)^\[source,http,options="nowrap"\]\s*\r?\n----\s*\r?\n', "``````http`n"
    $content = $content -replace '(?m)^----\s*$', '```'
    return @("### $Title", '', $content.Trim(), '')
}

Push-Location $ProjectRoot
try {
    $changedAsciiDocs = git status --porcelain -- '*.adoc' '*.asciidoc' |
        ForEach-Object {
            if ($_ -match '^(?<status>.{2})\s+(?<path>.+)$') {
                $Matches.path.Trim('"')
            }
        } |
        Where-Object {
            $_ -and (Test-Path -LiteralPath (Join-Path $ProjectRoot $_)) -and ($_.ToLowerInvariant().EndsWith('.adoc') -or $_.ToLowerInvariant().EndsWith('.asciidoc'))
        } |
        Select-Object -Unique

    if (-not $changedAsciiDocs -or $changedAsciiDocs.Count -eq 0) {
        exit 0
    }

    $docsDir = Join-Path $ProjectRoot 'docs\api'
    New-Item -ItemType Directory -Force -Path $docsDir | Out-Null

    foreach ($relativePath in $changedAsciiDocs) {
        $sourceFile = Join-Path $ProjectRoot $relativePath
        $sourceText = Get-Content -LiteralPath $sourceFile -Raw
        $sourceLines = Get-Content -LiteralPath $sourceFile
        $snippetsRoot = Join-Path $ProjectRoot 'backend\web\build\generated-snippets'

        $sections = New-Object System.Collections.Generic.List[object]
        $current = $null
        foreach ($line in $sourceLines) {
            if ($line -match '^==\s+(.+?)\s*$') {
                if ($null -ne $current) {
                    $sections.Add($current)
                }
                $current = [ordered]@{
                    Title = $Matches[1].Trim()
                    Lines = New-Object System.Collections.Generic.List[string]
                }
            } elseif ($null -ne $current) {
                $current.Lines.Add($line)
            }
        }
        if ($null -ne $current) {
            $sections.Add($current)
        }

        foreach ($section in $sections) {
            $title = $section.Title
            $slug = Convert-ToSlug $title
            $outFile = Join-Path $docsDir "$slug.md"
            $body = New-Object System.Collections.Generic.List[string]

            $body.Add("# $title API")
            $body.Add('')
            $body.Add("> Source: ``$relativePath``")
            $body.Add('')

            $includeLines = $section.Lines | Where-Object { $_ -match '^include::(.+?)\[\]\s*$' }
            if ($includeLines.Count -eq 0) {
                foreach ($plainLine in ($section.Lines | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })) {
                    $body.Add($plainLine)
                }
            } else {
                foreach ($includeLine in $includeLines) {
                    $includeLine -match '^include::(.+?)\[\]\s*$' | Out-Null
                    $includePath = $Matches[1]
                    $snippetPath = Resolve-SnippetPath -SourceFile $sourceFile -IncludePath $includePath -SnippetsRoot $snippetsRoot
                    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($snippetPath)
                    $snippetTitle = switch ($fileName) {
                        'http-request' { 'HTTP Request' }
                        'http-response' { 'HTTP Response' }
                        default { $fileName }
                    }
                    foreach ($snippetLine in (Convert-AdocSnippetToMarkdown -SnippetPath $snippetPath -Title $snippetTitle)) {
                        $body.Add($snippetLine)
                    }
                }
            }

            Set-Content -LiteralPath $outFile -Value ($body -join [Environment]::NewLine) -Encoding UTF8
        }

        if ($sections.Count -gt 0) {
            $indexFile = Join-Path $docsDir 'README.md'
            $index = @('# API Documents', '')
            $index += Get-ChildItem -LiteralPath $docsDir -Filter '*.md' |
                Where-Object { $_.Name -ne 'README.md' } |
                Sort-Object Name |
                ForEach-Object { "- [$($_.BaseName)]($($_.Name))" }
            Set-Content -LiteralPath $indexFile -Value ($index -join [Environment]::NewLine) -Encoding UTF8
        }
    }
} finally {
    Pop-Location
}

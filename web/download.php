<?php
$allowed = [
    'rhvoice-core.apk',
    'rhvoice-Polish.apk',
    'rhvoice-Cezary.apk',
    'rhvoice-Alicja.apk',
    'rhvoice-Magda.apk',
    'rhvoice-Michal.apk',
    'rhvoice-Natan.apk',
];

$file = isset($_GET['file']) ? basename((string) $_GET['file']) : '';
if (!in_array($file, $allowed, true)) {
    http_response_code(404);
    exit('Not found');
}

$baseDir = __DIR__ . '/downloads';
$path = $baseDir . '/' . $file;
if (!is_file($path)) {
    http_response_code(404);
    exit('Not found');
}

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$shouldCount = ($method === 'GET');

if ($shouldCount) {
    $dataDir = getenv('RHVOICE_DATA_DIR');
    if ($dataDir === false || $dataDir === '') {
        $dataDir = dirname(__DIR__) . '/.rhvoice-data';
    }
    $dataDir = rtrim($dataDir, '/');
    if (!is_dir($dataDir)) {
        @mkdir($dataDir, 0755, true);
    }
    $countsFile = $dataDir . '/download-counts.json';
    $fp = @fopen($countsFile, 'c+');
    if ($fp) {
        if (flock($fp, LOCK_EX)) {
            $raw = stream_get_contents($fp);
            $counts = [];
            if ($raw !== false && $raw !== '') {
                $decoded = json_decode($raw, true);
                if (is_array($decoded)) {
                    $counts = $decoded;
                }
            }
            $counts[$file] = (int) ($counts[$file] ?? 0) + 1;
            ftruncate($fp, 0);
            rewind($fp);
            fwrite($fp, json_encode($counts, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));
            fflush($fp);
            flock($fp, LOCK_UN);
        }
        fclose($fp);
    }
}

header('Content-Type: application/vnd.android.package-archive');
header('Content-Disposition: attachment; filename="' . $file . '"');
header('Content-Length: ' . filesize($path));
header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store, no-cache, must-revalidate');

if ($method === 'HEAD') {
    exit;
}

@set_time_limit(0);
readfile($path);

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

$dataDir = getenv('RHVOICE_DATA_DIR');
if ($dataDir === false || $dataDir === '') {
    $dataDir = dirname(__DIR__) . '/.rhvoice-data';
}
$dataDir = rtrim($dataDir, '/');
$countsFile = $dataDir . '/download-counts.json';
$counts = [];

if (is_file($countsFile)) {
    $raw = file_get_contents($countsFile);
    if ($raw !== false && $raw !== '') {
        $decoded = json_decode($raw, true);
        if (is_array($decoded)) {
            $counts = $decoded;
        }
    }
}

$result = [];
foreach ($allowed as $file) {
    $result[$file] = (int) ($counts[$file] ?? 0);
}

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate');

echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

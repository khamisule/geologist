/* Geologist Service Worker — huifadhi app + tiles kwa matumizi ya OFFLINE (iPhone PWA/web).
   Kumbuka: kwenye APK ya Android (file://) SW haitumiki — pale tunatumia native cache (MainActivity). */
const CACHE = 'geologist-v1';
const TILES = 'geologist-tiles-v1';
const SHELL = [
  './', 'index.html', 'Geologist.html', 'map3d.html', 'manifest.json',
  'lib/leaflet.js', 'lib/leaflet.css', 'lib/leaflet-heat.js', 'lib/jspdf.umd.min.js',
  'lib/maplibre-gl.js', 'lib/maplibre-gl.css',
  'icon-192.png', 'icon-512.png'
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(c => Promise.allSettled(SHELL.map(u => c.add(u)))).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE && k !== TILES).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

function isTile(url) {
  return /arcgisonline\.com|mt1\.google\.com|elevation-tiles-prod|amazonaws\.com/.test(url);
}

self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  const url = e.request.url;

  // Tiles za satelaiti/DEM: cache-first (hifadhi mara ya kwanza -> offline baadaye)
  if (isTile(url)) {
    e.respondWith(caches.open(TILES).then(async c => {
      const hit = await c.match(e.request);
      if (hit) return hit;
      try {
        const res = await fetch(e.request);
        if (res) { try { c.put(e.request, res.clone()); } catch (x) {} }
        return res;
      } catch (err) {
        return hit || new Response('', { status: 504 });
      }
    }));
    return;
  }

  // App shell: cache-first, kisha network
  e.respondWith(
    caches.match(e.request).then(hit => hit || fetch(e.request).then(res => {
      try { const cp = res.clone(); caches.open(CACHE).then(c => c.put(e.request, cp)); } catch (x) {}
      return res;
    }).catch(() => caches.match('index.html') || caches.match('Geologist.html')))
  );
});

const CACHE_NAME = 'ratelog-v1';
const STATIC_ASSETS = [
  '/styles.css',
  '/nav.css',
  '/form.css',
  '/texts.css',
  '/generic.css',
  '/components.css',
  '/feedback.css',
  '/community.css',
  '/lucide.min.js',
  '/img/logo.svg',
  '/img/logo-name.svg'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) =>
      Promise.all(
        STATIC_ASSETS.map((url) =>
          fetch(url).then((res) => {
            if (res.ok) cache.put(url, res);
          }).catch(() => {})
        )
      )
    )
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cached) => cached || fetch(event.request).then((response) => {
      if (response.ok) {
        const clone = response.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
      }
      return response;
    }).catch(() => caches.match('/')))
  );
});

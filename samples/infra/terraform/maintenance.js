// CloudFront Function (viewer request), attached only while hibernated.
// Public pages and static assets pass through; app routes get a branded 503;
// the status document tells an already-loaded SPA that the platform is paused.

var PUBLIC_PATHS = ['/', '/privacy', '/terms', '/index.html'];
var STATIC_ASSET = /\.(js|css|png|jpe?g|svg|webp|ico|woff2?|json|txt|map)$/;

function handler(event) {
  var uri = event.request.uri;

  if (uri === '/platform-status.json') {
    return json(200, '{"paused":true}');
  }
  if (PUBLIC_PATHS.indexOf(uri) !== -1 || STATIC_ASSET.test(uri)) {
    return event.request;
  }
  return {
    statusCode: 503,
    statusDescription: 'Service Unavailable',
    headers: {
      'content-type': { value: 'text/html; charset=utf-8' },
      'retry-after': { value: '86400' },
      'cache-control': { value: 'no-store' },
    },
    body:
      '<!doctype html><title>Flame is resting</title>' +
      '<main style="font-family:system-ui;max-width:32rem;margin:15vh auto;text-align:center">' +
      '<h1>Flame is between seasons</h1>' +
      '<p>The platform is temporarily paused. Your data is safe and will be back when competition resumes.</p>' +
      '</main>',
  };
}

function json(status, body) {
  return {
    statusCode: status,
    headers: {
      'content-type': { value: 'application/json' },
      'cache-control': { value: 'no-store' },
    },
    body: body,
  };
}

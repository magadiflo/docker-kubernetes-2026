#!/bin/sh

envsubst '${GATEWAY_SERVER_BASE_URL}' < /usr/share/nginx/html/assets/env.js > /tmp/env.js \
  && mv /tmp/env.js /usr/share/nginx/html/assets/env.js

echo "✓ env.js configurado"

export ENV_CONFIG_VERSION=$(date +%s)
envsubst '${ENV_CONFIG_VERSION}' < /usr/share/nginx/html/index.html > /tmp/index.html \
  && mv /tmp/index.html /usr/share/nginx/html/index.html

echo "✓ index.html configurado"

exec "$@"

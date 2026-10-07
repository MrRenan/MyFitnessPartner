#!/bin/sh
# ─────────────────────────────────────────────────────────
# Bootstrap do Let's Encrypt — rode UMA VEZ na VM após o DNS propagar.
#
# Resolve o ovo-e-galinha: o Nginx precisa de certificado para subir em
# HTTPS, mas o certbot precisa do Nginx no ar (porta 80) para validar.
#
# Uso (na VM, dentro de ~/myfitness):
#   chmod +x nginx/init-letsencrypt.sh
#   ./nginx/init-letsencrypt.sh
# ─────────────────────────────────────────────────────────
set -e

DOMAIN="fitness.renanleite.dev.br"
EMAIL="renan.carli@hotmail.com"   # usado pelo Let's Encrypt p/ avisos de expiração
COMPOSE="docker compose -f docker-compose.prod.yml"

echo "==> 1. Subindo app + nginx (HTTP only, para o desafio ACME)..."
# Sobe tudo; o nginx vai reclamar da falta de cert, então usamos um cert dummy primeiro
CERT_PATH="/etc/letsencrypt/live/${DOMAIN}"

echo "==> 2. Criando certificado temporário (dummy) para o nginx conseguir subir..."
$COMPOSE run --rm --entrypoint "\
  sh -c 'mkdir -p ${CERT_PATH} && \
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout ${CERT_PATH}/privkey.pem \
    -out ${CERT_PATH}/fullchain.pem \
    -subj /CN=localhost'" certbot

echo "==> 3. Subindo nginx com o cert dummy..."
$COMPOSE up -d nginx
sleep 5

echo "==> 4. Removendo o cert dummy..."
$COMPOSE run --rm --entrypoint "\
  rm -rf /etc/letsencrypt/live/${DOMAIN} \
         /etc/letsencrypt/archive/${DOMAIN} \
         /etc/letsencrypt/renewal/${DOMAIN}.conf" certbot

echo "==> 5. Solicitando o certificado real ao Let's Encrypt..."
$COMPOSE run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    --email ${EMAIL} \
    -d ${DOMAIN} \
    --rsa-key-size 2048 \
    --agree-tos \
    --no-eff-email \
    --force-renewal" certbot

echo "==> 6. Recarregando o nginx com o certificado real..."
$COMPOSE exec nginx nginx -s reload

echo ""
echo "==> PRONTO! Acesse https://${DOMAIN}"

#!/bin/bash
# Script de configuration automatique de Keycloak
# Usage: ./setup-keycloak.sh

set -e

KEYCLOAK_URL="http://localhost:8084"
REALM="weather-event"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "⏳ Attente de Keycloak..."
for i in $(seq 1 $MAX_RETRIES); do
    if curl -s "$KEYCLOAK_URL/realms/master" > /dev/null 2>&1; then
        echo "✅ Keycloak est disponible"
        break
    fi
    echo "  Tentative $i/$MAX_RETRIES..."
    sleep $RETRY_INTERVAL
done

echo " Obtention du token admin..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "❌ Impossible d'obtenir le token admin"
    exit 1
fi

echo " Création du realm $REALM..."
REALM_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"realm\":\"$REALM\",\"enabled\":true,\"attributes\":{\"frontendUrl\":\"$KEYCLOAK_URL\"}}")

if [ "$REALM_RESULT" = "201" ]; then
    echo " Realm créé"
elif [ "$REALM_RESULT" = "409" ]; then
    echo "  Realm existe déjà - mise à jour du frontendUrl..."
    ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
      -d "client_id=admin-cli" -d "username=admin" -d "password=admin" \
      -d "grant_type=password" | jq -r '.access_token')
    curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"realm\":\"$REALM\",\"enabled\":true,\"attributes\":{\"frontendUrl\":\"$KEYCLOAK_URL\"}}"
    echo "✅ Realm mis à jour"
fi

ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" -d "username=admin" -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')

echo " Création du client weather-app..."
CLIENT_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms/$REALM/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"clientId":"weather-app","enabled":true,"publicClient":true,"directAccessGrantsEnabled":true}')

if [ "$CLIENT_RESULT" = "201" ]; then
    echo "Client créé"
else
    echo "  Client existe déjà"
fi

ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" -d "username=admin" -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')

echo " Création de l'utilisateur test..."
USER_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"user","enabled":true,"emailVerified":true,"firstName":"Test","lastName":"User","email":"user@weather-event.com","credentials":[{"type":"password","value":"password","temporary":false}]}')

if [ "$USER_RESULT" = "201" ]; then
    echo " Utilisateur créé"
else
    echo "  Utilisateur existe déjà"
fi

echo ""
echo " Test de connexion..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -d "client_id=weather-app" -d "username=user" -d "password=password" \
  -d "grant_type=password" | jq -r '.access_token')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo " Keycloak configuré avec succès!"
    echo "   URL: $KEYCLOAK_URL"
    echo "   Realm: $REALM"
    echo "   Client: weather-app"
    echo "   User: user / password"
else
    echo " Erreur de configuration"
    exit 1
fi

#!/bin/bash

echo "=== CONFIGURATION DE KEYCLOAK ==="

# 1. Obtenir le token admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8084/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
  echo "❌ Impossible d'obtenir le token admin"
  exit 1
fi
echo "✅ Token admin obtenu"

# 2. Créer le realm weather-event
echo "Création du realm weather-event..."
curl -s -X POST http://localhost:8084/admin/realms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realm": "weather-event",
    "enabled": true,
    "displayName": "Weather Event API"
  }'

# 3. Créer le client weather-app
echo "Création du client weather-app..."
curl -s -X POST http://localhost:8084/admin/realms/weather-event/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "weather-app",
    "name": "Weather Event Application",
    "publicClient": true,
    "directAccessGrantsEnabled": true,
    "standardFlowEnabled": true
  }'

# 4. Créer l'utilisateur test
echo "Création de l'utilisateur user..."
curl -s -X POST http://localhost:8084/admin/realms/weather-event/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "email": "user@weather-event.com",
    "enabled": true,
    "firstName": "Test",
    "lastName": "User",
    "credentials": [{
      "type": "password",
      "value": "password",
      "temporary": false
    }]
  }'

echo ""
echo " Configuration terminée"

#!/bin/bash
# CostIQ API Test Script
# Run: chmod +x test_api.sh && ./test_api.sh

KEYCLOAK=http://localhost:8080
REALM=costiq-realm
CLIENT=costiq-app
USER=admin.demo
PASS=Password1
API=http://localhost:8085/costiq/api

echo "=== Step 1: Getting token from Keycloak ==="
RESPONSE=$(curl -s -X POST \
  ${KEYCLOAK}/realms/${REALM}/protocol/openid-connect/token \
  --data-urlencode "grant_type=password" \
  --data-urlencode "client_id=${CLIENT}" \
  --data-urlencode "username=${USER}" \
  --data-urlencode "password=${PASS}" \
  --data-urlencode "scope=openid")

echo "Raw response: $RESPONSE" | head -c 200
echo ""

TOKEN=$(echo $RESPONSE | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('access_token','ERROR:'+str(d)))" 2>&1)

if [[ $TOKEN == ERROR* ]]; then
  echo "Token fetch failed: $TOKEN"
  exit 1
fi

echo "Token obtained: ${TOKEN:0:30}..."
echo ""

echo "=== Step 2: Testing /api/food-costs ==="
curl -s ${API}/food-costs \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Accept: application/json" | python3 -m json.tool 2>&1 | head -30

echo ""
echo "=== Step 3: Testing /api/suppliers ==="
curl -s ${API}/suppliers \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Accept: application/json" | python3 -m json.tool 2>&1 | head -30

echo ""
echo "=== Step 4: Testing /api/countries ==="
curl -s ${API}/countries \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Accept: application/json" | python3 -m json.tool 2>&1 | head -30
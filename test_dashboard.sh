#!/bin/bash

# Test login and dashboard access
echo "=== Testing Login ==="
COOKIE_FILE="/tmp/test_cookies.txt"
rm -f $COOKIE_FILE

# Login
curl -s -c $COOKIE_FILE -L \
  -d "username=admin&password=admin123" \
  http://localhost:8084/login > /tmp/login_response.html

echo "Login response status:"
grep -o "HTTP/[0-9.]* [0-9]*" /tmp/login_response.html || echo "Login successful (no HTTP status in body)"

echo ""
echo "=== Testing Dashboard ==="
DASHBOARD_RESPONSE=$(curl -s -b $COOKIE_FILE http://localhost:8084/dashboard -w "\nHTTPSTATUS:%{http_code}")

HTTP_CODE=$(echo "$DASHBOARD_RESPONSE" | grep "HTTPSTATUS" | cut -d: -f2)
DASHBOARD_BODY=$(echo "$DASHBOARD_RESPONSE" | sed '/HTTPSTATUS/d')

echo "Dashboard HTTP Status: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "200" ]; then
  echo "✓ Dashboard accessed successfully!"
  echo ""
  echo "Dashboard Title:"
  echo "$DASHBOARD_BODY" | grep -o "<title>.*</title>" | head -1
  echo ""
  echo "Dashboard Header:"
  echo "$DASHBOARD_BODY" | grep -A 2 "欢迎回来" | head -3
else
  echo "✗ Dashboard access failed with HTTP $HTTP_CODE"
  echo ""
  echo "Dashboard Response (first 500 chars):"
  echo "$DASHBOARD_BODY" | head -c 500
fi

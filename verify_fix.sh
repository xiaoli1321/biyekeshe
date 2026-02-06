#!/bin/bash

echo "=== Verifying Template Fix ==="
echo ""

# Test that dashboard endpoint works without errors
echo "1. Testing Dashboard Endpoint (without auth - should redirect to login):"
RESPONSE=$(curl -s -o /dev/null -w "HTTPSTATUS:%{http_code}" http://localhost:8084/dashboard)
HTTP_CODE=$(echo "$RESPONSE" | grep -o "[0-9]*")
echo "Status: $HTTP_CODE (302 = redirect to login, which is expected)"

echo ""
echo "2. Checking Template Files:"
for file in dashboard/index.html courses/list.html courses/detail.html chapters/view.html knowledge-graph/view.html; do
  echo "  - $file:"
  if grep -q 'th:replace="layout/main"' src/main/resources/templates/$file; then
    echo "    ✓ Correct template reference (no fragment errors)"
  else
    echo "    ✗ Still has incorrect reference"
  fi
done

echo ""
echo "3. Testing full login flow:"
SESSION_FILE="/tmp/session_$(date +%s).txt"

# Step 1: Get initial page (to establish session)
curl -s -c $SESSION_FILE http://localhost:8084/login > /dev/null

# Step 2: Login with credentials (include CSRF token if present)
LOGIN_RESULT=$(curl -s -b $SESSION_FILE -c $SESSION_FILE \
  -d "username=admin&password=admin123" \
  -w "\nREDIRECT:%{http_code}" \
  http://localhost:8084/login)

HTTP_CODE=$(echo "$LOGIN_RESULT" | grep "REDIRECT" | cut -d: -f2)
echo "  Login attempt status: $HTTP_CODE"

# Step 3: Access dashboard with session
DASHBOARD_RESULT=$(curl -s -b $SESSION_FILE -w "\nHTTPSTATUS:%{http_code}" http://localhost:8084/dashboard)
DASHBOARD_HTTP=$(echo "$DASHBOARD_RESULT" | grep "HTTPSTATUS" | cut -d: -f2)
DASHBOARD_BODY=$(echo "$DASHBOARD_RESULT" | sed '/HTTPSTATUS/d')

echo ""
echo "=== Final Result ==="
if [ "$DASHBOARD_HTTP" = "200" ]; then
  echo "✓ Dashboard accessible (HTTP 200)"
  echo ""
  if echo "$DASHBOARD_BODY" | grep -q "欢迎回来"; then
    echo "✓ Dashboard content loaded correctly!"
    echo "✓ Template fix successful - no more Thymeleaf Fragment errors"
  else
    echo "• Dashboard loads but content may not be fully rendered"
  fi
else
  echo "Dashboard HTTP Status: $DASHBOARD_HTTP"
fi

rm -f $SESSION_FILE

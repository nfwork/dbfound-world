#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USER_CODE="smoke_$(date +%s)"

request_json() {
  local title="$1"
  local path="$2"
  local body="$3"

  echo
  echo "==> ${title}"
  curl -sS -X POST "${BASE_URL}${path}" \
    -H "Content-Type: application/json" \
    -d "${body}"
  echo
}

echo "Smoke test target: ${BASE_URL}"
echo "Make sure the database has been initialized with examples/sql/init.sql."

request_json "query users" "/user.query" '{
  "page_start": 0,
  "page_limit": 5
}'

request_json "add user" "/user.execute!add" "{
  \"user_code\": \"${USER_CODE}\",
  \"user_name\": \"Smoke User\",
  \"password\": \"123456\"
}"

request_json "query added user" "/user.query" "{
  \"user_code\": \"${USER_CODE}\",
  \"page_start\": 0,
  \"page_limit\": 5
}"

request_json "dynamic SQL query" "/userPart.query!sec2" '{
  "fields": ["user_code","user_name"]
}'

request_json "adapter query" "/userAdapter.query" '{
  "user_code": "xiaoming; xiaohong"
}'

request_json "custom function validation" "/function.execute!hello" '{
  "message": "hello world"
}'

echo
echo "Smoke requests completed. Inspect the JSON above for success=true."

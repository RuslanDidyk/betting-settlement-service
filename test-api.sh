#!/bin/bash
# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

BET_IDS_TO_CHECK=(1 2 3 4 5)
BASE_URL="http://localhost:8080"
print_status() {
    local bet_id=$1
    local status=$2

    case "$status" in
        "WON")
            echo -e "Bet ID ${BLUE}$bet_id${NC}: ${GREEN}$status${NC}"
            ;;
        "LOST")
            echo -e "Bet ID ${BLUE}$bet_id${NC}: ${RED}$status${NC}"
            ;;
        "PENDING")
            echo -e "Bet ID ${BLUE}$bet_id${NC}: ${YELLOW}$status${NC}"
            ;;
        "NOT_FOUND")
            echo -e "Bet ID ${BLUE}$bet_id${NC}: ${RED}NOT FOUND${NC}"
            ;;
        *)
            echo -e "Bet ID ${BLUE}$bet_id${NC}: $status"
            ;;
    esac
}
check_jq() {
    if ! command -v jq &> /dev/null; then
        echo "Error: jq is required but not installed."
        echo "Please install jq: https://stedolan.github.io/jq/"
        echo "On Ubuntu/Debian: sudo apt-get install jq"
        echo "On macOS: brew install jq"
        exit 1
    fi
}
fetch_bet_data() {
    local response
    response=$(curl -s "$BASE_URL/api/v1/bets")
    if [ $? -ne 0 ]; then
        echo "Error: Failed to fetch data from $BASE_URL/api/v1/bets"
        exit 1
    fi
    if ! echo "$response" | jq empty 2>/dev/null; then
        echo "Error: Invalid JSON response received"
        echo "Response: $response"
        exit 1
    fi
    echo "$response"
}
get_bet_status() {
    local json_data=$1
    local bet_id=$2

    local status
    status=$(echo "$json_data" | jq -r --arg id "$bet_id" '
          if .content then
              (.content[] | select(.betId == ($id | tonumber)) | .status) // empty
          else
              empty
          end' 2>/dev/null)

      if [ -z "$status" ]; then
          echo "NOT_FOUND"
      else
          echo "$status"
      fi
}
check_bet_status() {
    check_jq
    local json_data
    json_data=$(fetch_bet_data)
    for bet_id in "${BET_IDS_TO_CHECK[@]}"; do
        local status
        status=$(get_bet_status "$json_data" "$bet_id")
        print_status "$bet_id" "$status"
    done
}

echo "🧪 Testing Sports Betting Settlement API"
echo "========================================"

echo -e "\n${YELLOW}1. Health Check${NC}"
curl -s "$BASE_URL/actuator/health" | python3 -m json.tool

# 201 - Team A
# 202 - Team B
# 301 - Team C
# 302 - Team D
echo -e "\n${YELLOW}2. Creating New Bets${NC}"
curl -X POST "$BASE_URL/api/v1/bets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "eventId": 100,
    "eventMarketId": 1,
    "eventWinnerId": 201,
    "betAmount": 100.00
  }' -s | python3 -m json.tool
curl -X POST "$BASE_URL/api/v1/bets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1002,
    "eventId": 100,
    "eventMarketId": 1,
    "eventWinnerId": 202,
    "betAmount": 50.00
  }' -s | python3 -m json.tool
curl -X POST "$BASE_URL/api/v1/bets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1003,
    "eventId": 100,
    "eventMarketId": 1,
    "eventWinnerId": 201,
    "betAmount": 200.00
  }' -s | python3 -m json.tool
curl -X POST "$BASE_URL/api/v1/bets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1004,
    "eventId": 101,
    "eventMarketId": 1,
    "eventWinnerId": 301,
    "betAmount": 125.00
  }' -s | python3 -m json.tool
curl -X POST "$BASE_URL/api/v1/bets" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1005,
    "eventId": 101,
    "eventMarketId": 1,
    "eventWinnerId": 302,
    "betAmount": 75.00
  }' -s | python3 -m json.tool

echo -e "\n${YELLOW}3. Checking all bets created${NC}"
check_bet_status

echo -e "\n${YELLOW}4. Publishing Event Outcome (Event 100, Team 201 wins)${NC}"
curl -X POST "$BASE_URL/api/v1/events" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 100,
    "eventName": "Manchester United vs Liverpool",
    "eventWinnerId": 201
  }' -s | python3 -m json.tool
echo -e "\n${YELLOW}Waiting 3 seconds for processing...${NC}"
sleep 3

echo -e "\n${YELLOW}5. Check bet status${NC}"
check_bet_status

echo -e "\n${YELLOW}6. Publishing Event Outcome (Event 101, Team 301 wins)${NC}"
curl -X POST "$BASE_URL/api/v1/events" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 101,
    "eventName": "Arsenal vs Barcelona",
    "eventWinnerId": 301
  }' -s | python3 -m json.tool
echo -e "\n${YELLOW}Waiting 3 seconds for processing...${NC}"
sleep 3

echo -e "\n${YELLOW}7. Check bet status${NC}"
check_bet_status

echo -e "\n${GREEN}✅ API tests completed!${NC}"
#!/bin/bash

# Test All Taxpayers Script
# This script tests all 4 taxpayer scenarios and displays results

BASE_URL="${1:-http://localhost:8080}"
echo "=========================================="
echo "Tax Risk Engine - Test All Taxpayers"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test function
test_taxpayer() {
    local name=$1
    local taxpayer_id=$2
    local tin=$3
    local expected_score=$4
    local expected_level=$5
    
    echo -e "${YELLOW}Testing: $name${NC}"
    echo "Expected: Score=$expected_score, Level=$expected_level"
    echo ""
    
    response=$(curl -s -X POST "$BASE_URL/api/v1/risk/assess" \
        -H "Content-Type: application/json" \
        -d "{\"taxpayerId\": \"$taxpayer_id\", \"tin\": \"$tin\"}")
    
    echo "Response:"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}Warning: jq is not installed. Output will be unformatted.${NC}"
    echo "Install with: sudo apt-get install jq (Ubuntu/Debian) or brew install jq (Mac)"
    echo ""
fi

# Check if server is running
echo "Checking if server is running..."
health_check=$(curl -s "$BASE_URL/actuator/health" 2>/dev/null)
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Cannot connect to $BASE_URL${NC}"
    echo "Make sure the application is running with:"
    echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
    exit 1
fi
echo -e "${GREEN}✓ Server is running${NC}"
echo ""

# Test 1: Perfect Taxpayer
test_taxpayer \
    "1. Perfect Taxpayer (Meridian Office Supplies PLC)" \
    "73f6813e-4bea-4eb6-8fc8-df9ea8888f2e" \
    "TIN-523938499" \
    "2.97" \
    "LOW"

# Test 2: Late Filing Only
test_taxpayer \
    "2. Late Filing Only (Coastal Fisheries Cooperative)" \
    "965c6a52-5863-491b-927f-e4949eff49b7" \
    "TIN-434848879" \
    "7.84" \
    "LOW"

# Test 3: Late Payment Only
test_taxpayer \
    "3. Late Payment Only (Harborview Auto Repair Ltd)" \
    "efb9ba6b-c0e6-452f-8712-0310fea39f09" \
    "TIN-439280725" \
    "10.56" \
    "LOW"

# Test 4: Multiple Amendments
test_taxpayer \
    "4. Multiple Amendments (Zenith Business Consulting Ltd)" \
    "8646ee0e-54e5-4ce8-a2fb-c60b6fe42a71" \
    "TIN-668212944" \
    "40.83" \
    "MEDIUM"

echo "=========================================="
echo "Testing Complete!"
echo "=========================================="
echo ""
echo "Compare actual scores with expected scores above."
echo "Scores should be within ±5% of expected values."

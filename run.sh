#!/bin/bash
echo "🎲 Sports Betting Settlement Service Startup Script"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi
# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

# Function to wait for service
wait_for_service() {
    local service=$1
    local port=$2
    local max_attempts=30
    local attempt=0

    echo -e "${YELLOW}Waiting for $service to be ready on port $port...${NC}"

    while ! nc -z localhost $port 2>/dev/null; do
        if [ $attempt -eq $max_attempts ]; then
            echo -e "${RED}$service failed to start after $max_attempts attempts${NC}"
            return 1
        fi
        attempt=$((attempt+1))
        sleep 2
    done

    echo -e "${GREEN}$service is ready!${NC}"
    return 0
}

# Start infrastructure
#echo -e "\n${YELLOW}Starting infrastructure services...${NC}"
docker-compose up -d

# Wait for services to be ready
wait_for_service "Zookeeper" 2181
wait_for_service "Kafka" 9092
wait_for_service "RocketMQ" 9876
wait_for_service "Betting Settlement Service" 8080

echo -e "\n${GREEN}Infrastructure services are running!${NC}"
echo "- Kafka UI: http://localhost:8090"
echo "- RocketMQ Console: http://localhost:8091"
echo "- Application: http://localhost:8080"
echo "- H2 Console: http://localhost:8080/h2-console"
echo ""
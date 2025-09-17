#!/bin/bash
echo "🛑 Stopping Sports Betting Settlement Service"
echo "==========================================="

# Stop Spring Boot application (if running)
pkill -f "betting-settlement-service" 2>/dev/null

# Stop Docker services
echo "Stopping Docker services..."
docker-compose down

echo "✅ All services stopped"
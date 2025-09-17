# Sports Betting Settlement Service

A microservice that simulates sports betting event outcome handling and bet settlement using Kafka and RocketMQ.

## Features

- REST API endpoint to publish sports event outcomes to Kafka
- Kafka consumer that processes event outcomes
- Bet matching logic to identify bets that need settlement
- RocketMQ producer for bet settlement messages
- RocketMQ consumer that settles bets
- In-memory H2 database for bet storage

## Technology Stack

- Java 21
- Spring Boot 3.5.5
- Apache Kafka
- Apache RocketMQ
- H2 In-Memory Database
- Gradle

## Prerequisites

- Java 21
- Docker and Docker Compose
- Gradle (or use the Gradle wrapper)

## Quick Start

### 1. Clone the Repository

```bash
    git clone https://github.com/RuslanDidyk/betting-settlement-service.git
    cd betting-settlement-service
```

### 2. Start

Start Kafka and RocketMQ using Docker Compose:

```bash
    ./run.sh
```

This will start:
- Zookeeper (port 2181)
- Kafka (port 9092)
- Kafka UI (port 8090) - http://localhost:8090
- RocketMQ NameServer (port 9876)
- RocketMQ Broker (ports 10909, 10911, 10912)
- RocketMQ Console (port 8091) - http://localhost:8091
- The application will start on http://localhost:8080

### 3. Verify everything is running

Wait for all services to be healthy (approximately 30 seconds):
```bash
    docker-compose ps
```

Check the health endpoint:
```bash
  curl http://localhost:8080/actuator/health
```

## Testing the Complete Flow

```bash
    ./test-api.sh
```

### Pre-loaded Sample Data

The script comes with pre-loaded sample bets for testing:

| Bet ID | User ID | Event ID | Event Winner ID | Bet Amount | Status  |
|--------|---------|----------|-----------------|------------|---------|
| 1      | 1001    | 100      | 201 (Team A)    | $100.00    | PENDING |
| 2      | 1002    | 100      | 202 (Team B)    | $50.00     | PENDING |
| 3      | 1003    | 100      | 201 (Team A)    | $200.00    | PENDING |
| 4      | 1004    | 101      | 301 (Team C)    | $150.00    | PENDING |
| 5      | 1005    | 101      | 302 (Team D)    | $75.00     | PENDING |

### Test Scenario 1: Event 100 - Team A Wins
Expected results:
- Bets 1 and 3 (Team A/201): Status = WON
- Bet 2 (Team B/202): Status = LOST

### Test Scenario 2: Event 101 - Team C Wins
Expected results:
- Bets 4 (Team C/301): Status = WON
- Bet 5 (Team D/302): Status = LOST


## Stopping the Application

```bash
    ./stop.sh
```

## Application Flow

1. **API Endpoint** receives event outcome
2. **Kafka Producer** publishes to `event-outcomes` topic
3. **Kafka Consumer** receives the message
4. **Bet Matching Service** finds pending bets for the event
5. **RocketMQ Producer** sends settlement messages to `bet-settlements` topic
6. **RocketMQ Consumer** processes settlements
7. **Bet Settlement Service** updates bet statuses (WON/LOST)

## Monitoring

### Kafka UI
Access Kafka UI at http://localhost:8090 to:
- View topics and messages
- Monitor consumer groups
- Check message flow

### RocketMQ Console
Access RocketMQ Console at http://localhost:8091 to:
- View topics and messages
- Monitor consumer groups
- Check broker status

### H2 Database Console
Access H2 Console at http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

### Application Logs
The application provides detailed logging. Monitor the console output for:
- Event outcome processing
- Bet matching results
- Settlement processing
- Error messages

## Configuration

Key configuration properties in `application.yml`:

```yaml
# Server port
server.port: 8080

# Kafka settings
spring.kafka.bootstrap-servers: localhost:9092
spring.kafka.consumer.group-id: betting-settlement-group

# RocketMQ settings
rocketmq.name-server: localhost:9876
```

## Troubleshooting

### Issue: Cannot connect to Kafka
- Ensure Docker containers are running: `docker-compose ps`
- Check Kafka is accessible: `telnet localhost 9092`
- Verify Kafka topics are created in Kafka UI

### Issue: RocketMQ connection failed
- Check RocketMQ NameServer: `telnet localhost 9876`
- Verify broker is running: `docker logs rocketmq-broker`
- Check RocketMQ Console for broker status

### Issue: Bets not updating
- Check application logs for errors
- Verify Kafka consumer is receiving messages
- Check RocketMQ consumer logs
- Ensure H2 database is accessible


## Testing with Different Scenarios

You can test various scenarios:

1. **Multiple winners**: Send different winner IDs for the same event
2. **Non-existent event**: Send an event ID with no bets
3. **Concurrent processing**: Send multiple events simultaneously
4. **Error handling**: Send malformed requests

## License

This project is provided as a technical demonstration.

## Support

For issues or questions, please check the application logs first, then refer to the troubleshooting section.
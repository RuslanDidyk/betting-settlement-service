package io.github.ruslandidyk.betting.service;

import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import io.github.ruslandidyk.betting.event.OutcomeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeConsumer {

    private final BetMatchingService betMatchingService;
    private final BetSettlementProducer betSettlementProducer;

    @KafkaListener(topics = "${app.kafka.event-outcomes-topic}", groupId = "${app.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload OutcomeEvent event,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset,
                        @Header(value = "correlationId", required = false) String correlationId,
                        Acknowledgment acknowledgment) {
        log.info("Received event outcome from Kafka - Topic: {}, Partition: {}, Offset: {}, Correlation ID: {}, Data: {}",
                topic, partition, offset, correlationId, event);
        try {
            List<BetSettlementEvent> betSettlementEvents = betMatchingService.matchBets(event);
            if (!betSettlementEvents.isEmpty()) {
                // Send each bet settlement to RocketMQ
                for (BetSettlementEvent settlement : betSettlementEvents) {
                    try {
                        betSettlementProducer.produce(settlement, correlationId);
                    } catch (Exception e) {
                        log.error("Failed to send settlement for bet: {}", settlement.getBetId(), e);
                    }
                }
            } else {
                log.info("No bets to settle for event: {}", event.getEventId());
            }
            // Acknowledge the message after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Message acknowledged for event: {}", event.getEventId());
            }
        } catch (Exception e) {
            log.error("Error processing event outcome: {}", event, e);
            // In production, send to a DLQ or retry
            throw e; // This will trigger a retry mechanism
        }
    }

}

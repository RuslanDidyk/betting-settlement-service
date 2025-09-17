package io.github.ruslandidyk.betting.service;

import io.github.ruslandidyk.betting.event.OutcomeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeProducer {

    private final KafkaTemplate<String, OutcomeEvent> kafkaTemplate;
    @Value("${app.kafka.event-outcomes-topic:event-outcomes}")
    private String topic;

    public void produce(OutcomeEvent outcomeEvent) {
        String correlationId = UUID.randomUUID().toString();
        log.info("Sending event outcome to Kafka - Correlation ID: {}, Event: {}", correlationId, outcomeEvent);
        Message<OutcomeEvent> message = MessageBuilder
                .withPayload(outcomeEvent)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, outcomeEvent.getEventId().toString())
                .setHeader("correlationId", correlationId)
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent event outcome [{}] with offset: {} and partition: {}",
                                outcomeEvent.getEventId(),
                                result.getRecordMetadata().offset(),
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send event outcome [{}]", outcomeEvent.getEventId(), ex);
                    }
                });
    }
}


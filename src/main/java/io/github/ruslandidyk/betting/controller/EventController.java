package io.github.ruslandidyk.betting.controller;

import io.github.ruslandidyk.betting.dto.OutcomeEventRequest;
import io.github.ruslandidyk.betting.event.OutcomeEvent;
import io.github.ruslandidyk.betting.service.EventOutcomeProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventOutcomeProducer eventOutcomeProducer;

    @PostMapping
    public ResponseEntity<Map<String, Object>> publish(@Valid @RequestBody OutcomeEventRequest request) {
        log.info("Received event outcome request: {}", request);
        eventOutcomeProducer.produce(OutcomeEvent.builder()
                .eventId(request.getEventId())
                .eventName(request.getEventName())
                .eventWinnerId(request.getEventWinnerId())
                .build());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "message", "Event outcome published successfully",
                        "eventId", request.getEventId(),
                        "eventName", request.getEventName(),
                        "eventWinnerId", request.getEventWinnerId()
                ));
    }
}

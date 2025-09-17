package io.github.ruslandidyk.betting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutcomeEventRequest {

    @NotNull(message = "Event ID is required")
    @JsonProperty("eventId")
    private Long eventId;

    @NotNull(message = "Event name is required")
    @JsonProperty("eventName")
    private String eventName;

    @NotNull(message = "Event winner ID is required")
    @JsonProperty("eventWinnerId")
    private Long eventWinnerId;
}
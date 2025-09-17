package io.github.ruslandidyk.betting.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutcomeEvent {
    @JsonProperty("eventId")
    private Long eventId;
    
    @JsonProperty("eventName")
    private String eventName;
    
    @JsonProperty("eventWinnerId")
    private Long eventWinnerId;
}
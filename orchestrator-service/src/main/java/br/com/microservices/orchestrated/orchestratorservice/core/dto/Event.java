package br.com.microservices.orchestrated.orchestratorservice.core.dto;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    private String id;

    private String transactionId;

    private String orderId;

    private Order payload;

    private EventSource source;

    private SagaStatus status;
    
    private List<History> eventHistory;
}
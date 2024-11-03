package br.com.microservices.orchestrated.orderservice.core.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event")
public class Event {

    @Id
    private String id;

    private String transactionId;

    private String orderId;

    private Order payload;

    private String source;

    private String status;

    private List<History> eventHistory;
}

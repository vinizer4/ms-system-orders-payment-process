package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.*;
import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Slf4j
@AllArgsConstructor
public class SagaExecutionCoordinator {

    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID: %s | EVENT ID: %s";

    public Topics getNextTopic(Event event) {
        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed.");
        }
        var topic = findTopicBySourceAndStatus(event);
        logCurrentSaga(event, topic);
        return topic;
    }

    private Topics findTopicBySourceAndStatus(Event event) {
        return (Topics) (Arrays.stream(SAGA_HANDLER).filter(
                        row -> isEventSourceAndStatusValid(event, row))
                .map(i -> i[TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("No topic found for source and status.")));
    }

    private boolean isEventSourceAndStatusValid(Event event, Object[] row) {
        var source = row[EVENT_SOURCE_INDEX];
        var status = row[SAGA_STATUS_INDEX];
        return event.getSource().equals(source) && event.getStatus().equals(status);
    }

    private void logCurrentSaga(Event event, Topics topic) {
        var sagaId = createSagaId(event);
        var source = event.getSource();
        switch (event.getStatus()) {
            case SUCCESS -> log.info(
                    "### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC: {} | SAGA ID: {}",
                    source, topic, sagaId
            );
            case ROLLBACK_PENDING -> log.info(
                    "### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC: {} | SAGA ID: {}",
                    source, topic, sagaId
            );
            case FAIL -> log.info(
                    "### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC: {} | SAGA ID: {}",
                    source, topic, sagaId
            );
        }
    }

    private String createSagaId(Event event) {
        return format(SAGA_LOG_ID, event.getOrderId(), event.getTransactionId(), event.getId());
    }
}

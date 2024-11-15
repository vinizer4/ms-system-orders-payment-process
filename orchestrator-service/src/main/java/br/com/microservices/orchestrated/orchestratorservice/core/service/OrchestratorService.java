package br.com.microservices.orchestrated.orchestratorservice.core.service;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionCoordinator;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource.ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus.SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics.NOTIFY_ENDING;

@Service
@Slf4j
@AllArgsConstructor
public class OrchestratorService {

    private final SagaOrchestratorProducer producer;
    private final SagaExecutionCoordinator sagaExecutionCoordinator;
    private final JsonUtil jsonUtil;

    private static final EventSource CURRENT_SOURCE = ORCHESTRATOR;

    public void startSaga(Event event) {
        event.setSource(CURRENT_SOURCE);
        event.setStatus(SUCCESS);
        var topic = getTopic(event);
        log.info("SAGA STARTED");
        addHistory(event, "Saga started");
        sendToProducerWithTopic(event, topic);
    }

    public void finishSagaSuccess(Event event) {
        event.setSource(CURRENT_SOURCE);
        event.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT: {}", event.getId());
        addHistory(event, "Saga Finished Successfully!");
        notifyFinishedSaga(event);
    }

    public void finishSagaFail(Event event) {
        event.setSource(CURRENT_SOURCE);
        event.setStatus(FAIL);
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT: {}", event.getId());
        addHistory(event, "Saga Finished with errors!");
        notifyFinishedSaga(event);
    }

    public void continueSaga(Event event) {
        var topic = getTopic(event);
        log.info("SAGA CONTINUED FOR EVENT: {}", event.getId());
        sendToProducerWithTopic(event, topic);
    }

    private Topics getTopic(Event event) {
        return sagaExecutionCoordinator.getNextTopic(event);
    }

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addHistory(history);
    }

    private void notifyFinishedSaga(Event event) {
        producer.sendEvent(jsonUtil.toJson(event), NOTIFY_ENDING.getTopic());
    }

    private void sendToProducerWithTopic(Event event, Topics topic) {
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());
    }
}

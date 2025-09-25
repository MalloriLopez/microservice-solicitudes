package co.com.bancolombia.model.messaging.reports.gateways;

import reactor.core.publisher.Mono;

public interface ReportsRepository {
    Mono<String> sendMessageReports(Double message);
}

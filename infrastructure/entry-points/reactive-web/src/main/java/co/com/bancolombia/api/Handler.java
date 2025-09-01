package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.LoanApplicationRequestDTO;
import co.com.bancolombia.api.mapper.LoanApplicationDTOMapper;
import co.com.bancolombia.usecase.LoanApplicationUseCase;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final RequestValidator requestValidator;
    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationDTOMapper loanApplicationDTOMapper;

    public Mono<ServerResponse> submitApplicationUseCase(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(LoanApplicationRequestDTO.class)
                .flatMap(requestValidator::validateLoanApplication)
                .map(loanApplicationDTOMapper::toModel)
                .flatMap(loanApplicationReq -> {
                    log.info("Solicitud recibida: {}", loanApplicationReq.toString());
                    return loanApplicationUseCase.submitApplication(loanApplicationReq)
                            .doOnSuccess(saved -> log.info("Solicitud guardada: {}", saved.toString()));
                })
                .flatMap(savedApplication -> ServerResponse.status(HttpResponseStatus.CREATED.code())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanApplicationDTOMapper.toResponse(savedApplication)));
    }
}

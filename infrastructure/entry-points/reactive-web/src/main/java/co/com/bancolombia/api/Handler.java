package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.LoanApplicationRequestDTO;
import co.com.bancolombia.api.dto.request.UpdateLoanApplicationReqDTO;
import co.com.bancolombia.api.dto.response.LoanApplicationListResponse;
import co.com.bancolombia.api.mapper.LoanApplicationDTOMapper;
import co.com.bancolombia.usecase.LoanApplicationListUseCase;
import co.com.bancolombia.usecase.LoanApplicationUseCase;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final RequestValidator requestValidator;
    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationDTOMapper loanApplicationDTOMapper;
    private final LoanApplicationListUseCase loanApplicationListUseCase;

    @PreAuthorize("hasRole('CLIENTE')")
    public Mono<ServerResponse> submitApplicationUseCase(ServerRequest serverRequest) {

        Mono<LoanApplicationRequestDTO> dtoMono = serverRequest.bodyToMono(LoanApplicationRequestDTO.class)
                .flatMap(requestValidator::validateLoanApplication);

        return Mono.zip(dtoMono, currentToken())
                .flatMap(tuple -> {
                    var dto  = tuple.getT1();
                    var auth = tuple.getT2();

                    // Claims del token
                    String tokenEmail  = auth.getToken().getClaimAsString("email");


                    boolean ok = false;
                    if (dto.email() != null && !dto.email().isBlank()) {
                        ok = dto.email().equalsIgnoreCase(tokenEmail);
                    }

                    if (!ok) {
                        return Mono.error(new AccessDeniedException("No puedes crear solicitudes para otro usuario"));
                    }

                    var model = loanApplicationDTOMapper.toModel(dto);
                    log.info("Solicitud recibida: {}", model);
                    return loanApplicationUseCase.submitApplication(model)
                            .doOnSuccess(saved -> log.info("Solicitud guardada: {}", saved));
                })
                .flatMap(saved ->
                        ServerResponse.status(HttpResponseStatus.CREATED.code())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loanApplicationDTOMapper.toResponse(saved))
                );
    }

    private Mono<JwtAuthenticationToken> currentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication());
    }

    @PreAuthorize("hasRole('ASESOR')")
    public Mono<ServerResponse> listLoanApplicationsUseCase(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        Long loanTypeId = request.queryParam("loanTypeId").map(Long::parseLong).orElse(null);
        Long status     = request.queryParam("status").map(Long::parseLong).orElse(null);

        log.info("GET /api/v1/solicitud page={}, size={}, loanTypeId={}, status={}", page, size, loanTypeId, status);

        return loanApplicationListUseCase
                .listLoanApplications(page, size, loanTypeId, status)
                .doOnSubscribe(s -> log.info("Inicio del listado de solicitudes"))
                .doOnNext(item -> log.debug("Elemento listado: email={}, tipo_prestamo={}, estado={}",
                        item.email(), item.loanTypeName(), item.statusName()))
                .map(item -> new LoanApplicationListResponse(
                        item.id(),
                        item.amount(),
                        item.termMonths(),
                        item.email(),
                        item.name(),
                        item.loanTypeName(),
                        item.interestRate(),
                        item.statusName(),
                        item.baseSalary(),
                        item.approvedMonthlyDebtTotal()
                ))
                .collectList()
                .flatMap(list -> {
                    log.info("Retornando {} items", list.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(list);
                })
                .doFinally(sig -> log.info("Listado de solicitudes finalizado: señal={}", sig));
    }


    public Mono<ServerResponse> updateApplicationUseCase(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(UpdateLoanApplicationReqDTO.class)
                .flatMap(requestValidator::validateLoanApplication)

                .map(dto -> {
                    var model = loanApplicationDTOMapper.toModel(dto);
                    model.setId(id);
                    return model;
                })
                .flatMap(loanApplicationUseCase::update)
                .doOnSuccess(updateLoanApp -> log.info("Solicitud actualizada: {}", updateLoanApp))
                .flatMap(updateLoanApp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanApplicationDTOMapper.toResponse(updateLoanApp)));
    }



}

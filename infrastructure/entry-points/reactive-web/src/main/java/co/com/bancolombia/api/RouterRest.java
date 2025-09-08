package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.LoanApplicationRequestDTO;
import co.com.bancolombia.api.dto.response.LoanApplicationListResponse;
import co.com.bancolombia.api.dto.response.LoanApplicationResponseDTO;
import co.com.bancolombia.model.loanapplication.LoanApplicationReviewItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "submitApplicationUseCase",
                    operation = @Operation(
                            operationId = "createLoanApplication",
                            summary = "Crea una solicitud de prestamo",
                            description = "Crea una solicitud de prestamo y devuelve su representación",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = LoanApplicationRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud de prestamo creado",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationResponseDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Petición inválida")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listLoanApplicationsUseCase",
                    operation = @Operation(
                            operationId = "listLoanApplications",
                            summary = "Lista las solicitudes de prestamo pendientes por revision",
                            description = "Lista solicitudes de prestamos y devuelve su representación",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = LoanApplicationReviewItem.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitudes de prestamos listadas",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationListResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Petición inválida")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route()
                .POST("/api/v1/solicitudes", handler::submitApplicationUseCase)
                .GET("/api/v1/solicitud", handler::listLoanApplicationsUseCase)
                .build();
    }
}

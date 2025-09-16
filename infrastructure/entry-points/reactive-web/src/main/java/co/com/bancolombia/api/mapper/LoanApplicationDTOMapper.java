package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.UpdateLoanApplicationReqDTO;
import org.mapstruct.Mapper;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.api.dto.request.LoanApplicationRequestDTO;
import co.com.bancolombia.api.dto.response.LoanApplicationResponseDTO;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface LoanApplicationDTOMapper {


    LoanApplicationResponseDTO toResponse(LoanApplication loanApplication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationStatusId", constant = "1L")
    LoanApplication toModel(LoanApplicationRequestDTO loanApplicationRequestDTO);

    LoanApplication toModel(UpdateLoanApplicationReqDTO updateLoanApplicationReqDTO);
}

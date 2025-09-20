package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

/**
 * MapStruct mapper for converting between domain Errors and ErrorResponse DTOs
 */
@Mapper(componentModel = JAKARTA_CDI)
public interface ErrorMapper {

    /**
     * Maps domain Error to ErrorDetail DTO
     */
    ErrorResponse.ErrorDetail toErrorDetail(Errors.Error error);

    /**
     * Maps list of domain Errors to list of ErrorDetail DTOs
     */
    List<ErrorResponse.ErrorDetail> toErrorDetailList(List<Errors.Error> errors);

    /**
     * Maps domain Errors to ErrorResponse DTO
     */
    default ErrorResponse toErrorResponse(Errors errors) {
        List<ErrorResponse.ErrorDetail> errorDetails = toErrorDetailList(errors.errors());
        return ErrorResponse.of(errorDetails, "Validation failed");
    }

    /**
     * Creates ErrorResponse for a single error
     */
    default ErrorResponse toErrorResponse(String field, String message, String code) {
        var error = Errors.Error.of(field, message, code);
        var errorDetail = toErrorDetail(error);
        return ErrorResponse.of(List.of(errorDetail), message);
    }
}



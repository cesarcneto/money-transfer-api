package io.cesarcneto.moneytransfer.shared.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorResponseDto {

    private String message;
    private Integer code;

}

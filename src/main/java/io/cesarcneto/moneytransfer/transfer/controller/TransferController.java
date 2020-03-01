package io.cesarcneto.moneytransfer.transfer.controller;

import io.cesarcneto.moneytransfer.transfer.dto.TransferInputDto;
import io.cesarcneto.moneytransfer.transfer.mapper.TransferMapper;
import io.cesarcneto.moneytransfer.transfer.service.TransferService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final TransferMapper transferMapper;

    public void performTransfer(Context ctx) {

        TransferInputDto transferInputDto = ctx.bodyValidator(TransferInputDto.class)
                .check(input -> nonNull(input.getFrom()), "'from' must be specified")
                .check(input -> nonNull(input.getTo()), "'to' must be specified")
                .check(input -> !Objects.equals(input.getFrom(), input.getTo()),
                        "'from' account and 'to' account cannot be the same")
                .check(input -> nonNull(input.getOccurredAt()), "'occurredAt' must be specified")
                .check(input -> nonNull(Instant.parse(input.getOccurredAt())),
                        "'occurredAt' must be specified in a ISO instant format")
                .check(input -> nonNull(input.getAmount()), "'amount' must be specified")
                .check(input -> input.getAmount().compareTo(BigDecimal.ZERO) > 0,
                        "'amount' must be bigger than 0")
                .get();

        transferService.performTransferRequest(
                transferMapper.transferInputDtoToTransferRequest(transferInputDto)
        );

        ctx.status(OK_200);
    }

}

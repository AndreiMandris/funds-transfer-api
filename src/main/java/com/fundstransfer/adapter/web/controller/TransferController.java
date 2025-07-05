package com.fundstransfer.adapter.web.controller;

import com.fundstransfer.adapter.web.dto.TransferDto;
import com.fundstransfer.adapter.web.dto.TransferRequestDto;
import com.fundstransfer.adapter.web.mapper.DtoMapper;
import com.fundstransfer.application.service.FundsTransferUseCase;
import com.fundstransfer.application.service.TransferService;
import com.fundstransfer.domain.model.exception.TransferNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final DtoMapper dtoMapper;

    @PostMapping
    public ResponseEntity<TransferDto> createTransfer(@Valid @RequestBody TransferRequestDto request) {
        var transferRequest = new FundsTransferUseCase.TransferRequest(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                request.description()
        );

        var result = transferService.executeTransfer(transferRequest);
        var response = dtoMapper.toDto(result.transfer());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferDto> getTransfer(@PathVariable String id) {
        var transfer = transferService.findById(id)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found: " + id));
        var response = dtoMapper.toDto(transfer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransferDto>> getTransfersByAccount(@PathVariable String accountId) {
        var transfers = transferService.findByFromAccountIdOrToAccountId(accountId, accountId);
        var response = transfers.stream()
                .map(dtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransferDto>> getAllTransfers() {
        var transfers = transferService.findAll();
        var response = transfers.stream()
                .map(dtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }
} 

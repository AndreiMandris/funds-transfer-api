package com.fundstransfer.adapter.web.controller;

import com.fundstransfer.adapter.web.dto.AccountDto;
import com.fundstransfer.adapter.web.dto.CreateAccountRequestDto;
import com.fundstransfer.adapter.web.mapper.DtoMapper;
import com.fundstransfer.application.service.AccountService;
import com.fundstransfer.domain.model.exception.AccountNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final DtoMapper dtoMapper;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequestDto request) {
        var account = dtoMapper.toDomain(request);
        var savedAccount = accountService.save(account);
        var response = dtoMapper.toDto(savedAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id) {
        var account = accountService.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        var response = dtoMapper.toDto(account);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<AccountDto>> getAccountsByOwner(@PathVariable Long ownerId) {
        var accounts = accountService.findByOwnerId(ownerId);
        var response = accounts.stream()
                .map(dtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        var accounts = accountService.findAll();
        var response = accounts.stream()
                .map(dtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        if (!accountService.existsById(id)) {
            throw new AccountNotFoundException("Account not found with id: " + id);
        }
        accountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 

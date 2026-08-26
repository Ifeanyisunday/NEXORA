package com.nexora.banking.statement.controller;

import com.nexora.banking.statement.dto.response.StatementResponse;
import com.nexora.banking.statement.pdf.PdfStatementServiceImpl;
import com.nexora.banking.statement.service.StatementService;
import com.nexora.banking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    private final PdfStatementServiceImpl pdfStatementServiceImpl;

    @GetMapping
    public StatementResponse generateStatement(

            @AuthenticationPrincipal
            User currentUser,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to

    ) {

        return statementService.generateStatement(
                currentUser,
                from,
                to
        );
    }


    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadStatement(

        @AuthenticationPrincipal
        User currentUser,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to

     ) {

        StatementResponse statement =
                statementService.generateStatement(
                        currentUser,
                        from,
                        to
                );

        byte[] pdf =
                pdfStatementServiceImpl.generatePdf(
                        statement
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=statement.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
      }
}
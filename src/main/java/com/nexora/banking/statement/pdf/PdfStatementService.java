package com.nexora.banking.statement.pdf;

import com.nexora.banking.statement.dto.response.StatementResponse;

public interface PdfStatementService {

    byte[] generatePdf(
            StatementResponse statement
    );

}
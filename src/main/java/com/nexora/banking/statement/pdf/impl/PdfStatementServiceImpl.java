package com.nexora.banking.statement.pdf.impl;

import com.nexora.banking.statement.dto.response.StatementItemResponse;
import com.nexora.banking.statement.dto.response.StatementResponse;
import com.nexora.banking.statement.pdf.PdfStatementService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfStatementServiceImpl
        implements PdfStatementService {

    @Override
    @SneakyThrows
    public byte[] generatePdf(
            StatementResponse statement
    ) {

        try (

                PDDocument document = new PDDocument();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()

        ) {

            PDPage page = new PDPage();

            document.addPage(page);

            try (

                    PDPageContentStream contentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            )

            ) {

                contentStream.beginText();

                contentStream.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        ),
                        18
                );

                contentStream.newLineAtOffset(
                        50,
                        750
                );

                contentStream.showText(
                        "NEXORA BANK"
                );

                contentStream.endText();

                float yPosition = 710;

                contentStream.beginText();

                contentStream.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        ),
                        12
                );

                contentStream.newLineAtOffset(
                        50,
                        yPosition
                );

                contentStream.showText(
                        "Statement Reference: "
                                + statement.statementReference()
                );

                contentStream.newLineAtOffset(
                        0,
                        -20
                );

                contentStream.showText(
                        "Generated At: "
                                + statement.generatedAt()
                );

                contentStream.newLineAtOffset(
                        0,
                        -20
                );

                contentStream.showText(
                        "Opening Balance: "
                                + statement.openingBalance()
                );

                contentStream.newLineAtOffset(
                        0,
                        -20
                );

                contentStream.showText(
                        "Closing Balance: "
                                + statement.closingBalance()
                );

                contentStream.newLineAtOffset(
                        0,
                        -20
                );

                contentStream.showText(
                        "Total Credits: "
                                + statement.totalCredits()
                );

                contentStream.newLineAtOffset(
                        0,
                        -20
                );

                contentStream.showText(
                        "Total Debits: "
                                + statement.totalDebits()
                );

                contentStream.endText();

                float transactionYPosition = 560;

                contentStream.beginText();

                contentStream.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        ),
                        12
                );

                contentStream.newLineAtOffset(
                        50,
                        transactionYPosition
                );

                contentStream.showText(
                        "TRANSACTIONS"
                );

                contentStream.endText();

                transactionYPosition -= 30;

                for (StatementItemResponse item : statement.transactions()) {

                        contentStream.beginText();

                        contentStream.setFont(
                                new PDType1Font(
                                        Standard14Fonts.FontName.HELVETICA
                                ),
                                10
                        );

                        contentStream.newLineAtOffset(
                                50,
                                transactionYPosition
                        );

                        contentStream.showText(
                                item.date()
                                        + " | "
                                        + item.reference()
                                        + " | "
                                        + item.debit()
                                        + " | "
                                        + item.credit()
                                        + " | "
                                        + item.balance()
                        );

                        contentStream.endText();

                        transactionYPosition -= 20;
                }
            }

            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }
}
package com.nexora.banking.statement.pdf;

import com.nexora.banking.statement.exception.StatementPdfGenerationException;
import com.nexora.banking.statement.dto.response.StatementItemResponse;
import com.nexora.banking.statement.dto.response.StatementResponse;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfStatementServiceImpl
        implements PdfStatementService {

    private static final float PAGE_WIDTH = 595;
    private static final float PAGE_HEIGHT = 842;

    private static final float LEFT_MARGIN = 50;
    private static final float TOP_MARGIN = 750;
    private static final float BOTTOM_MARGIN = 50;

    private static final float TRANSACTION_ROW_HEIGHT = 20;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern("dd MMM yyyy HH:mm")
                    .withZone(ZoneOffset.UTC);

    @Override
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

            PDPageContentStream contentStream =
                    new PDPageContentStream(
                            document,
                            page
                    );

            try {

                writeStatementHeader(
                        contentStream,
                        statement
                );

                float transactionYPosition =
                        writeTransactionSectionHeader(
                                contentStream
                        );

                for (
                        StatementItemResponse item
                        : statement.transactions()
                ) {

                    if (
                            transactionYPosition
                                    < BOTTOM_MARGIN
                    ) {

                        contentStream.close();

                        page = new PDPage();

                        document.addPage(page);

                        contentStream =
                                new PDPageContentStream(
                                        document,
                                        page
                                );

                        transactionYPosition =
                                writeTransactionSectionHeader(
                                        contentStream
                                );
                    }

                    writeTransactionRow(
                            contentStream,
                            item,
                            transactionYPosition
                    );

                    transactionYPosition -=
                            TRANSACTION_ROW_HEIGHT;
                }

            } finally {

                contentStream.close();
            }

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new StatementPdfGenerationException(
                    "Failed to generate statement PDF.",
                    exception
            );
        }
    }

    private void writeStatementHeader(
            PDPageContentStream contentStream,
            StatementResponse statement
    ) throws IOException {

        writeText(
                contentStream,
                "NEXORA BANK",
                LEFT_MARGIN,
                TOP_MARGIN,
                18,
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                )
        );

        float yPosition = 710;

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        writeText(
                contentStream,
                "Statement Reference: "
                        + statement.statementReference(),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );

        yPosition -= 20;

        writeText(
                contentStream,
                "Generated At: "
                        + formatDate(statement.generatedAt()),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );

        yPosition -= 20;

        writeText(
                contentStream,
                "Opening Balance: "
                        + formatAmount(
                                statement.openingBalance()
                        ),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );

        yPosition -= 20;

        writeText(
                contentStream,
                "Closing Balance: "
                        + formatAmount(
                                statement.closingBalance()
                        ),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );

        yPosition -= 20;

        writeText(
                contentStream,
                "Total Credits: "
                        + formatAmount(
                                statement.totalCredits()
                        ),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );

        yPosition -= 20;

        writeText(
                contentStream,
                "Total Debits: "
                        + formatAmount(
                                statement.totalDebits()
                        ),
                LEFT_MARGIN,
                yPosition,
                12,
                font
        );
    }

    private float writeTransactionSectionHeader(
            PDPageContentStream contentStream
    ) throws IOException {

        float yPosition = 560;

        writeText(
                contentStream,
                "TRANSACTIONS",
                LEFT_MARGIN,
                yPosition,
                12,
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                )
        );

        yPosition -= 30;

        writeTransactionHeader(
                contentStream,
                yPosition
        );

        return yPosition - 20;
    }

    private void writeTransactionHeader(
            PDPageContentStream contentStream,
            float y
    ) throws IOException {

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        writeText(
                contentStream,
                "DATE",
                50,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                "REFERENCE",
                125,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                "DESCRIPTION",
                235,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                "DEBIT",
                390,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                "CREDIT",
                450,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                "BALANCE",
                510,
                y,
                8,
                font
        );
    }

    private void writeTransactionRow(
            PDPageContentStream contentStream,
            StatementItemResponse item,
            float y
    ) throws IOException {

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        writeText(
                contentStream,
                formatDate(item.date()),
                50,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                truncate(
                     item.reference(),
                     15
                ),
                125,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                truncate(
                        item.description(),
                        24
                ),
                235,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                formatAmount(item.debit()),
                390,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                formatAmount(item.credit()),
                450,
                y,
                8,
                font
        );

        writeText(
                contentStream,
                formatAmount(item.balance()),
                510,
                y,
                8,
                font
        );
    }

    private void writeText(
            PDPageContentStream contentStream,
            String text,
            float x,
            float y,
            float fontSize,
            PDType1Font font
    ) throws IOException {

        contentStream.beginText();

        contentStream.setFont(
                font,
                fontSize
        );

        contentStream.newLineAtOffset(
                x,
                y
        );

        contentStream.showText(
                text
        );

        contentStream.endText();
    }

    private String formatAmount(
            BigDecimal amount
    ) {

        if (
                amount == null
                        || amount.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {
            return "-";
        }

        return String.format(
                "%,.2f",
                amount
        );
    }

    private String formatDate(
            Instant instant
    ) {

        if (instant == null) {
            return "-";
        }

        return DATE_FORMATTER.format(
                instant
        );
    }

    private String truncate(
            String text,
            int maxLength
    ) {

        if (
                text == null
                        || text.isBlank()
        ) {
            return "-";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(
                        0,
                        maxLength - 3
                )
                + "...";
    }
}
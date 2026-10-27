package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.factory.TransactionFactory;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.exceptions.WalletNotFoundException;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FundingConfirmationServiceImpl
        implements FundingConfirmationService {

    private final FundingRepository fundingRepository;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Override
    @Transactional
    public FundingResponse confirmFunding(
            String fundingReference,
            FundingProvider provider,
            String providerReference,
            boolean successful
    ) {
        // 1. Lock the funding record to serialize competing callbacks.
        Funding funding = fundingRepository
                .findByReferenceForUpdate(fundingReference)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funding not found."
                        )
                );

        // 2. Validate the provider and its reference before accepting
        // an idempotent retry.
        if (funding.getProvider() != provider) {
            throw new IllegalArgumentException(
                    "Funding does not belong to this provider."
            );
        }

        if (providerReference == null
                || providerReference.isBlank()
                || !providerReference.equals(
                        funding.getProviderReference()
                )) {
            throw new IllegalArgumentException(
                    "Provider reference does not match."
            );
        }

        // 3. A valid duplicate success callback must not credit twice.
        if (funding.getStatus() == FundingStatus.COMPLETED) {
            if (!successful) {
                throw new IllegalStateException(
                        "Completed funding cannot be marked failed."
                );
            }
            
            return FundingResponse.from(funding);
        }

        // 4. Only processing funding can receive a final outcome.
        if (funding.getStatus() != FundingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Only processing funding can be confirmed."
            );
        }

        // 5. A provider failure must never credit the wallet.
        if (!successful) {
            funding.markFailed();
            return FundingResponse.from(funding);
        }

        // 6. Lock the wallet before changing its balance.
        Wallet wallet = walletRepository
                .findByIdForUpdate(funding.getWallet().getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Wallet is not active."
            );
        }

        // 7. Update the balance and record the exact balance movement.
        BigDecimal balanceBefore = wallet.getBalance();

        wallet.deposit(funding.getAmount());

        BigDecimal balanceAfter = wallet.getBalance();

        // 8. Link the ledger transaction to the funding record.
        Transaction transaction = TransactionFactory.createForFunding(
                wallet,
                funding,
                funding.getAmount(),
                balanceBefore,
                balanceAfter,
                "Wallet funding: " + funding.getReference()
        );

        // 9. Save the ledger transaction and complete the funding
        // inside the same database transaction.
        transactionService.save(transaction);

        funding.markCompleted(Instant.now());

        return FundingResponse.from(funding);
    }
}
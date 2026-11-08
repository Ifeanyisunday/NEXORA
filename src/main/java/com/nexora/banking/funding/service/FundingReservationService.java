package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.exception.FundingIdempotencyKeyConflictException;
import com.nexora.banking.funding.factory.FundingFactory;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingReservationService {

    private final FundingRepository fundingRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Reservation reserve(
            User currentUser,
            CreateFundingRequest request,
            String idempotencyKey
    ) {

        var existingFunding = fundingRepository
                .findByUserAndIdempotencyKey(
                        currentUser,
                        idempotencyKey
                );

        if (existingFunding.isPresent()) {
            Funding funding = existingFunding.get();

            validateSameRequest(funding, request);

            return new Reservation(funding, false);
        }

        var wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new IllegalStateException("Wallet not found.")
                );

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active.");
        }

        Funding funding = FundingFactory.create(
                currentUser,
                wallet,
                request.amount(),
                request.method(),
                FundingProvider.MOCK,
                idempotencyKey
        );

        funding = fundingRepository.saveAndFlush(funding);

        return new Reservation(funding, true);
    }

    public void validateSameRequest(
            Funding existingFunding,
            CreateFundingRequest request
    ) {

        boolean sameAmount = existingFunding.getAmount()
                .compareTo(request.amount()) == 0;

        boolean sameMethod = existingFunding.getMethod()
                == request.method();

        if (!sameAmount || !sameMethod) {
            throw new FundingIdempotencyKeyConflictException(
                    "This idempotency key has already been used "
                            + "with a different funding request."
            );
        }
    }

    public record Reservation(
            Funding funding,
            boolean newlyCreated
    ) {
    }
}
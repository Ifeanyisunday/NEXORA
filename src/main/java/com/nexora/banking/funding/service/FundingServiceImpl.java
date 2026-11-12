package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.exception.FundingIdempotencyKeyConflictException;
import com.nexora.banking.funding.provider.FundingProviderClient;
import com.nexora.banking.funding.provider.FundingProviderRegistry;
import com.nexora.banking.funding.provider.FundingProviderResult;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingServiceImpl implements FundingService {

    private final FundingRepository fundingRepository;
    private final FundingReservationService fundingReservationService;
    private final FundingProviderRegistry fundingProviderRegistry;

    @Override
    public FundingResponse initiateFunding(
            User currentUser,
            CreateFundingRequest request,
            String idempotencyKey
    ) {

        FundingReservationService.Reservation reservation;

        try {
            reservation = fundingReservationService.reserve(
                    currentUser,
                    request,
                    idempotencyKey
            );

        } catch (DataIntegrityViolationException exception) {

            /*
             * Another concurrent request may have created
             * the same user's funding reservation first.
             *
             * The reservation transaction has rolled back
             * before execution reaches this catch block.
             */

            Funding existingFunding = fundingRepository
                    .findByUserAndIdempotencyKey(
                            currentUser,
                            idempotencyKey
                    )
                    .orElseThrow(() -> exception);

            fundingReservationService.validateSameRequest(
                    existingFunding,
                    request
            );

            return FundingResponse.from(existingFunding);
        }

        Funding funding = reservation.funding();

        /*
         * Existing idempotency key:
         * Return the existing funding record.
         * Do not call the payment provider again.
         */
        if (!reservation.newlyCreated()) {
            return FundingResponse.from(funding);
        }

        /*
         * Select the provider associated with this funding.
         */
        FundingProviderClient providerClient =
                fundingProviderRegistry.getClient(funding.getProvider());

        FundingProviderResult result =
                providerClient.initiate(funding);

        if (!result.successful()) {
            funding.markFailed();

            return FundingResponse.from(
                    fundingRepository.save(funding)
            );
        }

        funding.markProcessing(
            result.providerReference(),
            result.authorizationUrl()
        );

        return FundingResponse.from(
                fundingRepository.save(funding)
        );
    }
}
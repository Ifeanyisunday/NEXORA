package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.factory.FundingFactory;
import com.nexora.banking.funding.provider.FundingProviderClient;
import com.nexora.banking.funding.provider.FundingProviderResult;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingServiceImpl implements FundingService {

    private final FundingRepository fundingRepository;
    private final WalletRepository walletRepository;
    private final FundingProviderClient fundingProviderClient;

    @Override
    public FundingResponse initiateFunding(
            User currentUser,
            CreateFundingRequest request
    ) {
        var wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Wallet not found."
                        )
                );

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Wallet is not active."
            );
        }

        Funding funding = FundingFactory.create(
                currentUser,
                wallet,
                request.amount(),
                request.method(),
                FundingProvider.MOCK
        );

        funding = fundingRepository.save(funding);

        FundingProviderResult result =
                fundingProviderClient.initiate(funding);

        if (!result.successful()) {
            funding.markFailed();

            return FundingResponse.from(
                    fundingRepository.save(funding)
            );
        }

        funding.markProcessing(result.providerReference());

        return FundingResponse.from(
                fundingRepository.save(funding)
        );
    }
}
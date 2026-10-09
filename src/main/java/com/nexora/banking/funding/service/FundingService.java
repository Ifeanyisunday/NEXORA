package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.user.entity.User;

public interface FundingService {

    FundingResponse initiateFunding(
            User currentUser,
            CreateFundingRequest request
    );
}
package com.nexora.banking.statement.service;

import com.nexora.banking.statement.dto.response.StatementResponse;
import com.nexora.banking.user.entity.User;

import java.time.LocalDate;

public interface StatementService {

    StatementResponse generateStatement(
            User user,
            LocalDate from,
            LocalDate to
    );

}
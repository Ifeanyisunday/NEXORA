package com.nexora.banking.funding.entity;

import com.nexora.banking.common.entity.BaseEntity;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "fundings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_funding_reference",
                        columnNames = "reference"
                ),
                @UniqueConstraint(
                        name = "uk_funding_provider_reference",
                        columnNames = {
                                "provider",
                                "provider_reference"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_funding_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_funding_wallet_created",
                        columnList = "wallet_id, created_at"
                ),
                @Index(
                        name = "idx_funding_status",
                        columnList = "status"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wallet_id",
            nullable = false
    )
    private Wallet wallet;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 3
    )
    private Currency currency;

    @Column(
            nullable = false,
            length = 100
    )
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private FundingStatus status;

    @Enumerated(EnumType.STRING)
	@Column(
			nullable = false,
		length = 30
	)
	private FundingMethod method;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private FundingProvider provider;

    @Column(
            name = "provider_reference",
            length = 150
    )
    private String providerReference;

    @Column(
            name = "completed_at"
    )
    private Instant completedAt;



    public void markProcessing(String providerReference) {
    if (status != FundingStatus.PENDING) {
        throw new IllegalStateException(
                "Only pending funding can enter processing."
        );
    }

    if (providerReference == null || providerReference.isBlank()) {
        throw new IllegalArgumentException(
                "Provider reference is required."
        );
    }
		this.providerReference = providerReference;
		this.status = FundingStatus.PROCESSING;
	}

	public void markFailed() {
		if (status != FundingStatus.PENDING
				&& status != FundingStatus.PROCESSING) {
			throw new IllegalStateException(
					"Only pending or processing funding can fail."
			);
		}
		this.status = FundingStatus.FAILED;
	}
}
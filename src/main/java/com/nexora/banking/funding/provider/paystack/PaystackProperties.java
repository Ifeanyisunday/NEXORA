
package com.nexora.banking.funding.provider.paystack;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "paystack")
public class PaystackProperties {

    private boolean enabled = false;

    private String baseUrl = "https://api.paystack.co";

    private String secretKey;

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration readTimeout = Duration.ofSeconds(10);
}

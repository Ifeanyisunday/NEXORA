
package com.nexora.banking.funding.provider.paystack;

import java.net.http.HttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(PaystackProperties.class)
@ConditionalOnProperty(
        prefix = "paystack",
        name = "enabled",
        havingValue = "true"
)
public class PaystackConfiguration {

    @Bean
    public RestClient paystackRestClient(
            PaystackProperties properties
    ) {

        if (properties.getSecretKey() == null
                || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException(
                    "PAYSTACK_SECRET_KEY must be configured when Paystack is enabled."
            );
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getSecretKey()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}

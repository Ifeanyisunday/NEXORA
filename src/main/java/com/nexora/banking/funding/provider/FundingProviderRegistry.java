package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.enums.FundingProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FundingProviderRegistry {

    private final Map<FundingProvider, FundingProviderClient> clients;

    public FundingProviderRegistry(
            List<FundingProviderClient> providerClients
    ) {

        Map<FundingProvider, FundingProviderClient> registeredClients =
                new EnumMap<>(FundingProvider.class);

        for (FundingProviderClient client : providerClients) {

            FundingProvider provider = client.provider();

            FundingProviderClient existingClient =
                    registeredClients.putIfAbsent(provider, client);

            if (existingClient != null) {
                throw new IllegalStateException(
                        "Multiple funding clients are registered for provider: "
                                + provider
                );
            }
        }

        this.clients = Map.copyOf(registeredClients);
    }

    public FundingProviderClient getClient(FundingProvider provider) {

        FundingProviderClient client = clients.get(provider);

        if (client == null) {
            throw new IllegalStateException(
                    "No funding provider is configured for: " + provider
            );
        }

        return client;
    }
}
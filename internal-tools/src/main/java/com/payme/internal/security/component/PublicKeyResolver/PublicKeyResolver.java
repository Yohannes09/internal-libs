package com.payme.internal.security.component.PublicKeyResolver;

import com.payme.internal.exception.WebClientException;
import com.payme.internal.security.dto.PublicKeyResponseDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * Resolves and maintains the current public key metadata from an external token service.
 *
 * <p>This component periodically fetches the active and previous public keys used for
 * verifying JWTs from a configurable endpoint. It is designed to be stateless and
 * thread-safe, exposing the current key state to consumers while abstracting away
 * all communication and response handling.</p>
 *
 * <p>Fetching occurs every 10 minutes via a scheduled task, ensuring timely updates
 * in line with key rotation policies (e.g., every 30 minutes). If a fetch fails,
 * the most recently valid key is retained to maintain continuity.</p>
 *
 * <p>This class is intentionally silent on logging and error reporting, leaving those
 * concerns to external observability tools or infrastructure layers.</p>
 *
 * <p>Intended for use across microservices to eliminate boilerplate key handling logic.</p>
 *
 * @see TokenWebClientProperties
 * @see PublicKeyResponseDto
 */
@Component
@RequiredArgsConstructor
public class PublicKeyResolver {
    private final WebClient webClient;
    private final TokenWebClientProperties tokenWebClientProperties;

    private volatile PublicKeyContainer publicKeyContainer;

    public Optional<PublicKeyMetaData> getPublicKeyMetaData(){
        return Optional.ofNullable(publicKeyContainer)
                .map(container -> PublicKeyMetaData.builder()
                        .currentPublicKey(container.currentPublicKey())
                        .previousPublicKey(container.previousPublicKey())
                        .signingAlgorithm(container.signingAlgorithm())
                        .tokenIssuer(container.tokenIssuer())
                        .build()
                );

    }

    @Scheduled(cron = "0 0/10 * * * ?")
    private void fetchActivePublicKey(){
        String uri = tokenWebClientProperties.getPublicKeyUri();

        if(uri == null || uri.isBlank()){
            throw new IllegalStateException("Public Key URI not configured. ");
        }

        PublicKeyResponseDto response = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(
                        httpStatusCode ->
                                httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),

                        clientResponse ->
                                clientResponse
                                        .bodyToMono(String.class)
                                        .map(body -> new WebClientException(body + "."))
                ).bodyToMono(PublicKeyResponseDto.class)
                .block();

        // Avoids breaking downstream consumers.
        if(response != null){
            this.publicKeyContainer = mapResponseToContainer(response);
        }

    }

    private PublicKeyContainer mapResponseToContainer(PublicKeyResponseDto responseDto){
        return PublicKeyContainer.builder()
                .currentPublicKey(responseDto.currentPublicKey())
                .previousPublicKey(responseDto.previousPublicKey())
                .signingAlgorithm(responseDto.signatureAlgorithm().name())
                .tokenIssuer(responseDto.tokenIssuer())
                .build();
    }

    @Builder
    private record PublicKeyContainer(
            String currentPublicKey,
            String previousPublicKey,
            String signingAlgorithm,
            String tokenIssuer
    ){}

}

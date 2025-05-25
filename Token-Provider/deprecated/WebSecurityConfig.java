package com.payme.token.deprecated;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration

@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final List<String> WHITE_LISTED_ADDRESSES = List.of(
            "",
            ""
    );

    @Bean
    @Profile("prod")
    public SecurityFilterChain enabledWebSecurity(
            HttpSecurity http,
            AuthenticationFilterConfig authenticationFilterConfig
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/api/v1/public-key").permitAll()
                                // Whitelist of IPS
                                .anyRequest().access((authentication, context) -> {
                                    String incomingRequestIPAddress = context.getRequest().getRemoteAddr();

                                    if(isWhiteListedAddress(incomingRequestIPAddress)){
                                        return new AuthorizationDecision(true);
                                    }
                                    return new AuthorizationDecision(
                                            authentication.get().isAuthenticated()
                                    );
                                })
                )
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authenticationFilterConfig, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain disabledWebSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    private boolean isWhiteListedAddress(String ipAddress){
        return WHITE_LISTED_ADDRESSES.stream()
                .anyMatch(address -> address.equals(ipAddress));
    }

}

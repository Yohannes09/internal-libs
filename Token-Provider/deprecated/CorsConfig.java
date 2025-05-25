package com.payme.token.deprecated;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {

    // save for gateway. Dont forget to add cors in WebSecurityConfig
//    @Bean
//    public WebMvcConfigurer corsConfig(){
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                // Remember to fix this so it's not hardcoded.
//                registry.addMapping("api/**")
//                        .allowedOrigins("http://localhost:8081")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("*")
//                        .allowCredentials(true); // Indicates whether user credentials (like cookies or auth headers) are allowed in CORS requests
//            }
//        };
//    }
}

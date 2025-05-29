//package com.payme.token.management;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//
//// Manually fail a property to check if it fails.
//@SpringBootTest
//@TestPropertySource(
//        properties = {
//                // Signing configuration
//                "token.signing.algorithm=RSA",  // not null or empty, must be one of: RSA, EC
//                "token.signing.key-size=2048",  // not null, min: 2048, max: 5000
//                "token.signing.key-id=payme.auth.id",  // not null or empty
//                "token.signing.rotation-interval-minutes=60",  // min: 1, max: 1440 (1 day)
//
//                // Encoding configuration
//                "token.encoding.type=JWT",  // not null or empty, must be one of: JWT, JWE
//                "token.encoding.compress=false",  // boolean
//
//                // Validation configuration
//                "token.validation.clock-skew-seconds=60",  // min: 0, max: 300 (e.g., 5 min max skew)
//
//                // Default claims
//                "token.default-claims.audience=payme.internal",  // not null or empty, valid URI or string
//                "token.default-claims.issuer=auth.payme.internal",  // not null or empty, valid URI or string
//
//                // Token templates (validity minutes)
//                "token.templates.short-lived.validity-minutes=15",  // min: 1, max: 60
//                "token.templates.standard.validity-minutes=60",  // min: 1, max: 1440
//                "token.templates.long-lived.validity-minutes=120",  // min: 1, max: 10080 (7 days)
//
//                // User profile token template bindings
//                "token.profiles.user.access-token.template=short-lived",  // must match template name
//                "token.profiles.user.refresh-token.template=standard",  // must match template name
//
//                // Service profile token template bindings
//                "token.profiles.service.access-token.template=short-lived",  // must match template name
//                "token.profiles.service.refresh-token.template=standard",  // must match template name
//                "token.profiles.service.initialization-token.template=long-lived"  // must match template name
//
//        }
//)
//public class TokenConfigPropertiesTests {
//        @Test
//        void failStartingApplication(){
//
//        }
//}

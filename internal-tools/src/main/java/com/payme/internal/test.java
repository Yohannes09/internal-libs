package com.payme.internal;

import com.payme.internal.security.token.TokenResolver;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class test {
    static String token = "eyJraWQiOiIxZDg5NzNiNC1hOGM3LTQ2MjgtYmI0Yi00MmZjNzJkZmEyM2QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJhbGwtc2VydmljZXMiLCJyZWMiOiJTRVJWSUNFIiwidHlwIjoiSU5JVElBTElaQVRJT04iLCJyb2wiOlsiU0VSVklDRSJdLCJzdWIiOiJJTklUSUFMSVpBVElPTl9TVUJKRUNUIiwiaWF0IjoxNzQ2MjMwMjc5LCJleHAiOjE3NDYzMTY2Nzl9.L1Fg_09Zn4bhBMdGtN-ArOlCcQBVdgYFbxnKqZ3lRIvPUbl92qDh656OvSFsehHTlBdTS_rlLd0zDv9NdmY39dh3oVZlLwNARvbh_p4mZkLH81yEwpY7JPgi-wMz6I1UYGThBTsyvzXJoBZVyPRZrSLuh4MoV2wEHyaiaohr39elZms1Kb6ABnJkTqMoZGJuzu-kqM_s_gFK7hRe-hj7zFx6v4HC3zNHD1WZoIOB9f0Xr2Mcn3GmoDJ1HYeAG_9m9dJkY4beb2YBkbbbKdOL_RbwR9b-z_FXnWa1wN8sTDiHshF3cbqjyE6uSUvu2Rxk9jWjWZC_YoEto17J5du2Hg";
    static String signingKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqD5LaDuy8sU87Kbv+4Zo6X3hTt5ovc2w/d6S5NO/nHqoI7OJ106X+38bloWy8+gazWp/H103nFFLe7Dc7INCqUYkIJG57Vu4RJfAUWjygHSzjqZQ3coQxwZYYOb23YGDISuMm20ihbTvE3Kf0vdhbW0LgKmlJ2sBsDjxu0CUy5hP0Fx7MabSQo6fv3hXffR5V0D7xiAOAUK0eQhw19a40bxErO0Mg4DUKGN2OWF0TAmpVoCFKINWpIqPxpBOepr6rIOggffZFmcMDYUD/oVkDk77jtwGqEWoq9kncGGWMPp5C699aMrjYxQT2ZpxIJJFByIyJlfP2r/maVIEa2elwQIDAQAB";

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Optional<String> claim = TokenResolver.resolveClaim(
                token,
                signingKey,
                "RSA",
                claims -> claims.get("typ", String.class)
        );

        System.out.println(claim.get());
    }
}

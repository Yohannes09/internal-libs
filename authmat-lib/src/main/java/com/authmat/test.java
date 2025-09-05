//package com.authmat;
//
//import com.authmat.validation.TokenResolver;
//
//import java.security.Key;
//import java.security.NoSuchAlgorithmException;
//import java.security.spec.InvalidKeySpecException;
//import java.util.Map;
//
//public class test {
//    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        TokenResolver tokenResolver = new TokenResolver();
//        String token = "eyJraWQiOiJlOWNlNjdhNi1hZmI5LTQ2YjQtYjBkNi04MTEwMzc5MTgyYTEiLCJ0eXBlIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJhdXRobWF0Iiwic3ViIjoiMTE5NTcxMDUiLCJhdWQiOlsiZG9ja2VlcCIsImF1dGhtYXQiXSwidHlwZSI6IkFDQ0VTUyIsInNjb3BlIjpbXSwianRpIjoiYTlhMWNmNTktZGU5Ni00ZmNhLTk3NjQtMDJlOWZjYWZiY2Y1IiwiaWF0IjoxNzU2NjM5MjU2LCJleHAiOjE3NTY2NDEwNTZ9.IZ-pmjOKRFMat7f15BIeXiEGYEUIyqI8ct4dLRuT9iBYmz4ZnFjIbkfkIWzyjeupXHAN1O-EqjocKuZRFwt5VWnL5nZVGwIJTpEpuO8SOXnmoH1GwT3V8dPpx99VT6HcD2ahDxrdNCtvWU29Vpq1_-qPdpNwg1iia7W3Vj6rZ9fl6jNOF746DxnQmUa5ulj-JcAMPw5ibrefXA13xjJvNv2NYP6UOkndk_9A9D3I_I_8Ygnmxe5YrBusmc-EArBYdjQ8pvMhfEHFTpFKz_73iCnaUep0NOoBNtmuC_JWVJEBkM7NOFv5DlXW_RWth9xeZtz1qZM9MlVZafOPJUA2Qg";
//        String pk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArZce84+vVHBrIXLLyCGP+Tw2W+hEBgAaxr3FwP6QhbWczaysyIRTHszguyQIVgBE5BdST1jIhQMolJB+hYn8//gWziz05dOOF1MhPezUfl65CW+iaAESamFE3Exsh5dsn44IbJGMox8mgcnv6WMOtnSltAMe77I23GoGB5SPRPfCHuWqn8g5gxEjKH7/JSXwFqr1T+kCQhTXEWpHinUW0SF8Gj88v0i8zRyameBETZ8fQBuUlMs9XwhWT46T9alnMtvurn9Av89LENqinSx8x1bP4KuU4BoifGMFLlbA8pHkHKOKRnvFCM3HVsleL6DVvvHI3/MO1zNYCdfl65ANxwIDAQAB";
//
//        Key key = tokenResolver.loadPublicKey(pk, "RSA");
//        String kid = tokenResolver.resolveHeader(token, key).getKeyId();
//        String sub = tokenResolver.extractSubject(token, key);
////        System.out.println(kid);
////        System.out.println(sub);
////        System.out.println(tokenResolver.resolveHeader(token, key).getAlgorithm());
//        Map<String,Object> keyId = tokenResolver.extractHeader(token);
//        keyId.forEach((s, object) -> System.out.println(s + " " + object));
//
//    }
//}

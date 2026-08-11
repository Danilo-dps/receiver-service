package br.com.danilodps.receiver.domain;

public record VerificationResult(boolean valid, String version, String error) {

    public static VerificationResult ok(String v) {
        return new VerificationResult(true, v, null);
    }

    public static VerificationResult fail(String e) {
        return new VerificationResult(false, null, e);
    }

}

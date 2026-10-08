package com.digital.signature.exception;

import lombok.Getter;

@Getter
public class SignerException extends RuntimeException {

    private final String code;

    public SignerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SignerException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}

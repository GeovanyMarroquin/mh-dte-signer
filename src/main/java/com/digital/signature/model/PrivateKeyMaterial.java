package com.digital.signature.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Material de llave privada extraido del certificado MH (.crt).
 */
@Getter
@Setter
public class PrivateKeyMaterial {

    private String algorithm;
    @JsonProperty("clave")
    private String passwordHash;
    private String embodied;
    private String format;
    private String keyType;

    public PrivateKeyMaterial() {
    }

    public PrivateKeyMaterial(String algorithm, String passwordHash, String embodied, String format, String keyType) {
        this.algorithm = algorithm;
        this.passwordHash = passwordHash;
        this.embodied = embodied;
        this.format = format;
        this.keyType = keyType;
    }
}

package com.digital.signature.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Certificado MH loaded from ${CERTIFICATE_DIRECTORY}/{nit}.crt.
 * Solo se modela lo funcional (nit + privateKey); el resto del XML se ignora.
 */
@Getter
@Setter
public class Certificate {

    @JsonProperty("nit")
    private String taxpayerId;
    private PrivateKeyMaterial privateKey;

    public Certificate() {
    }

    public Certificate(String taxpayerId, PrivateKeyMaterial privateKey) {
        this.taxpayerId = taxpayerId;
        this.privateKey = privateKey;
    }
}

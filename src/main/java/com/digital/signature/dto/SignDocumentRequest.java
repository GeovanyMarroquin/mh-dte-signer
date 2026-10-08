package com.digital.signature.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignDocumentRequest {
    @JsonProperty("nit")
    private String taxpayerId;

    @JsonProperty("activo")
    private Boolean active;

    @JsonProperty("passwordPri")
    private String privateKeyPassword;

    @JsonProperty("dteJson")
    private JsonNode documentJson;
}

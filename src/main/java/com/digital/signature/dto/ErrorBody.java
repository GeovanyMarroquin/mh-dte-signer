package com.digital.signature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorBody {

    @JsonProperty("codigo")
    private String code;

    @JsonProperty("mensaje")
    private Object message;

    public ErrorBody(String code, Object message) {
        this.code = code;
        this.message = message;
    }
}

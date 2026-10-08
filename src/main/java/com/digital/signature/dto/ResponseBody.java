package com.digital.signature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseBody {

    @JsonProperty("status")
    private String status;

    @JsonProperty("body")
    private Object body;

    private ResponseBody(String status, Object body) {
        this.status = status;
        this.body = body;
    }

    public static ResponseBody ok(Object body) {
        return new ResponseBody("OK", body);
    }

    public static ResponseBody error(String code, Object message) {
        return new ResponseBody("ERROR", new ErrorBody(code, List.of(message)));
    }
}

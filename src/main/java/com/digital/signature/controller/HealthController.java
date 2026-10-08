package com.digital.signature.controller;

import com.digital.signature.dto.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/health", "/health/"})
    public ResponseEntity<ResponseBody> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "firmador-dte");
        body.put("fecha", Instant.now().toString());
        return ResponseEntity.ok(ResponseBody.ok(body));
    }
}

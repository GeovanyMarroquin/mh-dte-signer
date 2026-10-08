package com.digital.signature.controller;

import com.digital.signature.dto.ResponseBody;
import com.digital.signature.dto.SignDocumentRequest;
import com.digital.signature.exception.SignerException;
import com.digital.signature.service.SignerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignerController {

    private final SignerService signerService;

    @PostMapping({"/firmardocumento", "/firmardocumento/"})
    public ResponseEntity<ResponseBody> sign(@RequestBody SignDocumentRequest request) {
        try {
            String jws = signerService.sign(request.getTaxpayerId(), request.getPrivateKeyPassword(), request.getDocumentJson());
            return ResponseEntity.ok(ResponseBody.ok(jws));
        } catch (SignerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBody.error(e.getCode(), e.getMessage()));
        }
    }
}

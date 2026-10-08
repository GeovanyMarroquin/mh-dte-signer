package com.digital.signature.service;

import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import com.digital.signature.model.Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignerService {

    private final CertificateService certificateService;
    private final PasswordService passwordService;
    private final PrivateKeyService privateKeyService;
    private final JwsService jwsService;
    private final ObjectMapper objectMapper;

    public String sign(String taxpayerId, String privateKeyPassword, JsonNode documentJson) {
        long startTime = System.currentTimeMillis();
        String generationCode = generationCode(documentJson);
        try {
            String jws = signInternal(taxpayerId, privateKeyPassword, documentJson);
            audit(taxpayerId, generationCode, "OK", startTime);
            return jws;
        } catch (SignerException e) {
            audit(taxpayerId, generationCode, e.getCode(), startTime);
            throw e;
        }
    }

    private String signInternal(String taxpayerId, String privateKeyPassword, JsonNode documentJson) {
        Certificate certificate = certificateService.loadByTaxpayerId(taxpayerId);

        if (!passwordService.matches(privateKeyPassword, certificate.getPrivateKey().getPasswordHash())) {
            throw new SignerException(
                    ErrorCodes.INVALID_PASSWORD,
                    "Password no valido: " + certificate.getTaxpayerId());
        }

        String embodied = certificate.getPrivateKey().getEmbodied();
        if (embodied == null || embodied.isEmpty()) {
            throw new SignerException(ErrorCodes.INVALID, ErrorCodes.MSG_INVALID);
        }

        byte[] payload = serialize(documentJson);
        RSAPrivateKey privateKey = privateKeyService.decode(embodied);
        return jwsService.sign(payload, privateKey);
    }

    private static String generationCode(JsonNode documentJson) {
        if (documentJson == null || documentJson.isNull()) {
            return null;
        }
        JsonNode identification = documentJson.get("identificacion");
        if (identification == null || identification.isNull()) {
            return null;
        }
        JsonNode generationCode = identification.get("codigoGeneracion");
        if (generationCode == null || generationCode.isNull()) {
            return null;
        }
        return generationCode.asString();
    }

    private static void audit(String taxpayerId, String generationCode, String result, long startTime) {
        log.info("sign taxpayerId={} timestamp={} generationCode={} result={} durationMs={}",
                sanitizeForLog(taxpayerId), Instant.now(), sanitizeForLog(generationCode), result,
                System.currentTimeMillis() - startTime);
    }

    private static String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\p{Cntrl}", "?");
    }

    private byte[] serialize(JsonNode documentJson) {
        try {
            if (documentJson == null || documentJson.isNull()) {
                return "null".getBytes(StandardCharsets.UTF_8);
            }
            ObjectMapper canonical = objectMapper.rebuild()
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                    .build();
            return canonical.writeValueAsBytes(documentJson);
        } catch (Exception e) {
            throw new SignerException(ErrorCodes.STRING_TO_JSON, ErrorCodes.MSG_STRING_TO_JSON, e);
        }
    }
}

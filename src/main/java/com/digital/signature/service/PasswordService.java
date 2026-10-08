package com.digital.signature.service;

import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Verifica que hex(SHA-512(texto plano)) coincida con el hash almacenado.
 * La comparacion no se detiene en el primer byte distinto.
 */
@Service
public class PasswordService {

    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            byte[] actual = HexFormat.of().formatHex(hash).getBytes(StandardCharsets.US_ASCII);
            byte[] expected = hashedPassword.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(actual, expected);
        } catch (NoSuchAlgorithmException e) {
            throw new SignerException(ErrorCodes.UNCATALOGUED, ErrorCodes.MSG_UNCATALOGUED, e);
        }
    }
}

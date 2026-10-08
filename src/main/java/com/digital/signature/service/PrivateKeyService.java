package com.digital.signature.service;

import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Decodifica embodied (Base64 estandar de PKCS#8 DER) y exige RSA.
 * Base64 invalido -> 810.
 * Parse que no sea PKCS#8 o no sea RSA -> 803.
 */
@Service
public class PrivateKeyService {

    public RSAPrivateKey decode(String embodiedBase64) {
        final byte[] der;
        try {
            // MimeDecoder tolera saltos de linea. Sin ellos equivale al Base64 estandar.
            der = Base64.getMimeDecoder().decode(embodiedBase64);
        } catch (IllegalArgumentException e) {
            throw new SignerException(ErrorCodes.JSON_TO_STRING, ErrorCodes.MSG_JSON_TO_STRING, e);
        }
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey key = factory.generatePrivate(new PKCS8EncodedKeySpec(der));
            if (!(key instanceof RSAPrivateKey rsa)) {
                throw new SignerException(ErrorCodes.NO_PUBLIC_KEY, ErrorCodes.MSG_NO_PUBLIC_KEY);
            }
            return rsa;
        } catch (SignerException e) {
            throw e;
        } catch (Exception e) {
            throw new SignerException(ErrorCodes.NO_PUBLIC_KEY, ErrorCodes.MSG_NO_PUBLIC_KEY, e);
        }
    }
}

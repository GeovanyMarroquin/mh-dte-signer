package com.digital.signature.service;

import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;

@Service
public class JwsService {

    public String sign(byte[] payload, RSAPrivateKey privateKey) {
        try {
            return Jwts.builder()
                    .content(payload)
                    .signWith(privateKey, Jwts.SIG.RS512)
                    .compact();
        } catch (SignerException e) {
            throw e;
        } catch (Exception e) {
            throw new SignerException(ErrorCodes.UNCATALOGUED, ErrorCodes.MSG_UNCATALOGUED, e);
        }
    }
}

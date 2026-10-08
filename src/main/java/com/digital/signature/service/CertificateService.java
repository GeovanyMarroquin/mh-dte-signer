package com.digital.signature.service;

import com.digital.signature.config.AppProperties;
import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import com.digital.signature.model.Certificate;
import com.digital.signature.model.PrivateKeyMaterial;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Carga el certificado MH desde ${dir}/{nit}.crt (XML permisivo).
 * Solo extrae nit y privateKey; el resto del XML se ignora.
 * El nit solo puede ser un nombre de archivo dentro de ese directorio.
 */
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final Pattern SAFE_FILE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Pattern TAXPAYER_ID_FORMAT = Pattern.compile(
            "(?:[0-9]{14}|[0-9]{4}-[0-9]{6}-[0-9]{4})");

    private final AppProperties properties;

    private final XmlMapper xmlMapper = mapperXml();

    /**
     * El parser no procesa DTD ni entidades externas. Un .crt malicioso
     * no puede hacer que el proceso lea otros archivos.
     */
    private static XmlMapper mapperXml() {
        XMLInputFactory xmlIn = XMLInputFactory.newFactory();
        xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlIn.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return XmlMapper.builder(new XmlFactory(xmlIn))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public Certificate loadByTaxpayerId(String taxpayerId) {
        Path file = fileResolve(taxpayerId);
        try {
            CertificateXml xml = xmlMapper.readValue(file.toFile(), CertificateXml.class);
            KeyXml key = xml.getPrivateKey() != null ? xml.getPrivateKey() : new KeyXml();
            return new Certificate(
                    xml.getTaxpayerId(),
                    new PrivateKeyMaterial(
                            key.getAlgorithm(), key.getPasswordHash(), key.getEmbodied(),
                            key.getFormat(), key.getKeyType()));
        } catch (SignerException e) {
            throw e;
        } catch (Exception e) {
            throw new SignerException(ErrorCodes.UNCATALOGUED, ErrorCodes.MSG_UNCATALOGUED, e);
        }
    }

    /**
     * Resuelve {dir}/{nit}.crt y rechaza cualquier nit que salga del directorio
     * (separadores, "..", enlaces simbolicos hacia afuera).
     */
    private Path fileResolve(String taxpayerId) {
        if (taxpayerId == null || !TAXPAYER_ID_FORMAT.matcher(taxpayerId).matches()) {
            throw new SignerException(ErrorCodes.REQUIRED_DATA, ErrorCodes.MSG_INVALID_TAXPAYER_ID);
        }
        String normalizedTaxpayerId = taxpayerId.replace("-", "");
        String dir = properties.getCertificateDirectory();
        if (dir == null || dir.isBlank()) {
            dir = "./uploads/certs/";
        }
        final Path base;
        try {
            base = Path.of(dir).toAbsolutePath().normalize().toRealPath();
        } catch (IOException e) {
            throw new SignerException(ErrorCodes.NO_FILE, ErrorCodes.MSG_NO_FILE, e);
        }
        if (!SAFE_FILE_IDENTIFIER.matcher(normalizedTaxpayerId).matches()) {
            throw new SignerException(ErrorCodes.REQUIRED_DATA, ErrorCodes.MSG_INVALID_TAXPAYER_ID);
        }
        Path candidate = base.resolve(normalizedTaxpayerId + ".crt").normalize();
        if (!candidate.startsWith(base) || !Files.isRegularFile(candidate)) {
            throw new SignerException(ErrorCodes.NO_FILE, ErrorCodes.MSG_NO_FILE);
        }
        try {
            Path real = candidate.toRealPath();
            if (!real.startsWith(base)) {
                throw new SignerException(ErrorCodes.NO_FILE, ErrorCodes.MSG_NO_FILE);
            }
            return real;
        } catch (SignerException e) {
            throw e;
        } catch (IOException e) {
            throw new SignerException(ErrorCodes.NO_FILE, ErrorCodes.MSG_NO_FILE, e);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CertificateXml {
        @JsonProperty("nit")
        private String taxpayerId;
        private KeyXml privateKey;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyXml {
        private String algorithm;
        @JsonProperty("clave")
        private String passwordHash;
        private String embodied;
        private String format;
        private String keyType;
    }
}

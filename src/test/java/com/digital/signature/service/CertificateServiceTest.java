package com.digital.signature.service;

import com.digital.signature.config.AppProperties;
import com.digital.signature.exception.ErrorCodes;
import com.digital.signature.exception.SignerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertificateServiceTest {

    @Test
    void rejectsInvalidTaxpayerIdFormat() {
        CertificateService service = new CertificateService(mock(AppProperties.class));

        SignerException exception = assertThrows(
                SignerException.class,
                () -> service.loadByTaxpayerId("0000-000000-000-0"));

        assertEquals(ErrorCodes.REQUIRED_DATA, exception.getCode());
        assertEquals(ErrorCodes.MSG_INVALID_TAXPAYER_ID, exception.getMessage());
    }

    @Test
    void normalizesValidTaxpayerIdBeforeLoadingCertificate() {
        AppProperties properties = mock(AppProperties.class);
        when(properties.getCertificateDirectory()).thenReturn("uploads/certs");
        CertificateService service = new CertificateService(properties);

        assertEquals("00000000000000", service
                .loadByTaxpayerId("0000-000000-0000")
                .getTaxpayerId());
        assertEquals("00000000000000", service
                .loadByTaxpayerId("00000000000000")
                .getTaxpayerId());
    }
}

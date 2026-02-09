package com.erpservices.nfe.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CnpjValidatorTest {
    
    @Test
    public void testValidCnpj() {
        CnpjValidator validator = new CnpjValidator();
        assertTrue(validator.isValid("11222333000181"));
    }
    
    @Test
    public void testInvalidCnpj() {
        CnpjValidator validator = new CnpjValidator();
        assertFalse(validator.isValid("00000000000000"));
    }
    
    @Test
    public void testNullCnpj() {
        CnpjValidator validator = new CnpjValidator();
        assertFalse(validator.isValid(null));
    }
}

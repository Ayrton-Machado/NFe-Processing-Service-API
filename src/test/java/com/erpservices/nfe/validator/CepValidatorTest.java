package com.erpservices.nfe.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CepValidatorTest {
    
    @Test
    public void testValidCep() {
        CepValidator validator = new CepValidator();
        assertTrue(validator.isValid("12345678"));  
    }
    
    @Test
    public void testValidFormattedCep() {
        CepValidator validator = new CepValidator();
        assertTrue(validator.isValid("12345-678"));  
    }
    
    @Test
    public void testInvalidCep() {
        CepValidator validator = new CepValidator();
        assertFalse(validator.isValid("00000000"));  
    }
    
    @Test
    public void testNullCep() {
        CepValidator validator = new CepValidator();
        assertFalse(validator.isValid(null));
    }
    
    @Test
    public void testShortCep() {
        CepValidator validator = new CepValidator();
        assertFalse(validator.isValid("1234567"));
    }
    
    @Test
    public void testLongCep() {
        CepValidator validator = new CepValidator();
        assertFalse(validator.isValid("123456789"));
    }
}

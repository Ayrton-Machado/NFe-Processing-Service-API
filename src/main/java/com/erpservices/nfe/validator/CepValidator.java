package com.erpservices.nfe.validator;

public class CepValidator {
    public boolean isValid(String cep) {
        if (cep == null) {
            return false;
        }
        
        // Remove hífen se vier formatado (12345-678 → 12345678)
        cep = cep.replaceAll("[^0-9]", "");
        
        // CEP tem 8 dígitos
        if (cep.length() != 8) {
            return false;
        }
        
        // Rejeita CEP inválido (todos zeros)
        if (cep.equals("00000000")) {
            return false;
        }
        
        return true;
    }}

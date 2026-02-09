package com.erpservices.nfe.validator;

public class CnpjValidator {
    
    public boolean isValid(String cnpj) {
        if (cnpj == null) {
            return false;
        }
        
        if (cnpj.length() != 14) {
            return false;
        }
        
        if (cnpj.equals("00000000000000")) {
            return false;
        }
        
        return true;
    }
}

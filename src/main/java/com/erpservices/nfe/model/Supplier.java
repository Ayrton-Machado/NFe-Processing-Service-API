package com.erpservices.nfe.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public class Supplier extends PanacheEntity {
    public String cnpj;
    public String corporateName;
    public String contactEmail;
    public String status; // ACTIVE, INACTIVE, BLOCKED
}

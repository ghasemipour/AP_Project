package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDto {
    private String bank_name;
    private String account_number;

    public BankAccountDto() {}
}

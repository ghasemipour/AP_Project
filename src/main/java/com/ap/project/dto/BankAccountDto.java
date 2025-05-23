package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDto {
    private String bankName;
    private String accountNumber;

    public BankAccountDto() {}
}

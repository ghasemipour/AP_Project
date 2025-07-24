package com.ap.project.dto;

import com.ap.project.Enums.TransactionMethod;
import com.ap.project.Enums.TransactionStatus;

public class TransactionDto {

    private Integer id;
    private Integer order_id;
    private Integer user_id;
    private TransactionMethod method;
    private TransactionStatus status;
    private Double amount;

    public TransactionDto(Integer id, Integer order_id, Integer user_Id, TransactionMethod method, TransactionStatus status, Double amount) {
        this.id = id;
        this.order_id = order_id;
        user_id = user_Id;
        this.method = method;
        this.status = status;
        this.amount = amount;
    }
}

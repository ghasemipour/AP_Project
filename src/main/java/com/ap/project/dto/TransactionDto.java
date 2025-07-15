package com.ap.project.dto;

import com.ap.project.Enums.TransactionMethod;
import com.ap.project.Enums.TransactionStatus;
import jakarta.persistence.criteria.CriteriaBuilder;

public class TransactionDto {

    private Integer id;
    private Integer order_id;
    private Integer userId;
    private Integer user_Id;
    private TransactionMethod method;
    private TransactionStatus status;

    public TransactionDto(Integer id, Integer order_id, Integer user_Id, TransactionMethod method, TransactionStatus status) {
        this.id = id;
        this.order_id = order_id;
        userId = user_Id;
        this.method = method;
        this.status = status;
    }
}

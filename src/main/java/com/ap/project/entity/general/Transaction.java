package com.ap.project.entity.general;


import com.ap.project.Enums.TransactionMethod;
import com.ap.project.Enums.TransactionStatus;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.user.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionMethod method;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    @Column(nullable = false)
    private Double amount;

    public Transaction(Order order, Wallet wallet, Customer customer, TransactionMethod transactionMethod, TransactionStatus transactionStatus, Double amount) {
        this.order = order;
        this.wallet = wallet;
        this.user = customer;
        this.method = transactionMethod;
        this.status = transactionStatus;
        this.amount = amount;
    }

    public Transaction(Order order, Wallet wallet, Customer customer, TransactionMethod transactionMethod, TransactionStatus transactionStatus, Integer amount) {
        this.order = order;
        this.wallet = wallet;
        this.user = customer;
        this.method = transactionMethod;
        this.status = transactionStatus;
        this.amount = Double.valueOf(amount);
    }

    public Transaction() {

    }
    public TransactionDto getDto() {
        if (order != null)
            return new TransactionDto(id, order.getId(), user.getUserId(), method, status, amount);
        else return new TransactionDto(id, null, user.getUserId(), method, status, amount);
    }
}

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

    @OneToOne
    @JoinColumn(name = "order_id")
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

    public Transaction(Order o, Wallet wallet, Customer customer, TransactionMethod transactionMethod, TransactionStatus transactionStatus) {
     this.order = o;
     this.wallet = wallet;
     this.user = customer;
     this.method = transactionMethod;
     this.status = transactionStatus;
    }
    public Transaction(){

    }

    public TransactionDto getDto() {
        return new TransactionDto(id, order.getId(), user.getUserId(), method, status);
    }
}

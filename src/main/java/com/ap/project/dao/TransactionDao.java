package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.user.Customer;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class TransactionDao {
    public static List<TransactionDto> getTransactionHistoryByUserID(int userId) {
        org.hibernate.Transaction tx = null;
        List<TransactionDto> result = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String hql = "FROM Transaction t WHERE t.user.userId = :userId";
            Query<Transaction> query = session.createQuery(hql, Transaction.class);
            query.setParameter("userId", userId);
            List<Transaction> transactions = query.list();
            for (Transaction transaction: transactions) {
                result.add(transaction.getDto());
            }
            tx.commit();
        } catch (Exception e) {
            transactionRollBack(tx, e);
        }
        return result;
    }

    public static void topUpWallet(int userId, double amount, HttpExchange exchange) {
        org.hibernate.Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, userId);
            if(customer == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(customer.getWallet());
            Wallet wallet = customer.getWallet();
            wallet.topUp(amount);
            transaction.commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

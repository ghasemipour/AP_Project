package com.ap.project.dao;

import com.ap.project.Enums.TransactionMethod;
import com.ap.project.Enums.TransactionStatus;
import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.Exceptions.NoSuchWallet;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.restaurant.Order;
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

    public static void saveTransaction(Transaction newTransaction, int userId, int orderId, int walletId, HttpExchange exchange) {
        org.hibernate.Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, userId);
            if(customer == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(customer.getWallet());
            Hibernate.initialize(customer.getOrders());
            Hibernate.initialize(customer.getTransactions());
            if(orderId > 0){
                Order order = session.get(Order.class, orderId);
                if(order == null) {
                    exchange.sendResponseHeaders(404, -1);
                    throw new NoSuchOrder(orderId + "Order not found");
                }
                order.setTransaction(newTransaction);
            }
            if(walletId > 0){
                Wallet wallet = session.get(Wallet.class, walletId);
                if(wallet == null) {
                    exchange.sendResponseHeaders(404, -1);
                    throw new NoSuchWallet(walletId + "Wallet not found");
                }
                wallet.addTransaction(newTransaction);
            }
            customer.addTransaction(newTransaction);
            session.persist(transaction);
            session.merge(customer);
            transaction.commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void saveWallet(Wallet wallet, int userId) {
        org.hibernate.Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, userId);
            if(customer == null) {
                throw new NoSuchUser(userId + "User not found");
            }
            wallet.setCustomer(customer);
            session.persist(wallet);
            session.merge(customer);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }
}

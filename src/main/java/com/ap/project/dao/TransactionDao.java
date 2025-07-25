package com.ap.project.dao;

import com.ap.project.Enums.Status;
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
import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;
import static com.ap.project.httpHandler.SuperHttpHandler.sendNotFoundMessage;

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
            for (Transaction transaction : transactions) {
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
            if (customer == null) {
                sendNotFoundMessage("{\"error\": \"User not found\"}", exchange);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(customer.getWallet());
            Wallet wallet = customer.getWallet();
            wallet.topUp(amount);
            session.merge(wallet);
            transaction.commit();
            session.refresh(wallet);
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static void saveTransaction(Transaction newTransaction, int userId, int orderId, int walletId, HttpExchange exchange) {
        org.hibernate.Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, userId);
            if (customer == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(customer.getWallet());
            Hibernate.initialize(customer.getOrders());
            Hibernate.initialize(customer.getTransactions());
            if (orderId > 0) {
                Order order = session.get(Order.class, orderId);
                if (order == null) {
                    exchange.sendResponseHeaders(404, -1);
                    throw new NoSuchOrder(orderId + "Order not found");
                }
                if (customer.getUserId() != order.getUser().getUserId()) {
                    String response = "Unauthorized user.";
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(403, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
                order.getTransactions().add(newTransaction);
            }
            if (walletId > 0) {
                Wallet wallet = session.get(Wallet.class, walletId);
                if (wallet == null) {
                    exchange.sendResponseHeaders(404, -1);
                    throw new NoSuchWallet(walletId + "Wallet not found");
                }
                wallet.addTransaction(newTransaction);
            }
            customer.addTransaction(newTransaction);
            session.persist(newTransaction);
            session.merge(customer);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static void saveWallet(Wallet wallet, int userId) {
        org.hibernate.Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, userId);
            if (customer == null) {
                throw new NoSuchUser(userId + "User not found");
            }
            wallet.setCustomer(customer);
            session.persist(wallet);
            session.merge(customer);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static boolean onlinePayment(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Order order = transaction.getOrder();
            Wallet wallet = session.get(Wallet.class, transaction.getWallet().getId());
            Hibernate.initialize(transaction);

            if (transaction.getMethod().equals(TransactionMethod.WALLET)) {
                int payPrice = transaction.getOrder().getPay_price();
                double balance = wallet.getBalance();

                if (payPrice > balance) {
                    order.setStatus(Status.PAYMENT_FAILED);
                    transaction.setStatus(TransactionStatus.FAILED);
                    return false;
                } else {
                    wallet.setBalance(balance - payPrice);
                    session.merge(wallet);
                    order.setStatus(Status.WAITING_VENDOR);
                    transaction.setStatus(TransactionStatus.SUCCESS);
                }
            } else if (transaction.getMethod().equals(TransactionMethod.ONLINE))
                order.setStatus(Status.WAITING_VENDOR);
            session.persist(transaction);
            session.merge(order);
            tx.commit();
            return true;

        } catch (Exception e) {
            transactionRollBack(tx, e);
            return false;
        }
    }

    public static List<TransactionDto> getAllTransactions(String search, String user, String method, String status) {
        org.hibernate.Transaction tx = null;
        List<TransactionDto> results = new ArrayList<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            StringBuilder hql = new StringBuilder("SELECT DISTINCT t FROM Transaction t LEFT JOIN t.order o" +
                    " LEFT JOIN o.items oi LEFT JOIN" +
                    " oi.food f WHERE 1=1");

            if (search != null && !search.isEmpty()) {
                hql.append(" AND f.name LIKE :search");
            }
            if (user != null && !user.isEmpty()) {
                hql.append(" AND t.user.userId = :user");
            }
            if (method != null && !method.isEmpty()) {
                    hql.append(" AND t.method = :method");
            }
            if (status != null && !status.isEmpty()) {
                    hql.append(" AND t.status = :status");
            }
            Query<Transaction> query = session.createQuery(hql.toString(), Transaction.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            if (user != null && !user.isEmpty()) {
                query.setParameter("user", user);
            }
            if (method != null && !method.isEmpty()) {
                if (TransactionMethod.fromString(method) == null)
                    throw new IllegalArgumentException("Invalid transaction method: " + method);
                else query.setParameter("method", TransactionMethod.fromString(method));
            }
            if (status != null && !status.isEmpty()) {
                if (TransactionStatus.fromString(status) == null)
                    throw new IllegalArgumentException("Invalid transaction status: " + status);
                else query.setParameter("status", TransactionStatus.fromString(status));
            }
            List<Transaction> transactions = query.list();
            for (Transaction transaction : transactions) {
                results.add(transaction.getDto());
            }
            tx.commit();
        } catch (Exception e) {
            transactionRollBack(tx, e);
        }

        return results;
    }

    public static Double getBalance(int userId) {
        Wallet wallet = null;
        Double balance = 0.0;
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            wallet = session.createQuery("FROM Wallet w WHERE w.customer.userId = :userId", Wallet.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            if (wallet == null) {
                return balance;
            } else {
                balance = wallet.getBalance();
            }
            tx.commit();
        }
        return balance;
    }
}

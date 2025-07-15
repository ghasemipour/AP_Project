package com.ap.project.dao;

import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.util.HibernateUtil;
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
}

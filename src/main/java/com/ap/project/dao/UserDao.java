package com.ap.project.dao;

import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import org.hibernate.Session;

public class UserDao {

    public static void saveUser(User user) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (SystemException ex) {
                    throw new RuntimeException(ex);
                }
            }
            e.printStackTrace();
        }

    }
    public static boolean IsPhoneNumberTaken(String phoneNumber) {
        boolean res = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();
            Long count = session.createQuery(
                            "select count(u) from User u where u.phoneNumber = :phoneNumber", Long.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .uniqueResult();

            res = count != null && count > 0;
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }


        return res;
    }

    public static boolean IsEmailTaken(String email) {
        boolean res = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();
            Long count = session.createQuery(
                            "select count(u) from User u where u.email = :email", Long.class)
                    .setParameter("email", email)
                    .uniqueResult();

            res = count != null && count > 0;
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }


        return res;
    }
}

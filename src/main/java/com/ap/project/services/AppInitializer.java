package com.ap.project.services;

import com.ap.project.entity.user.*;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class AppInitializer {
    public static void initializeAdmin() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            String hql = "FROM User u WHERE u.email = :email";
            Admin admin = (Admin) session.createQuery(hql)
                    .setParameter("email", "admin@email.com")
                    .uniqueResult();

            if(admin == null) {
                User newAdmin = new Admin("admin", "09122593542", "adminPass", "admin@email.com");
                session.save(newAdmin);
                System.out.println("Admin created successfully");
            } else {
                System.out.println("Admin already exists");
            }
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }
}

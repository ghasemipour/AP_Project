package com.ap.project.dao;

import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RestaurantDao {

    public static void saveRestaurant(Restaurant restaurant, String sellerId) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Seller seller = session.get(Seller.class, sellerId);
            if (seller == null) {
                throw new RuntimeException("No such seller");
            }
            seller.addRestaurant(restaurant);
            session.persist(restaurant);
            session.merge(seller);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        }
    }

   public static boolean isPhoneNumberTaken(final String phoneNumber) {
       boolean res = false;
       Session session = HibernateUtil.getSessionFactory().openSession();

       try {
           session.beginTransaction();
           Long count = session.createQuery(
                           "select count(r) from Restaurant r where r.phone = :phoneNumber", Long.class)
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

}

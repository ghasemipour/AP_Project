package com.ap.project.dao;

import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RestaurantDao {

    public static void saveRestaurant(Restaurant restaurant, User user) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(restaurant);
            ((Seller)user).addRestaurant(restaurant);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static boolean isPhoneNumberTaken(final String phoneNumber) {

    }


}

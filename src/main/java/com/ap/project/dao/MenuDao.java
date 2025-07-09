package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchSeller;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MenuDao {
    public static void save(Menu menu, int restaurantId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new NoSuchSeller(restaurantId + " not found");
            }
            restaurant.addMenu(menu);
            session.persist(menu);
            session.merge(restaurant);
            transaction.commit();
        }
    }
}

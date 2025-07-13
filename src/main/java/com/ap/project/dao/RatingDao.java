package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.Rating;
import com.ap.project.entity.user.Customer;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class RatingDao {
    public static void submitRating(Rating rating, HttpExchange exchange, int orderId, int userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            Customer user = session.get(Customer.class, userId);
            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchOrder(orderId + " not found");
            }
            if (user == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + " not found");
            }

            order.addRating(rating);
            user.addRating(rating);

            session.persist(rating);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }
}

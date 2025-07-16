package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.Exceptions.NoSuchRating;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.RatingDto;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.Rating;
import com.ap.project.entity.user.Customer;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;
import static com.ap.project.httpHandler.SuperHttpHandler.sendNotFoundMessage;

public class RatingDao {
    public static void submitRating(Rating rating, HttpExchange exchange, int orderId, int userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            Customer user = session.get(Customer.class, userId);
            if (order == null) {
                String response = "{\"error\": \"order not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchOrder(orderId + " not found");
            }
            if (user == null) {
                String response = "{\"error\": \"user not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchUser(userId + " not found");
            }
            if (order.getUser().getUserId() != userId) {
                String response = "Unauthorized user.";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(403, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            if (order.getRating() != null) {
                String response = "Order already has a rating.";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(403, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            order.addRating(rating);
            user.addRating(rating);

            session.persist(rating);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static List<RatingDto> getRatingsForItem(int foodId) {
        Transaction transaction = null;
        List<RatingDto> result = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "SELECT DISTINCT r FROM Rating r JOIN r.order o JOIN o.items i WHERE i.food.id = :foodId";
            List<Rating> ratings = session.createQuery(hql, Rating.class)
                    .setParameter("foodId", foodId)
                    .list();

            //to load up the images

            for (Rating rating : ratings) {
                Hibernate.initialize(rating.getImageBase64());
            }
            for (Rating rating : ratings) {
                result.add(rating.getRatingDto());
            }
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
        return result;
    }

    public static Rating getRatingByID(int ratingId, HttpExchange exchange) {
        Transaction transaction = null;
        Rating rating = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            rating = session.get(Rating.class, ratingId);
            if (rating == null) {
                String response = "{\"error\": \"Rating not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchRating(ratingId + " not found.");
            }
            Hibernate.initialize(rating.getImageBase64());
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
        return rating;
    }

    public static void updateRating(int ratingId, RatingDto req, HttpExchange exchange, Customer user) {
        Transaction transaction = null;
        Rating rating;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            rating = session.get(Rating.class, ratingId);
            if (rating == null) {
                String response = "{\"error\": \"Rating not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchRating(ratingId + " not found.");
            }
            if (rating.getUser().getUserId() != user.getUserId()) {
                String response = "Unauthorized user.";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(403, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            if (req.getRating() != null) {
                rating.setRating(req.getRating());
            }
            if (req.getComment() != null) {
                rating.setComment(req.getComment());
            }
            if (req.getImageBase64() != null) {
                rating.setImageBase64(req.getImageBase64());
            }
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static void deleteRating(int ratingId, HttpExchange exchange, Customer user) {
        Transaction transaction = null;
        Rating rating;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            rating = session.get(Rating.class, ratingId);
            if (rating == null) {
                String response = "{\"error\": \"Rating not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchRating(ratingId + " not found.");
            }

            Customer ratingUser = rating.getUser();
            Order order = rating.getOrder();
            if (ratingUser.getUserId() != user.getUserId()) {
                String response = "Unauthorized user.";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(403, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            order.removeRating(rating);
            ratingUser.removeRating(rating);

            session.remove(rating);
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);

        }
    }
}

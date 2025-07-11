package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchSeller;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RestaurantDao {

    public static void saveRestaurant(Restaurant restaurant, int sellerId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Seller seller = session.get(Seller.class, sellerId);
            if (seller == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchSeller(sellerId + " not found");
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

    public static List<Restaurant> getRestaurantsBySellerId(int userId, HttpExchange exchange) {
        List<Restaurant> restaurants = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Seller seller = session.get(Seller.class, userId);
            if (seller == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new RuntimeException("No such seller");
            }
            Hibernate.initialize(seller.getRestaurants());
            restaurants = seller.getRestaurants();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return restaurants;
        }
    }

    public static Restaurant getRestaurantById(int restaurantId) {
        Transaction transaction = null;
        Restaurant restaurant = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.id = :id", Long.class)
                    .setParameter("id", restaurantId)
                    .getSingleResult();
            if (count != null && count <= 0) {
                return null;
            }
            restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        }
        return restaurant;
    }

    public static void updateRestaurant(int restaurantId, RestaurantDto req) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (req.getName() != null) {
                restaurant.setName(req.getName());
            }
            if (req.getPhone() != null) {
                restaurant.setPhone(req.getPhone());
            }
            if (req.getAddress() != null) {
                restaurant.setAddress(req.getAddress());
            }
            if (req.getTax_fee() != null) {
                restaurant.setTax_fee(req.getTax_fee());
            }
            if (req.getAdditional_fee() != null) {
                restaurant.setAdditional_fee(req.getAdditional_fee());
            }
            if (req.getLogoBase64() != null) {
                restaurant.setLogoBase64(req.getLogoBase64());
            }
            if (req.getWorking_hour() != null) {
                restaurant.setWorking_hour(req.getWorking_hour());
            }

            // Could cause problems and is unnecessary | discuss later
            session.update(restaurant);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        }
    }

    public static int getSellerId(int restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT r.owner.id FROM Restaurant r WHERE r.id = :id", Integer.class).setParameter("id", restaurantId).getSingleResult();
        }
    }
}

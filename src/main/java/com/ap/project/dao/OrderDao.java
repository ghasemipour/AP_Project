package com.ap.project.dao;

import com.ap.project.Enums.Status;
import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.OrderDto;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Customer;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class OrderDao {

    public static Order getOrderFromId(int orderId, HttpExchange exchange) {
        Transaction transaction = null;
        Order order = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            order = session.get(Order.class, orderId);
            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchOrder(orderId + " not found.");
            }
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
        return order;
    }

    public static void changeOrderStatus(int orderId, Status status, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);

            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchOrder(orderId + " not found.");
            }

            order.setStatus(status);
            session.update(order);
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static void submitOrder(Order order, int vendorId, int userId, HttpExchange exchange) throws IOException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Restaurant restaurant = session.get(Restaurant.class, vendorId);
            Customer customer = session.get(Customer.class, userId);

            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(vendorId + " not found");
            }
            if (customer == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + " not found");
            }

            restaurant.addOrder(order);
            customer.addOrder(order);

            session.persist(order);
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static List<OrderDto> getOrderHistory(int userId, String search, String vendor) {
        List<OrderDto> result = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            String hql = "FROM Order o WHERE o.user.userId = :userId";

            if (search != null && !search.isEmpty()) {
                hql += " AND EXISTS (SELECT i FROM o.items i WHERE lower(i.food.name) LIKE :search)";
            }

            if (vendor != null && !vendor.isEmpty()) {
                hql += " AND o.restaurant.id = :vendorId";
            }

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("userId", userId);

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (vendor != null && !vendor.isEmpty()) {
                query.setParameter("vendorId", Integer.parseInt(vendor));
            }

            List<Order> orders = query.list();

            for (Order order : orders) {
                result.add(order.getOrderDto());
            }

            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }

        return result;
    }


}

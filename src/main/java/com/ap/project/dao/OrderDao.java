package com.ap.project.dao;

import com.ap.project.Enums.Status;
import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.OrderDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;
import static com.ap.project.httpHandler.SuperHttpHandler.sendNotFoundMessage;

public class OrderDao {

    public static Order getOrderFromId(int orderId, HttpExchange exchange) {
        Transaction transaction = null;
        Order order = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            order = session.get(Order.class, orderId);
            if (order == null) {
                String response = "{\"error\": \"Order not found\"}\n";
                    sendNotFoundMessage(response, exchange);
                    throw new NoSuchOrder(response);
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
                String response = "{\"error\": \"Vendor not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchRestaurant(vendorId + " not found");
            }
            if (customer == null) {
                String response = "{\"error\": \"User not found\"}";
                sendNotFoundMessage(response, exchange);
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


    public static List<Order> getOrderByStatus(HttpExchange exchange, Status status) {
        List<Order> orders = new ArrayList<>();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<Order> query = session.createQuery("FROM Order o WHERE o.status = :status", Order.class);
            query.setParameter("status", status);
            orders = query.getResultList();
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }

        return orders;
    }

    public static Customer getCustomer(int orderId, HttpExchange exchange) {
        Customer customer = null;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchOrder(orderId + "Order not found.");
            }
            Hibernate.initialize(order.getUser());
            customer = order.getUser();
            if(customer == null){
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(orderId + " Order customer not found.");
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customer;
    }

    public static Restaurant getRestaurant(int orderId, HttpExchange exchange) {
        Restaurant restaurant = null;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchOrder(orderId + "Order not found.");
            }
            Hibernate.initialize(order.getRestaurant());
            restaurant = order.getRestaurant();
            if(restaurant == null){
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(orderId + " Order restaurant not found.");
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return restaurant;
    }

    public static List<Order> getDeliveryHistory(Courier courier, String search, String vendorId, String userId) {
        List<Order> result = new ArrayList<>();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Order o WHERE o.courier = :courier AND o.status IN (:statuses)");
            if(vendorId != null && !vendorId.isEmpty()) {
                hql.append(" AND o.restaurant.id = :vendorId");
            }
            if(userId != null && !userId.isEmpty()) {
                hql.append(" AND o.user.id = :userId");
            }
            if(search != null && !search.isEmpty()) {
                hql.append(" AND (lower(o.restaurant.name) LIKE :search OR lower(o.user.name) LIKE :search)");
            }
            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            query.setParameter("courier", courier);
            query.setParameter("statuses", List.of(Status.DELIVERED, Status.RECEIVED));

            if(vendorId != null && !vendorId.isEmpty()) {
                query.setParameter("vendorId", Integer.parseInt(vendorId));
            }
            if(userId != null && !userId.isEmpty()) {
                query.setParameter("userId", Integer.parseInt(userId));
            }
            if(search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            result = query.list();

        } catch (Exception e){
            e.printStackTrace();
        }

        return result;

    }
}

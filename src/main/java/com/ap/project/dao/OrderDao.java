package com.ap.project.dao;

import com.ap.project.Enums.Status;
import com.ap.project.Exceptions.NoSuchOrder;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class OrderDao {

    public static Order getOrderFromId(int orderId) {
        Transaction transaction = null;
        Order order = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            order = session.get(Order.class, orderId);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
        return order;
    }

    public static void changeOrderStatus(int orderId, Status status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);

            if (order == null) {
                throw new NoSuchOrder(orderId + " not found");
            }

            order.setStatus(status);
            session.update(order);
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

}

package com.ap.project.dao;

import com.ap.project.Enums.Status;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.OrderDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class RestaurantDao {

    public static void saveRestaurant(Restaurant restaurant, int sellerId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Seller seller = session.get(Seller.class, sellerId);
            if (seller == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(sellerId + " not found");
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
                throw new NoSuchUser(userId + " not found");
            }
            Hibernate.initialize(seller.getRestaurants());
            restaurants = seller.getRestaurants();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return restaurants;
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

    public static List<OrderDto> getRestaurantOrdersByRestaurantId(int restaurantId, String status, String search, String user, String courier) {
        Transaction transaction = null;
        List<OrderDto> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Order> orders = new ArrayList<>();
            transaction = session.beginTransaction();
            String hql = "FROM Order o WHERE o.restaurant.id = :restaurantId";

            if (status != null && !status.isEmpty()) {
                hql += " AND o.status = :status";
            }
            if (search != null && !search.isEmpty()) {
                hql += " AND exists (select i from o.items i where lower(i.food.name) like :search)";
            }
            if (user != null && !user.isEmpty()) {
                hql += " AND o.user.userId= :user";
            }
            if (courier != null && !courier.isEmpty()) {
                hql += " AND o.courier.userId = :courier";
            }

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("restaurantId", restaurantId);

            if (status != null && !status.isEmpty()) {
                try {
                    query.setParameter("status", Status.valueOf(status.toUpperCase()));
                } catch (Exception e) {
                    transactionRollBack(transaction, e);
                }
            }

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }
            if (user != null && !user.isEmpty()) {
                query.setParameter("user", user);
            }
            if (courier != null && !courier.isEmpty()) {
                query.setParameter("courier", courier);
            }

            orders = query.list();
            for (Order order : orders) {
                results.add(order.getOrderDto());
            }
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
        return results;
    }

    public static List<RestaurantDto> getRestaurantsByFilter(String search, List<String> keywords) {
        Transaction transaction = null;
        List<RestaurantDto> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            StringBuilder hql = new StringBuilder("FROM Restaurant r WHERE 1=1");
            if (search != null && !search.isEmpty()) {
                hql.append(" AND (lower(r.name) LIKE :search)");
            }

            if (keywords != null && !keywords.isEmpty()) {
                hql.append(" AND exists (SELECT DISTINCT f FROM Food f JOIN f.keywords k WHERE f.restaurant = r AND lower(k) IN (:keywords))");
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);

            if(search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (keywords != null && !keywords.isEmpty()) {
                List<String> lowerKeywords = keywords.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

                query.setParameter("keywords", lowerKeywords);
            }

            List<Restaurant> restaurants = query.list();
            for (Restaurant restaurant : restaurants) {
                results.add(restaurant.getRestaurantDto());
            }
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return results;
    }

    public static List<Menu> getRestaurantMenus(int restaurantId, HttpExchange exchange) {
        List<Menu> menus = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + " not found");
            }
            Hibernate.initialize(restaurant.getMenus());
            menus = restaurant.getMenus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menus;
    }

    public static List<RestaurantDto> getTopRestaurants() {
        Transaction transaction = null;
        List<RestaurantDto> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Restaurant> restaurants = session.createQuery("FROM Restaurant ORDER BY ratings_avg DESC", Restaurant.class).setMaxResults(10).list();

            for (Restaurant restaurant:restaurants) {
                results.add(restaurant.getRestaurantDto());
            }

            transaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}

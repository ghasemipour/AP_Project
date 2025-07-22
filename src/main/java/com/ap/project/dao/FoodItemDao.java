package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchFoodItem;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.dto.FoodDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.restaurant.Restaurant;
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

import static com.ap.project.httpHandler.SuperHttpHandler.sendNotFoundMessage;

public class FoodItemDao {

    public static void saveFood(Food food, int vendor_id, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, vendor_id);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(vendor_id + " not found");
            }
            restaurant.addFood(food);
            session.persist(restaurant);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
        }
    }

    public static Food getFoodByID(int foodID, HttpExchange exchange) throws IOException {
        Transaction transaction = null;
        Food food = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            food = session.get(Food.class, foodID);

            if (food == null) {
                String response = "{\"error\": \"Food Item not found\"}";
                sendNotFoundMessage(response, exchange);
                throw new NoSuchFoodItem(foodID + " not found");
            }

            //to load up the keywords

            Hibernate.initialize(food.getKeywords());
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);

        }
        return food;
    }

    public static void updateFood(FoodDto req, int foodID, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Food food = session.get(Food.class, foodID);
            if (food == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchFoodItem(foodID + " not found");
            }

            if (req.getName() != null)
                food.setName(req.getName());
            if (req.getImageBase64() != null)
                food.setImageBase64(req.getImageBase64());
            if (req.getDescription() != null)
                food.setDescription(req.getDescription());
            if (req.getPrice() != null)
                food.setPrice(req.getPrice());
            if (req.getSupply() != null)
                food.setSupply(req.getSupply());
            if (req.getKeywords() != null)
                food.setKeywords(req.getKeywords());

            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);

        }
    }

    public static void deleteFood(int foodId, HttpExchange exchange) {
        Transaction transaction = null;
        Food food;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){

            transaction = session.beginTransaction();
            food = session.get(Food.class, foodId);
            if (food == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchFoodItem(foodId + " not found");
            }

            Restaurant restaurant = food.getRestaurant();
            restaurant.removeFood(food);

            session.remove(food);
            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);

        }
    }

    public static void transactionRollBack(Transaction transaction, Exception e) {
        e.printStackTrace();
        if (transaction != null && transaction.isActive())
            transaction.rollback();

    }

    public static List<FoodDto> getItemsByFilters(String search, int price, List<String> keywords) {
        Transaction transaction = null;
        List<FoodDto> results = new ArrayList<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            StringBuilder hql = new StringBuilder("FROM Food f WHERE 1=1");

            if (search != null && !search.isEmpty()) {
                hql.append(" AND (lower(f.name) LIKE :search)");
            }

            if (price >= 0) {
                hql.append(" AND f.price <= :price");
            }

            if (keywords != null && !keywords.isEmpty()) {
                hql.append(" AND exists (select k from f.keywords k where k in :keywords)");
            }

            Query<Food> query = session.createQuery(hql.toString(), Food.class);

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (price >= 0) {
                query.setParameter("price", price);
            }

            if (keywords != null && !keywords.isEmpty()) {
                List<String> lowerKeywords = keywords.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

                query.setParameter("keywords", lowerKeywords);
            }

            List<Food> foodList = query.getResultList();

            //to load up the keywords

            for (Food food : foodList)
                Hibernate.initialize(food.getKeywords());

            for (Food food : foodList) {
                results.add(food.getFoodDto());
            }


            transaction.commit();

        } catch (Exception e) {
            transactionRollBack(transaction, e);
            throw e;
        }

        return results;
    }


    public static List<String> getKeywords(int foodId) {
        List<String> keywords = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Food food = session.get(Food.class, foodId);
            if (food == null) {
                throw new NoSuchFoodItem(foodId + " not found");
            }
            Hibernate.initialize(food.getKeywords());
            keywords = food.getKeywords();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keywords;
    }

    public static List<FoodDto> getItemsByRestaurantId(int restaurantId, HttpExchange exchange) {
        List<FoodDto> res = new ArrayList<>();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + " not found");
            }
            Hibernate.initialize(restaurant.getFoodItems());
            List<Food> foodList = restaurant.getFoodItems();
            for (Food food : foodList) {
                Hibernate.initialize(food.getKeywords());
                res.add(food.getFoodDto());
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }
}

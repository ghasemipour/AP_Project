package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchFoodItem;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.dto.FoodDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class FoodItemDao {

    public static void saveFood(Food food, int vendor_id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, vendor_id);
            if (restaurant == null) {
                throw new NoSuchRestaurant(vendor_id + " not found");
            }
            restaurant.addFood(food);
            session.persist(restaurant);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
            throw e;
        }
    }

    public static Food getFoodByID(int foodID) {
        Transaction transaction = null;
        Food food = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            food = session.get(Food.class, foodID);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
            throw e;
        }
        return food;
    }

    public static void updateFood(FoodDto req, int foodID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Food food = session.get(Food.class, foodID);
            if (food == null) {
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
            throw e;
        }
    }

    public static void deleteFood(int foodId, Restaurant restaurant) {
        Transaction transaction = null;
        Food food;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            food = session.get(Food.class, foodId);
            if (food == null)
                throw new NoSuchFoodItem(foodId + " not found");

            restaurant.removeFood(food);
            transaction.commit();
        } catch (Exception e) {
            transactionRollBack(transaction, e);
            throw e;
        }
    }

    private static void transactionRollBack(Transaction transaction, Exception e) {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
    }
}

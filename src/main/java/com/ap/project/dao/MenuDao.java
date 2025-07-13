package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchFoodItem;
import com.ap.project.Exceptions.NoSuchMenu;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.dto.FoodDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class MenuDao {
    public static void save(Menu menu, int restaurantId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "restaurant not found");
            }
            restaurant.addMenu(menu);
            session.persist(menu);
            session.merge(restaurant);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Menu getMenuByTitle(int restaurantId, String menuTitle, HttpExchange exchange) {
        Menu menu = null;
        List<Menu> menus = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "restaurant not found");
            }
            Hibernate.initialize(restaurant.getMenus());
            menus = restaurant.getMenus();
            for(Menu m : menus) {
                if (m.getTitle().equals(menuTitle)) {
                    menu = m;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return menu;
        }
    }

    public static void deleteMenu(int restaurantId, int id, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Menu menu = (Menu) session.get(Menu.class, id);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchMenu(id + "Menu not found");
            }
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if(restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "restaurant not found");
            }
            restaurant.removeMenu(menu);
            session.merge(restaurant);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);

        }
    }

    public static void addFoodItem(int id, int itemId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Menu menu = session.get(Menu.class, id);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchMenu(id + "Menu not found");
            }
            Food food = session.get(Food.class, itemId);
            if(food == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchFoodItem(itemId + "Food item not found");
            }

            menu.addFoodItem(food);
            food.addMenu(menu);
            session.merge(food);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);

        }
    }

    public static void deleteFoodItemFromMenu(int menuId, int foodItemId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Menu menu = (Menu) session.get(Menu.class, menuId);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchMenu(menuId + "Menu not found");
            }
            Food food = (Food) session.get(Food.class, foodItemId);
            if(food == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchFoodItem(foodItemId + "Food item not found");
            }
            if(!menu.getFoodItems().contains(food)) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchFoodItem(foodItemId + "Food item not found");
            }
            menu.removeFoodItem(food);
            food.removeMenu(menu);
            session.merge(food);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }

    public static List<Food> getFoodItems(int menuId, HttpExchange exchange) {
        List<Food> foodItems = null;

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Menu menu = (Menu) session.get(Menu.class, menuId);
            if(menu == null){
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchMenu( menuId + "Menu not found");
            }
            Hibernate.initialize(menu.getFoodItems());
            foodItems = menu.getFoodItems();

        } catch (Exception e){
            e.printStackTrace();
        }

        return foodItems;
    }
}

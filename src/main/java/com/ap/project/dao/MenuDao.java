package com.ap.project.dao;

import com.ap.project.Exceptions.NoSuchSeller;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class MenuDao {
    public static void save(Menu menu, int restaurantId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new NoSuchSeller(restaurantId + " not found");
            }
            restaurant.addMenu(menu);
            session.persist(menu);
            session.merge(restaurant);
            transaction.commit();
        }
    }

    public static Menu getMenuByTitle(int restaurantId, String menuTitle) {
        Menu menu = null;
        List<Menu> menus = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new NoSuchSeller(restaurantId + " not found");
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

    public static void deleteMenu(int restaurantId, int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Menu menu = (Menu) session.get(Menu.class, id);
            if(menu == null) {
                throw new NoSuchSeller(id + " not found");
            }
            Restaurant restaurant = (Restaurant) session.get(Restaurant.class, restaurantId);
            if(restaurant == null) {
                throw new NoSuchSeller(id + " not found");
            }
            restaurant.removeMenu(menu);
            session.merge(restaurant);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
            throw e;
        }
    }
}

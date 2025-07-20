package com.ap.project.dao;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.*;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class UserDao {

    public static void saveUser(User user) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static void updateUser(int id, ProfileDto newProfile) {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try
        {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user != null) {
                if (newProfile.getFull_name() != null && !newProfile.getFull_name().isBlank()) {
                    user.setName(newProfile.getFull_name());
                }

                if (newProfile.getPhone() != null && !newProfile.getPhone().isBlank()) {
                    user.setPhoneNumber(newProfile.getPhone());
                }

                if (newProfile.getEmail() != null && !newProfile.getEmail().isBlank()) {
                    user.setEmail(newProfile.getEmail());
                }

                if (newProfile.getAddress() != null && !newProfile.getAddress().isBlank()) {
                    if (user instanceof HasAddress) {
                        ((HasAddress) user).setAddress(newProfile.getAddress());
                    }
                }

                if (newProfile.getBank_info() != null) {
                    if (newProfile.getBank_info().getBank_name() != null && !newProfile.getBank_info().getBank_name().isBlank()) {
                        if (user instanceof HasBankAccount) {
                            ((HasBankAccount) user).getBankAccount().setBankName(newProfile.getBank_info().getBank_name());
                        }
                    }
                    if (newProfile.getBank_info().getAccount_number() != null && !newProfile.getBank_info().getAccount_number().isBlank()) {
                        if (user instanceof HasBankAccount) {
                            ((HasBankAccount) user).getBankAccount().setAccountNumber(newProfile.getBank_info().getAccount_number());
                        }
                    }
                }

                if (newProfile.getDescription() != null && !newProfile.getDescription().isBlank()) {
                    if (user instanceof Seller) {
                        ((Seller) user).setDiscription(newProfile.getDescription());
                    }
                }

                if (newProfile.getBrandInfo() != null && !newProfile.getBrandInfo().isBlank()) {
                    if (user instanceof Seller) {
                        ((Seller) user).setBrandInfo(newProfile.getBrandInfo());
                    }
                }

                if (newProfile.getProfileImageBase64() != null && !newProfile.getProfileImageBase64().isBlank()) {
                    user.setProfilePicture(newProfile.getProfileImageBase64());
                }
                session.update(user);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        finally {
            session.close();
        }

    }

    public static User getUserById(int id) {
        Transaction transaction = null;
        User user = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            user = (User) session.get(User.class, id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return user;

    }

    public static boolean IsPhoneNumberTaken(String phoneNumber) {
        boolean res = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();
            Long count = session.createQuery(
                            "select count(u) from User u where u.phoneNumber = :phoneNumber", Long.class)
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

    public static boolean IsEmailTaken(String email) {
        if(email == null || email.isEmpty()) {
            return false;
        }
        boolean res = false;
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();
            Long count = session.createQuery(
                            "select count(u) from User u where u.email = :email", Long.class)
                    .setParameter("email", email)
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

    public static User getUserByPhone(String phoneNumber) {
        Transaction transaction = null;
        User user = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            user = session.createQuery("from User where phoneNumber = :phoneNumber", User.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
            transaction.rollback();
            }
            e.printStackTrace();
        }
        return user;
    }

    public static void addRestaurantToFavorites(int userId, int restaurantId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer user = session.get(Customer.class, userId);
            if(user == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if(restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "Restaurant not found");
            }

            user.addFavoriteRestaurant(restaurant);
            restaurant.addLikedCustomer(user);
            session.merge(restaurant);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }

    public static void deleteRestaurantFromFavorites(int userId, int restaurantId, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer user = session.get(Customer.class, userId);
            if(user == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if(restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "Restaurant not found");
            }
            if(!(user.getFavoriteRestaurants().contains(restaurant))) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchRestaurant(restaurantId + "Restaurant not found in Favorites");
            }
            user.removeRestaurantFromFavorites(restaurant);
            restaurant.removeCustomerFromLiked(user);
            session.merge(restaurant);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }

    public static List<Restaurant> getFavoriteRestaurants(int userId, HttpExchange exchange) {
        List<Restaurant> favorites = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Customer user = session.get(Customer.class, userId);
            if(user == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(user.getFavoriteRestaurants());
            favorites = user.getFavoriteRestaurants();

        } catch (Exception e){
            e.printStackTrace();
        }
        return favorites;
    }

    public static Wallet getWalletByUserId(int userId, HttpExchange exchange) {
        Wallet wallet = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Customer user = session.get(Customer.class, userId);
            if(user == null) {
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "User not found");
            }
            Hibernate.initialize(user.getWallet());
            wallet = user.getWallet();
        } catch (Exception e){
            e.printStackTrace();
        }
        return wallet;
    }

    public static List<ProfileDto> getListOfUsers(HttpExchange exchange) {
        List<ProfileDto> result = new ArrayList<>();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<User> users = session.createQuery("FROM User u WHERE u.phoneNumber != :phone", User.class)
                    .setParameter("phone", "09122593542")
                    .list();

            if(!users.isEmpty()) {
                for(User user : users) {
                    result.add(user.getProfile());
                }
            }
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return result;
    }

    public static void ChangeUserStatus(int userId, ApprovalStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            ((NeedApproval) user).changeStatus(status);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }
}

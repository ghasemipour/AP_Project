package com.ap.project.dao;

import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.user.HasAddress;
import com.ap.project.entity.user.HasBankAccount;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    public static void updateUser(String id, ProfileDto newProfile) {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try
        {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user != null) {
                if(newProfile.getFull_name() != null) {
                    user.setName(newProfile.getFull_name());
                }
                if(newProfile.getPhone() != null) {
                    user.setPhoneNumber(newProfile.getPhone());
                }
                if(newProfile.getEmail() != null) {
                    user.setEmail(newProfile.getEmail());
                }
                if(newProfile.getAddress() != null) {
                    if(user instanceof HasAddress) {
                        ((HasAddress)user).setAddress(newProfile.getAddress());
                    }
                }
                if(newProfile.getBank_info().getBank_name() != null) {
                    if(user instanceof HasBankAccount)
                    {
                        ((HasBankAccount)user).getBankAccount().setBankName(newProfile.getBank_info().getBank_name());
                    }
                }
                if(newProfile.getBank_info().getAccount_number() != null) {
                    if(user instanceof HasBankAccount)
                    {
                        ((HasBankAccount)user).getBankAccount().setAccountNumber(newProfile.getBank_info().getAccount_number());
                    }
                }
                if(newProfile.getDiscription() != null) {
                    if(user instanceof Seller)
                    {
                        ((Seller)user).setDiscription(newProfile.getDiscription());
                    }
                }

                if(newProfile.getBrandInfo() != null) {
                    if(user instanceof Seller)
                    {
                        ((Seller)user).setBrandInfo(newProfile.getBrandInfo());
                    }
                }

                if(newProfile.getProfileImageBase64() != null) {
                    user.setProfilePicture(newProfile.getProfileImageBase64());
                }
                session.update(user);
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

    public static User getUserById(String id) {
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
}

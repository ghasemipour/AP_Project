package com.ap.project.dao;

import com.ap.project.dto.CouponDto;
import com.ap.project.entity.general.Coupon;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.text.DateFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ap.project.dao.FoodItemDao.transactionRollBack;

public class CouponDao {
    public static void saveCoupon(Coupon coupon) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(coupon);
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }

    }

    public static List<CouponDto> getListOfCoupons(HttpExchange exchange) {
        List<CouponDto> result = new ArrayList<>();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            List<Coupon> coupons = session.createQuery("from Coupon").list();
            for (Coupon coupon : coupons) {
                CouponDto couponDto = coupon.getCouponDto();
                result.add(couponDto);
            }
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return result;
    }

    public static Coupon getCouponById(int id) {
        Transaction transaction = null;
        Coupon coupon = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            coupon = session.get(Coupon.class, id);
            transaction.commit();
        } catch(Exception e){
            transactionRollBack(transaction, e);
        }
        return coupon;
    }

    public static void deleteCouponById(int id) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Coupon coupon = session.get(Coupon.class, id);
            session.remove(coupon);
            transaction.commit();
        } catch(Exception e){
            transactionRollBack(transaction, e);
        }
    }

    public static void updateCoupon(int id, CouponDto newCoupon) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Coupon coupon = session.get(Coupon.class, id);
            if(newCoupon.getCouponCode() != null){
                coupon.setCouponCode(newCoupon.getCouponCode());
            }
            if(newCoupon.getType() != null){
                coupon.setType(newCoupon.getType());
            }
            if(newCoupon.getValue() != null){
                coupon.setValue(newCoupon.getValue());
            }
            if(newCoupon.getMinPrice() != null){
                coupon.setMinPrice(newCoupon.getMinPrice());
            }
            if(newCoupon.getUserCount() != null){
                coupon.setUserCount(newCoupon.getUserCount());
            }
            if(newCoupon.getStartDate() != null){
                coupon.setStartDate(newCoupon.getStartDate());
            }
            if(newCoupon.getEndDate() != null){
                coupon.setEndDate(newCoupon.getEndDate());
            }
            session.update(coupon);
            transaction.commit();
        }catch (Exception e){
            transactionRollBack(transaction, e);
        }
    }

    public static boolean isCouponCodeTaken(String couponCode) {
        Transaction transaction = null;
        boolean res = false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Long count = (Long) session.createQuery("select count(c) from Coupon c WHERE c.couponCode =: couponCode")
                    .setParameter("couponCode", couponCode)
                    .uniqueResult();

            if(count > 0)
                res = true;
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return res;
    }

    public static Coupon getCouponByCouponCode(String couponCode) {
        Coupon coupon = null;
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            coupon = session.createQuery("FROM Coupon c WHERE c.couponCode = :code", Coupon.class)
                    .setParameter("code", couponCode)
                    .uniqueResult();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return coupon;
    }

    public static boolean isCouponValid(String couponCode) {
        boolean res = false;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Coupon coupon = session.createQuery("FROM Coupon c WHERE c.couponCode = :code", Coupon.class)
                    .setParameter("code", couponCode)
                    .uniqueResult();
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String date = localDate.format(formatter);
            
            if(coupon.getCouponCode() != null && (date.compareTo(coupon.getStartDate()) >= 0 && date.compareTo(coupon.getEndDate()) <= 0)){
                res = true;
            }
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return res;
    }
}

package com.ap.project.dao;

import com.ap.project.dto.CouponDto;
import com.ap.project.entity.general.Coupon;
import com.ap.project.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
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
            }
            transaction.commit();
        } catch (Exception e){
            transactionRollBack(transaction, e);
        }
        return result;
    }
}

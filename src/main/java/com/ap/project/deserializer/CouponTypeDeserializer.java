package com.ap.project.deserializer;

import com.ap.project.Enums.CouponType;
import com.ap.project.Enums.UserRole;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CouponTypeDeserializer implements JsonDeserializer<CouponType> {
    @Override
    public CouponType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String couponType = jsonElement.getAsString();
        try {
            return CouponType.fromString(couponType);
        }catch (IllegalArgumentException e) {
            throw new JsonParseException("invalid Coupon type role: " + couponType);
        }
    }
}

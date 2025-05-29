package com.ap.project.deserializer;

import com.ap.project.Enums.UserRole;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UserRoleDeserializer implements JsonDeserializer<UserRole> {
    @Override
    public UserRole deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String roleName = jsonElement.getAsString();
        try {
            return UserRole.fromString(roleName);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid user role: " + roleName);
        }
    }
}

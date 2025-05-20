package com.example.lab.Cache;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryCache {
    private final Map<Long, ConversionDTO> conversionCache = new HashMap<>();
    private final Map<Long, User> userCache = new HashMap<>();

    public void putConversion(Long key, ConversionDTO value) {
        conversionCache.put(key, value);
    }

    public ConversionDTO getConversion(Long key) {
        return conversionCache.get(key);
    }

    public boolean containsConversionKey(Long key) {
        return conversionCache.containsKey(key);
    }

    public void putUser(Long key, User value) {
        userCache.put(key, value);
    }

    public User getUser(Long key) {
        return userCache.get(key);
    }

    public boolean containsUserKey(Long key) {
        return userCache.containsKey(key);
    }

    public void removeUser(Long key) {
        userCache.remove(key);
    }
}
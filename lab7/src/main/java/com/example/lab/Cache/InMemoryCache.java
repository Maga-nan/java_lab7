package com.example.lab.Cache;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryCache {
    private final Map<Long, ConversionDTO> conversionCache = new HashMap<>();
    private final Map<Long, User> userCache = new HashMap<>();
    private final Map<Long, List<ConversionDTO>> userConversionsCache = new HashMap<>();
    private final Map<String, List<ConversionDTO>> listCache = new HashMap<>();

    public void putConversion(Long key, ConversionDTO value) {
        conversionCache.put(key, value);
    }

    public ConversionDTO getConversion(Long key) {
        return conversionCache.get(key);
    }

    public boolean containsConversionKey(Long key) {
        return conversionCache.containsKey(key);
    }

    public void removeConversion(Long key) {
        conversionCache.remove(key);
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

    public void putUserConversions(Long userId, List<ConversionDTO> conversions) {
        userConversionsCache.put(userId, conversions);
    }

    public List<ConversionDTO> getUserConversions(Long userId) {
        return userConversionsCache.get(userId);
    }

    public boolean containsUserConversionsKey(Long userId) {
        return userConversionsCache.containsKey(userId);
    }

    public void removeUserConversions(Long userId) {
        userConversionsCache.remove(userId);
    }

    public void putList(String key, List<ConversionDTO> list) {
        listCache.put(key, list);
    }

    public List<ConversionDTO> getList(String key) {
        return listCache.get(key);
    }

    public boolean containsListKey(String key) {
        return listCache.containsKey(key);
    }

    public void removeList(String key) {
        listCache.remove(key);
    }
}

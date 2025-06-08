package com.example.lab.Service;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Model.ConversionRequest;
import com.example.lab.Model.User;
import com.example.lab.Cache.InMemoryCache;
import com.example.lab.Repository.ConversionRequestRepository;
import com.example.lab.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DateConversionService {

    private final UserRepository userRepository;
    private final ConversionRequestRepository requestRepository;
    private final InMemoryCache cache;
    private final RequestCounterService requestCounterService;

    @Autowired
    public DateConversionService(UserRepository userRepository,
                                 ConversionRequestRepository requestRepository,
                                 InMemoryCache cache,
                                 RequestCounterService requestCounterService) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.cache = cache;
        this.requestCounterService = requestCounterService;
    }

    @Transactional
    public ConversionDTO createConversion(ConversionDTO conversionDTO) {
        requestCounterService.increment();

        User user = null;
        if (conversionDTO.getUserId() != null) {
            if (cache.containsUserKey(conversionDTO.getUserId())) {
                user = cache.getUser(conversionDTO.getUserId());
            } else {
                user = userRepository.findById(conversionDTO.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cache.putUser(user.getId(), user);
            }
        }

        ConversionRequest request = new ConversionRequest();
        request.setTimestamp(conversionDTO.getTimestamp());
        request.setLocalTime(conversionDTO.getLocalTime());
        request.setGmtTime(conversionDTO.getGmtTime());
        request.setRequestTime(Instant.now());
        request.setUser(user);

        ConversionRequest savedRequest = requestRepository.save(request);
        ConversionDTO result = convertToDTO(savedRequest);
        cache.putConversion(result.getId(), result);

        cache.removeList("allConversions");
        if (user != null) {
            cache.removeUserConversions(user.getId());
            cache.removeList("conversionsByUsername_" + user.getUsername());
        }

        return result;
    }

    public List<ConversionDTO> getAllConversions() {
        requestCounterService.increment();

        String cacheKey = "allConversions";

        if (cache.containsListKey(cacheKey)) {
            return cache.getList(cacheKey);
        }

        List<ConversionDTO> allConversions = requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        cache.putList(cacheKey, allConversions);
        allConversions.forEach(dto -> cache.putConversion(dto.getId(), dto));
        return allConversions;
    }

    public ConversionDTO getConversionById(Long id) {
        requestCounterService.increment();

        if (cache.containsConversionKey(id)) {
            return cache.getConversion(id);
        }

        ConversionDTO result = requestRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Conversion not found with id: " + id));

        cache.putConversion(id, result);
        return result;
    }

    @Transactional
    public ConversionDTO updateConversion(Long id, ConversionDTO conversionDTO) {
        requestCounterService.increment();

        ConversionRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversion not found with id: " + id));

        User user = null;
        if (conversionDTO.getUserId() != null) {
            if (cache.containsUserKey(conversionDTO.getUserId())) {
                user = cache.getUser(conversionDTO.getUserId());
            } else {
                user = userRepository.findById(conversionDTO.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cache.putUser(user.getId(), user);
            }
            request.setUser(user);
        }

        request.setTimestamp(conversionDTO.getTimestamp());
        request.setLocalTime(conversionDTO.getLocalTime());
        request.setGmtTime(conversionDTO.getGmtTime());

        ConversionRequest updatedRequest = requestRepository.save(request);
        ConversionDTO result = convertToDTO(updatedRequest);
        cache.putConversion(id, result);

        cache.removeList("allConversions");
        if (user != null) {
            cache.removeUserConversions(user.getId());
            cache.removeList("conversionsByUsername_" + user.getUsername());
        }

        return result;
    }

    @Transactional
    public void deleteConversion(Long id) {
        requestCounterService.increment();

        ConversionRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversion not found with id: " + id));

        if (request.getUser() != null) {
            cache.removeUserConversions(request.getUser().getId());
            cache.removeList("conversionsByUsername_" + request.getUser().getUsername());
        }

        requestRepository.deleteById(id);
        cache.removeConversion(id);
        cache.removeList("allConversions");
    }

    public List<ConversionDTO> getUserConversionHistory(Long userId) {
        requestCounterService.increment();

        if (cache.containsUserConversionsKey(userId)) {
            return cache.getUserConversions(userId);
        }

        List<ConversionDTO> result = requestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        cache.putUserConversions(userId, result);
        return result;
    }

    public List<ConversionDTO> getConversionsByUsername(String username) {
        requestCounterService.increment();

        String cacheKey = "conversionsByUsername_" + username;

        if (cache.containsListKey(cacheKey)) {
            return cache.getList(cacheKey);
        }

        List<ConversionRequest> requests = requestRepository.findByUsername(username);

        List<ConversionDTO> result = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        cache.putList(cacheKey, result);
        result.forEach(dto -> cache.putConversion(dto.getId(), dto));

        return result;
    }

    public ConversionDTO convertTimestamp(long timestamp) {
        requestCounterService.increment();

        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime localTime = instant.atZone(ZoneId.systemDefault());
        ZonedDateTime gmtTime = instant.atZone(ZoneId.of("GMT"));

        ConversionDTO dto = new ConversionDTO();
        dto.setTimestamp(timestamp);
        dto.setLocalTime(localTime.toString());
        dto.setGmtTime(gmtTime.toString());
        dto.setRequestTime(Instant.now());

        return dto;
    }

    public ConversionDTO convertAndSave(long timestamp, Long userId) {
        requestCounterService.increment();

        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime localTime = instant.atZone(ZoneId.systemDefault());
        ZonedDateTime gmtTime = instant.atZone(ZoneId.of("GMT"));

        ConversionDTO dto = new ConversionDTO();
        dto.setTimestamp(timestamp);
        dto.setLocalTime(localTime.toString());
        dto.setGmtTime(gmtTime.toString());
        dto.setRequestTime(Instant.now());

        if (userId != null) {
            User user;
            if (cache.containsUserKey(userId)) {
                user = cache.getUser(userId);
            } else {
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cache.putUser(userId, user);
            }

            ConversionRequest request = new ConversionRequest();
            request.setTimestamp(dto.getTimestamp());
            request.setLocalTime(dto.getLocalTime());
            request.setGmtTime(dto.getGmtTime());
            request.setRequestTime(dto.getRequestTime());
            request.setUser(user);

            ConversionRequest saved = requestRepository.save(request);
            ConversionDTO savedDto = convertToDTO(saved);
            cache.putConversion(savedDto.getId(), savedDto);

            cache.removeUserConversions(userId);
            cache.removeList("allConversions");
            cache.removeList("conversionsByUsername_" + user.getUsername());

            return savedDto;
        }

        return dto;
    }

    private ConversionDTO convertToDTO(ConversionRequest request) {
        ConversionDTO dto = new ConversionDTO();
        dto.setId(request.getId());
        dto.setTimestamp(request.getTimestamp());
        dto.setLocalTime(request.getLocalTime());
        dto.setGmtTime(request.getGmtTime());
        dto.setRequestTime(request.getRequestTime());
        if (request.getUser() != null) {
            dto.setUserId(request.getUser().getId());
        }
        return dto;
    }
}

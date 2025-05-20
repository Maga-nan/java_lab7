package com.example.lab.Service;

import com.example.lab.Cache.InMemoryCache;
import com.example.lab.DTO.ConversionDTO;
import com.example.lab.model.ConversionRequest;
import com.example.lab.model.User;
import com.example.lab.Repository.ConversionRequestRepository;
import com.example.lab.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DateConversionService {

    private final UserRepository userRepository;
    private final ConversionRequestRepository requestRepository;
    private final InMemoryCache cache;
    private final RequestCounterService requestCounter;

    @Autowired
    public DateConversionService(UserRepository userRepository,
                                 ConversionRequestRepository requestRepository,
                                 InMemoryCache cache,
                                 RequestCounterService requestCounterService) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.cache = cache;
        this.requestCounter = requestCounterService;
    }

    public ConversionDTO convertTimestamp(long timestamp) {
        requestCounter.increment();

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
        requestCounter.increment();

        if (cache.containsConversionKey(timestamp)) {
            return cache.getConversion(timestamp);
        }

        ConversionDTO dto = convertTimestamp(timestamp);
        cache.putConversion(timestamp, dto);

        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);
            user.ifPresent(u -> saveConversionRequest(dto, u));
        }

        return dto;
    }

    private void saveConversionRequest(ConversionDTO dto, User user) {
        ConversionRequest request = new ConversionRequest();
        request.setTimestamp(dto.getTimestamp());
        request.setLocalTime(dto.getLocalTime());
        request.setGmtTime(dto.getGmtTime());
        request.setRequestTime(dto.getRequestTime());
        request.setUser(user);

        requestRepository.save(request);
    }

    public List<ConversionDTO> getUserConversionHistory(Long userId) {
        requestCounter.increment();

        return requestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ConversionRequest> getRequestsByUsername(String username) {
        requestCounter.increment();

        return requestRepository.findByUsername(username);
    }

    private ConversionDTO convertToDTO(ConversionRequest request) {
        ConversionDTO dto = new ConversionDTO();
        dto.setTimestamp(request.getTimestamp());
        dto.setLocalTime(request.getLocalTime());
        dto.setGmtTime(request.getGmtTime());
        dto.setRequestTime(request.getRequestTime());
        return dto;
    }
}

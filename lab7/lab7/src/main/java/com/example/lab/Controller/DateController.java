package com.example.lab.Controller;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Service.DateConversionService;
import com.example.lab.model.ConversionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/date")
public class DateController {

    private final DateConversionService conversionService;

    @Autowired
    public DateController(DateConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/convert")
    public ConversionDTO convertDate(
            @RequestParam long timestamp,
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            return conversionService.convertAndSave(timestamp, userId);
        }
        return conversionService.convertTimestamp(timestamp);
    }

    @GetMapping("/users/{userId}/requests")
    public List<ConversionDTO> getUserConversionHistory(@PathVariable Long userId) {
        return conversionService.getUserConversionHistory(userId);
    }

    @GetMapping("/requests/username/{username}")
    public List<ConversionRequest> getRequestsByUsername(@PathVariable String username) {
        return conversionService.getRequestsByUsername(username);
    }
}
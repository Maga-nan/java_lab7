package com.example.lab.Controller;

import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Service.DateConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/conversions")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversionDTO createConversion(@RequestBody ConversionDTO conversionDTO) {
        return conversionService.createConversion(conversionDTO);
    }

    @GetMapping("/conversions")
    public List<ConversionDTO> getAllConversions() {
        return conversionService.getAllConversions();
    }

    @GetMapping("/conversions/{id}")
    public ConversionDTO getConversionById(@PathVariable Long id) {
        return conversionService.getConversionById(id);
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

    @PutMapping("/conversions/{id}")
    public ConversionDTO updateConversion(
            @PathVariable Long id,
            @RequestBody ConversionDTO conversionDTO) {
        return conversionService.updateConversion(id, conversionDTO);
    }

    @DeleteMapping("/conversions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversion(@PathVariable Long id) {
        conversionService.deleteConversion(id);
    }
    @GetMapping("/user/search")
    public ResponseEntity<List<ConversionDTO>> getConversionsByUsername(@RequestParam String username) {
        List<ConversionDTO> conversions = conversionService.getConversionsByUsername(username);
        if (conversions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conversions);
    }

}
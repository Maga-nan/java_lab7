package com.example.lab.DTO;

import lombok.Data;
import java.time.Instant;

@Data
public class ConversionDTO {
    private Long id;
    private long timestamp;
    private String localTime;
    private String gmtTime;
    private Instant requestTime;
    private Long userId;
}

package com.example.lab.DTO;

import lombok.Data;
import java.time.Instant;

@Data

public class ConversionDTO {
    private long timestamp;
    private String localTime;
    private String gmtTime;
    private Instant requestTime;
}
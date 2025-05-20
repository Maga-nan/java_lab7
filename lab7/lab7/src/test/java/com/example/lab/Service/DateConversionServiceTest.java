package com.example.lab.Service;

import com.example.lab.Cache.InMemoryCache;
import com.example.lab.DTO.ConversionDTO;
import com.example.lab.Repository.ConversionRequestRepository;
import com.example.lab.Repository.UserRepository;
import com.example.lab.model.ConversionRequest;
import com.example.lab.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateConversionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversionRequestRepository requestRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private DateConversionService service;

    private final long testTimestamp = 1716000000000L;

    @Mock
    private User mockUser;

    @Mock
    private ConversionRequest mockRequest;

    @Mock
    private ConversionDTO mockCachedDTO;

    @BeforeEach
    void setup() {
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("mockUser");
        when(mockRequest.getTimestamp()).thenReturn(testTimestamp);
        when(mockRequest.getLocalTime()).thenReturn("mockLocal");
        when(mockRequest.getGmtTime()).thenReturn("mockGMT");
        when(mockRequest.getRequestTime()).thenReturn(Instant.now());
        when(mockCachedDTO.getTimestamp()).thenReturn(testTimestamp);
    }

    @Test
    void testConvertTimestampNotNullFields() {
        ConversionDTO dto = service.convertTimestamp(testTimestamp);
        assertNotNull(dto.getTimestamp());
        assertNotNull(dto.getLocalTime());
        assertNotNull(dto.getGmtTime());
        assertNotNull(dto.getRequestTime());
    }

    @Test
    void testConvertAndSaveFromCache() {
        when(cache.containsConversionKey(testTimestamp)).thenReturn(true);
        when(cache.getConversion(testTimestamp)).thenReturn(mockCachedDTO);

        ConversionDTO result = service.convertAndSave(testTimestamp, null);

        assertEquals(testTimestamp, result.getTimestamp());
        verify(cache, never()).putConversion(anyLong(), any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testConvertAndSaveWithUser() {
        when(cache.containsConversionKey(testTimestamp)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ConversionDTO result = service.convertAndSave(testTimestamp, 1L);

        assertEquals(testTimestamp, result.getTimestamp());
        verify(cache).putConversion(eq(testTimestamp), any());
        verify(requestRepository).save(any(ConversionRequest.class));
    }

    @Test
    void testConvertAndSaveWithoutUser() {
        when(cache.containsConversionKey(testTimestamp)).thenReturn(false);

        ConversionDTO result = service.convertAndSave(testTimestamp, null);

        assertEquals(testTimestamp, result.getTimestamp());
        verify(cache).putConversion(eq(testTimestamp), any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testGetUserConversionHistory() {
        when(requestRepository.findByUserId(1L)).thenReturn(List.of(mockRequest));

        List<ConversionDTO> result = service.getUserConversionHistory(1L);

        assertEquals(1, result.size());
        assertEquals(testTimestamp, result.get(0).getTimestamp());
        assertEquals("mockLocal", result.get(0).getLocalTime());
    }

    @Test
    void testGetRequestsByUsername() {
        when(requestRepository.findByUsername("mockUser")).thenReturn(List.of(mockRequest));

        List<ConversionRequest> result = service.getRequestsByUsername("mockUser");

        assertEquals(1, result.size());
        assertEquals(testTimestamp, result.get(0).getTimestamp());
    }
}

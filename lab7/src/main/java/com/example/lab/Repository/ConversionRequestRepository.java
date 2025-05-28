package com.example.lab.Repository;

import com.example.lab.Model.ConversionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversionRequestRepository extends JpaRepository<ConversionRequest, Long> {
    List<ConversionRequest> findByUserId(Long userId);

    @Query("SELECT cr FROM ConversionRequest cr WHERE cr.user.username = :username")
    List<ConversionRequest> findByUsername(@Param("username") String username);
}
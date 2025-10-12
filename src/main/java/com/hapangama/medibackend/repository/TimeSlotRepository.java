package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByProviderIdAndAvailableTrueAndStartTimeAfter(
            Long providerId, LocalDateTime after);
    List<TimeSlot> findByProviderIdAndAvailableTrueAndStartTimeBetween(
            Long providerId, LocalDateTime start, LocalDateTime end);
}

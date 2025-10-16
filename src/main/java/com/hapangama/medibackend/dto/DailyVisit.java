package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyVisit {
    private LocalDate date;
    private Long visitCount;
    private Long confirmedCount;
    private Long cancelledCount;
}

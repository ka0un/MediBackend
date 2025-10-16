package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentBreakdown {
    private String department;
    private Long totalAppointments;
    private Long confirmedAppointments;
    private BigDecimal revenue;
    private Double completionRate;
}

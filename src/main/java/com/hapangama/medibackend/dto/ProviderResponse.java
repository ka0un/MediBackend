package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.HealthcareProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {
    private Long id;
    private String name;
    private String specialty;
    private String hospitalName;
    private HealthcareProvider.HospitalType hospitalType;
}

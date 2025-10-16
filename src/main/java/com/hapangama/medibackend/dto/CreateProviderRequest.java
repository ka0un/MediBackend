package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.HealthcareProvider;
import lombok.Data;

@Data
public class CreateProviderRequest {
    private String name;
    private String specialty;
    private String hospitalName;
    private HealthcareProvider.HospitalType hospitalType;
}

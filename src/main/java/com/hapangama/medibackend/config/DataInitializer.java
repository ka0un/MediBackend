package com.hapangama.medibackend.config;

import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final HealthcareProviderRepository providerRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Override
    public void run(String... args) {
        initializeData();
    }

    private void initializeData() {
        // Create sample patients
        Patient patient1 = new Patient();
        patient1.setName("John Doe");
        patient1.setEmail("john.doe@example.com");
        patient1.setPhone("+1234567890");
        patient1.setDigitalHealthCardNumber("DHC-2024-001");
        patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setName("Jane Smith");
        patient2.setEmail("jane.smith@example.com");
        patient2.setPhone("+1234567891");
        patient2.setDigitalHealthCardNumber("DHC-2024-002");
        patientRepository.save(patient2);

        // Create government hospital provider
        HealthcareProvider govProvider1 = new HealthcareProvider();
        govProvider1.setName("Dr. Sarah Williams");
        govProvider1.setSpecialty("Cardiology");
        govProvider1.setHospitalName("City Government Hospital");
        govProvider1.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        govProvider1 = providerRepository.save(govProvider1);

        HealthcareProvider govProvider2 = new HealthcareProvider();
        govProvider2.setName("Dr. Michael Brown");
        govProvider2.setSpecialty("General Medicine");
        govProvider2.setHospitalName("City Government Hospital");
        govProvider2.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);
        govProvider2 = providerRepository.save(govProvider2);

        // Create private hospital providers
        HealthcareProvider privateProvider1 = new HealthcareProvider();
        privateProvider1.setName("Dr. Emily Davis");
        privateProvider1.setSpecialty("Dermatology");
        privateProvider1.setHospitalName("Advanced Private Clinic");
        privateProvider1.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);
        privateProvider1 = providerRepository.save(privateProvider1);

        HealthcareProvider privateProvider2 = new HealthcareProvider();
        privateProvider2.setName("Dr. Robert Johnson");
        privateProvider2.setSpecialty("Orthopedics");
        privateProvider2.setHospitalName("Elite Medical Center");
        privateProvider2.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);
        privateProvider2 = providerRepository.save(privateProvider2);

        // Create time slots for government providers
        createTimeSlots(govProvider1, LocalDateTime.now().plusDays(1), 5);
        createTimeSlots(govProvider2, LocalDateTime.now().plusDays(1), 5);

        // Create time slots for private providers
        createTimeSlots(privateProvider1, LocalDateTime.now().plusDays(1), 5);
        createTimeSlots(privateProvider2, LocalDateTime.now().plusDays(1), 5);

        System.out.println("Sample data initialized successfully!");
        System.out.println("- 2 Patients created");
        System.out.println("- 4 Healthcare Providers created (2 Government, 2 Private)");
        System.out.println("- 20 Time Slots created");
    }

    private void createTimeSlots(HealthcareProvider provider, LocalDateTime startDate, int count) {
        for (int i = 0; i < count; i++) {
            TimeSlot slot = new TimeSlot();
            slot.setProvider(provider);
            slot.setStartTime(startDate.plusHours(i * 2));
            slot.setEndTime(startDate.plusHours(i * 2 + 1));
            slot.setAvailable(true);
            timeSlotRepository.save(slot);
        }
    }
}

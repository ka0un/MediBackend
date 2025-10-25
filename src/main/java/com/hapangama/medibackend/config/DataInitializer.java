package com.hapangama.medibackend.config;

import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import com.hapangama.medibackend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final HealthcareProviderRepository providerRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final TestResultRepository testResultRepository;
    private final VaccinationRepository vaccinationRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initializeData();
    }

    private void initializeData() {
        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setActive(true);
        adminUser = userRepository.save(adminUser);

        // Create user for patient 1
        User user1 = new User();
        user1.setUsername("john.doe");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setRole(User.Role.PATIENT);
        user1.setActive(true);
        user1 = userRepository.save(user1);

        // Create sample patients with complete information
        Patient patient1 = new Patient();
        patient1.setUser(user1);
        patient1.setName("John Doe");
        patient1.setEmail("john.doe@example.com");
        patient1.setPhone("+94703802880");
        patient1.setDigitalHealthCardNumber("DHC-2024-001");
        patient1.setAddress("123 Main Street, City");
        patient1.setDateOfBirth(LocalDate.of(1985, 5, 15));
        patient1.setBloodType("O+");
        patient1.setAllergies("Penicillin");
        patient1.setMedicalHistory("Hypertension diagnosed in 2018");
        patient1.setEmergencyContactName("Jane Doe");
        patient1.setEmergencyContactPhone("+94771234568");
        patient1 = patientRepository.save(patient1);

        // Create user for patient 2
        User user2 = new User();
        user2.setUsername("jane.smith");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setRole(User.Role.PATIENT);
        user2.setActive(true);
        user2 = userRepository.save(user2);

        Patient patient2 = new Patient();
        patient2.setUser(user2);
        patient2.setName("Jane Smith");
        patient2.setEmail("jane.smith@example.com");
        patient2.setPhone("+94703802880");
        patient2.setDigitalHealthCardNumber("DHC-2024-002");
        patient2.setAddress("456 Oak Avenue, Town");
        patient2.setDateOfBirth(LocalDate.of(1990, 8, 20));
        patient2.setBloodType("A+");
        patient2.setAllergies("None");
        patient2.setMedicalHistory("Type 2 Diabetes since 2020");
        patient2.setEmergencyContactName("John Smith");
        patient2.setEmergencyContactPhone("+94771234570");
        patient2 = patientRepository.save(patient2);

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

        // Create sample appointments with past dates for reports
        createSampleAppointments(patient1, patient2, govProvider1, govProvider2, privateProvider1, privateProvider2);

        // Initialize medical records for patient1
        initializeMedicalRecords(patient1, govProvider1);
        
        // Initialize medical records for patient2
        initializeMedicalRecords(patient2, privateProvider1);

        System.out.println("Sample data initialized successfully!");
        System.out.println("- Admin user created (username: admin, password: admin)");
        System.out.println("- 2 Patient users created (john.doe/password123, jane.smith/password123)");
        System.out.println("- 2 Patients created with complete medical records");
        System.out.println("- 4 Healthcare Providers created (2 Government, 2 Private)");
        System.out.println("- 20 Time Slots created");
        System.out.println("- Sample medications, prescriptions, test results, and vaccinations created");
    }

    private void initializeMedicalRecords(Patient patient, HealthcareProvider provider) {
        // Create sample medications
        Medication med1 = new Medication();
        med1.setPatient(patient);
        med1.setMedicationName("Lisinopril");
        med1.setDosage("10mg");
        med1.setFrequency("Once daily");
        med1.setStartDate(LocalDate.now().minusMonths(6));
        med1.setPrescribedBy("Dr. " + provider.getName());
        med1.setActive(true);
        medicationRepository.save(med1);

        Medication med2 = new Medication();
        med2.setPatient(patient);
        med2.setMedicationName("Metformin");
        med2.setDosage("500mg");
        med2.setFrequency("Twice daily");
        med2.setStartDate(LocalDate.now().minusMonths(3));
        med2.setPrescribedBy("Dr. " + provider.getName());
        med2.setActive(true);
        medicationRepository.save(med2);

        // Create sample prescriptions
        Prescription presc1 = new Prescription();
        presc1.setPatient(patient);
        presc1.setPrescribedBy("Dr. " + provider.getName());
        presc1.setPrescriptionDate(LocalDateTime.now().minusMonths(2));
        presc1.setDiagnosis("Hypertension management");
        presc1.setTreatment("Continue current medication, monitor blood pressure");
        presc1.setMedications("Lisinopril 10mg once daily");
        presc1.setNotes("Patient responding well to treatment");
        presc1.setFollowUpDate(LocalDateTime.now().plusMonths(3));
        prescriptionRepository.save(presc1);

        // Create sample test results
        TestResult test1 = new TestResult();
        test1.setPatient(patient);
        test1.setTestName("Blood Pressure");
        test1.setTestDate(LocalDate.now().minusDays(7));
        test1.setResult("120/80");
        test1.setResultUnit("mmHg");
        test1.setReferenceRange("< 120/80");
        test1.setOrderedBy("Dr. " + provider.getName());
        test1.setPerformedBy("Nurse Johnson");
        test1.setNotes("Normal blood pressure reading");
        testResultRepository.save(test1);

        TestResult test2 = new TestResult();
        test2.setPatient(patient);
        test2.setTestName("HbA1c");
        test2.setTestDate(LocalDate.now().minusDays(14));
        test2.setResult("6.2");
        test2.setResultUnit("%");
        test2.setReferenceRange("< 5.7%");
        test2.setOrderedBy("Dr. " + provider.getName());
        test2.setPerformedBy("Lab Technician");
        test2.setNotes("Slightly elevated, continue monitoring");
        testResultRepository.save(test2);

        // Create sample vaccinations
        Vaccination vac1 = new Vaccination();
        vac1.setPatient(patient);
        vac1.setVaccineName("Influenza");
        vac1.setVaccinationDate(LocalDate.now().minusMonths(6));
        vac1.setBatchNumber("FLU2024-001");
        vac1.setManufacturer("Pfizer");
        vac1.setAdministeredBy("Nurse Williams");
        vac1.setNextDoseDate(LocalDate.now().plusMonths(6));
        vac1.setNotes("Annual flu vaccine");
        vaccinationRepository.save(vac1);

        Vaccination vac2 = new Vaccination();
        vac2.setPatient(patient);
        vac2.setVaccineName("COVID-19 Booster");
        vac2.setVaccinationDate(LocalDate.now().minusMonths(3));
        vac2.setBatchNumber("COVID2024-XYZ");
        vac2.setManufacturer("Moderna");
        vac2.setAdministeredBy("Nurse Smith");
        vac2.setNotes("Booster dose administered");
        vaccinationRepository.save(vac2);
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

    private void createSampleAppointments(Patient patient1, Patient patient2,
                                          HealthcareProvider govProvider1, HealthcareProvider govProvider2,
                                          HealthcareProvider privateProvider1, HealthcareProvider privateProvider2) {
        // Create appointments over the past 30 days with various statuses
        LocalDateTime now = LocalDateTime.now();
        
        // Past appointments for patient1
        createAppointment(patient1, govProvider1, now.minusDays(25), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient1, govProvider1, now.minusDays(20), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient1, govProvider2, now.minusDays(15), Appointment.AppointmentStatus.CONFIRMED);
        createAppointment(patient1, privateProvider1, now.minusDays(10), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient1, govProvider1, now.minusDays(5), Appointment.AppointmentStatus.CANCELLED);
        
        // Past appointments for patient2
        createAppointment(patient2, privateProvider1, now.minusDays(28), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient2, privateProvider2, now.minusDays(22), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient2, govProvider2, now.minusDays(18), Appointment.AppointmentStatus.CONFIRMED);
        createAppointment(patient2, privateProvider1, now.minusDays(12), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient2, govProvider1, now.minusDays(7), Appointment.AppointmentStatus.COMPLETED);
        createAppointment(patient2, privateProvider2, now.minusDays(3), Appointment.AppointmentStatus.CONFIRMED);
        
        // Mix of recent appointments
        createAppointment(patient1, govProvider2, now.minusDays(2), Appointment.AppointmentStatus.CONFIRMED);
        createAppointment(patient2, govProvider1, now.minusDays(1), Appointment.AppointmentStatus.PENDING_PAYMENT);
        
        System.out.println("- 13 Sample appointments created (past 30 days)");
    }

    private void createAppointment(Patient patient, HealthcareProvider provider, 
                                   LocalDateTime appointmentTime, Appointment.AppointmentStatus status) {
        // Create a time slot for this appointment
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setProvider(provider);
        timeSlot.setStartTime(appointmentTime);
        timeSlot.setEndTime(appointmentTime.plusHours(1));
        timeSlot.setAvailable(false);
        timeSlot = timeSlotRepository.save(timeSlot);
        
        // Create the appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProvider(provider);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(status);
        appointment.setBookingDateTime(appointmentTime.minusDays(2)); // Booked 2 days before
        appointment.setConfirmationNumber("CONF-" + System.currentTimeMillis() + "-" + patient.getId());
        appointment.setPaymentRequired(provider.getHospitalType() == HealthcareProvider.HospitalType.PRIVATE);
        
        appointmentRepository.save(appointment);
    }
}

package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HealthcareProviderRepository providerRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PaymentRepository paymentRepository;

    public List<ProviderResponse> getProvidersBySpecialty(String specialty) {
        List<HealthcareProvider> providers = specialty != null && !specialty.isEmpty()
                ? providerRepository.findBySpecialty(specialty)
                : providerRepository.findAll();

        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    public List<TimeSlotResponse> getAvailableTimeSlots(Long providerId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<TimeSlot> timeSlots = timeSlotRepository
                .findByProviderIdAndAvailableTrueAndStartTimeBetween(providerId, startOfDay, endOfDay);

        return timeSlots.stream()
                .map(this::mapToTimeSlotResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        // Validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Validate provider
        HealthcareProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Validate and lock time slot
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        // Check if time slot is still available (concurrent booking check)
        if (!timeSlot.getAvailable()) {
            throw new RuntimeException("Time slot is no longer available");
        }

        // Mark time slot as unavailable
        timeSlot.setAvailable(false);
        timeSlotRepository.save(timeSlot);

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProvider(provider);
        appointment.setTimeSlot(timeSlot);
        appointment.setBookingDateTime(LocalDateTime.now());
        appointment.setConfirmationNumber(generateConfirmationNumber());

        // Determine if payment is required based on hospital type
        boolean paymentRequired = provider.getHospitalType() == HealthcareProvider.HospitalType.PRIVATE;
        appointment.setPaymentRequired(paymentRequired);

        // Set status based on payment requirement
        if (paymentRequired) {
            appointment.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        } else {
            appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        }

        appointment = appointmentRepository.save(appointment);

        return mapToAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse processPayment(PaymentRequest paymentRequest) {
        Appointment appointment = appointmentRepository.findById(paymentRequest.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPaymentRequired()) {
            throw new RuntimeException("Payment not required for this appointment");
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Payment already processed for this appointment");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setTransactionId(UUID.randomUUID().toString());
        
        // Simulate payment processing (in real scenario, integrate with payment gateway)
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        
        paymentRepository.save(payment);

        // Update appointment status
        appointment.setPayment(payment);
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);

        return mapToAppointmentResponse(appointment);
    }

    public AppointmentResponse getAppointmentByConfirmationNumber(String confirmationNumber) {
        Appointment appointment = appointmentRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return mapToAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Appointment not found"));
        
        // Mark time slot as available again
        TimeSlot timeSlot = appointment.getTimeSlot();
        timeSlot.setAvailable(true);
        timeSlotRepository.save(timeSlot);
        
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private String generateConfirmationNumber() {
        return "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatient().getId());
        response.setPatientName(appointment.getPatient().getName());
        response.setProviderId(appointment.getProvider().getId());
        response.setProviderName(appointment.getProvider().getName());
        response.setSpecialty(appointment.getProvider().getSpecialty());
        response.setAppointmentTime(appointment.getTimeSlot().getStartTime());
        response.setConfirmationNumber(appointment.getConfirmationNumber());
        response.setStatus(appointment.getStatus());
        response.setPaymentRequired(appointment.getPaymentRequired());
        response.setHospitalName(appointment.getProvider().getHospitalName());
        return response;
    }

    private TimeSlotResponse mapToTimeSlotResponse(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();
        response.setId(timeSlot.getId());
        response.setProviderId(timeSlot.getProvider().getId());
        response.setProviderName(timeSlot.getProvider().getName());
        response.setStartTime(timeSlot.getStartTime());
        response.setEndTime(timeSlot.getEndTime());
        response.setAvailable(timeSlot.getAvailable());
        return response;
    }

    private ProviderResponse mapToProviderResponse(HealthcareProvider provider) {
        ProviderResponse response = new ProviderResponse();
        response.setId(provider.getId());
        response.setName(provider.getName());
        response.setSpecialty(provider.getSpecialty());
        response.setHospitalName(provider.getHospitalName());
        response.setHospitalType(provider.getHospitalType());
        return response;
    }

    // Healthcare Provider Management
    @Transactional
    public ProviderResponse createProvider(CreateProviderRequest request) {
        HealthcareProvider provider = new HealthcareProvider();
        provider.setName(request.getName());
        provider.setSpecialty(request.getSpecialty());
        provider.setHospitalName(request.getHospitalName());
        provider.setHospitalType(request.getHospitalType());
        
        provider = providerRepository.save(provider);
        return mapToProviderResponse(provider);
    }

    @Transactional
    public ProviderResponse updateProvider(Long providerId, UpdateProviderRequest request) {
        HealthcareProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Provider not found"));
        
        if (request.getName() != null) {
            provider.setName(request.getName());
        }
        if (request.getSpecialty() != null) {
            provider.setSpecialty(request.getSpecialty());
        }
        if (request.getHospitalName() != null) {
            provider.setHospitalName(request.getHospitalName());
        }
        if (request.getHospitalType() != null) {
            provider.setHospitalType(request.getHospitalType());
        }
        
        provider = providerRepository.save(provider);
        return mapToProviderResponse(provider);
    }

    @Transactional
    public void deleteProvider(Long providerId) {
        HealthcareProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Provider not found"));
        providerRepository.delete(provider);
    }

    public ProviderResponse getProviderById(Long providerId) {
        HealthcareProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Provider not found"));
        return mapToProviderResponse(provider);
    }

    // Time Slot Management
    @Transactional
    public TimeSlotResponse createTimeSlot(Long providerId, CreateTimeSlotRequest request) {
        HealthcareProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Provider not found"));
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setProvider(provider);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        
        timeSlot = timeSlotRepository.save(timeSlot);
        return mapToTimeSlotResponse(timeSlot);
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(Long timeSlotId, UpdateTimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Time slot not found"));
        
        if (request.getStartTime() != null) {
            timeSlot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            timeSlot.setEndTime(request.getEndTime());
        }
        if (request.getAvailable() != null) {
            timeSlot.setAvailable(request.getAvailable());
        }
        
        timeSlot = timeSlotRepository.save(timeSlot);
        return mapToTimeSlotResponse(timeSlot);
    }

    @Transactional
    public void deleteTimeSlot(Long timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new com.hapangama.medibackend.exception.NotFoundException("Time slot not found"));
        timeSlotRepository.delete(timeSlot);
    }

    public List<TimeSlotResponse> getTimeSlotsByProvider(Long providerId) {
        List<TimeSlot> timeSlots = timeSlotRepository.findByProviderId(providerId);
        return timeSlots.stream()
                .map(this::mapToTimeSlotResponse)
                .collect(Collectors.toList());
    }
}

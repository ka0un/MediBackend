package com.hapangama.medibackend.service;

import com.hapangama.medibackend.dto.*;
import com.hapangama.medibackend.model.*;
import com.hapangama.medibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private HealthcareProviderRepository providerRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private HealthcareProvider governmentProvider;
    private HealthcareProvider privateProvider;
    private TimeSlot testTimeSlot;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("John Doe");
        testPatient.setEmail("john@example.com");
        testPatient.setPhone("1234567890");
        testPatient.setDigitalHealthCardNumber("DHC12345");

        governmentProvider = new HealthcareProvider();
        governmentProvider.setId(1L);
        governmentProvider.setName("Dr. Smith");
        governmentProvider.setSpecialty("Cardiology");
        governmentProvider.setHospitalName("Government Hospital");
        governmentProvider.setHospitalType(HealthcareProvider.HospitalType.GOVERNMENT);

        privateProvider = new HealthcareProvider();
        privateProvider.setId(2L);
        privateProvider.setName("Dr. Johnson");
        privateProvider.setSpecialty("Dermatology");
        privateProvider.setHospitalName("Private Clinic");
        privateProvider.setHospitalType(HealthcareProvider.HospitalType.PRIVATE);

        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setProvider(governmentProvider);
        testTimeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        testTimeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testTimeSlot.setAvailable(true);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setPatient(testPatient);
        testAppointment.setProvider(governmentProvider);
        testAppointment.setTimeSlot(testTimeSlot);
        testAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        testAppointment.setBookingDateTime(LocalDateTime.now());
        testAppointment.setConfirmationNumber("APT-12345678");
        testAppointment.setPaymentRequired(false);
    }

    @Test
    void testGetProvidersBySpecialty_WithSpecialty() {
        when(providerRepository.findBySpecialty("Cardiology"))
                .thenReturn(Arrays.asList(governmentProvider));

        List<ProviderResponse> result = appointmentService.getProvidersBySpecialty("Cardiology");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        assertEquals("Cardiology", result.get(0).getSpecialty());
        verify(providerRepository, times(1)).findBySpecialty("Cardiology");
    }

    @Test
    void testGetProvidersBySpecialty_WithoutSpecialty() {
        when(providerRepository.findAll())
                .thenReturn(Arrays.asList(governmentProvider, privateProvider));

        List<ProviderResponse> result = appointmentService.getProvidersBySpecialty(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(providerRepository, times(1)).findAll();
    }

    @Test
    void testGetAvailableTimeSlots() {
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        when(timeSlotRepository.findByProviderIdAndAvailableTrueAndStartTimeBetween(
                1L, startOfDay, endOfDay))
                .thenReturn(Arrays.asList(testTimeSlot));

        List<TimeSlotResponse> result = appointmentService.getAvailableTimeSlots(1L, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTimeSlot.getId(), result.get(0).getId());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void testBookAppointment_GovernmentHospital_NoPaymentRequired() {
        BookAppointmentRequest request = new BookAppointmentRequest(1L, 1L, 1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(governmentProvider));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(testTimeSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);

        AppointmentResponse result = appointmentService.bookAppointment(request);

        assertNotNull(result);
        assertEquals(testPatient.getName(), result.getPatientName());
        assertEquals(governmentProvider.getName(), result.getProviderName());
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, result.getStatus());
        assertFalse(result.getPaymentRequired());
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testBookAppointment_PrivateHospital_PaymentRequired() {
        BookAppointmentRequest request = new BookAppointmentRequest(1L, 2L, 1L);
        TimeSlot privateTimeSlot = new TimeSlot();
        privateTimeSlot.setId(1L);
        privateTimeSlot.setProvider(privateProvider);
        privateTimeSlot.setStartTime(LocalDateTime.now().plusDays(1));
        privateTimeSlot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        privateTimeSlot.setAvailable(true);

        Appointment privateAppointment = new Appointment();
        privateAppointment.setId(2L);
        privateAppointment.setPatient(testPatient);
        privateAppointment.setProvider(privateProvider);
        privateAppointment.setTimeSlot(privateTimeSlot);
        privateAppointment.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        privateAppointment.setBookingDateTime(LocalDateTime.now());
        privateAppointment.setConfirmationNumber("APT-87654321");
        privateAppointment.setPaymentRequired(true);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(2L)).thenReturn(Optional.of(privateProvider));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(privateTimeSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(privateAppointment);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(privateTimeSlot);

        AppointmentResponse result = appointmentService.bookAppointment(request);

        assertNotNull(result);
        assertTrue(result.getPaymentRequired());
        assertEquals(Appointment.AppointmentStatus.PENDING_PAYMENT, result.getStatus());
    }

    @Test
    void testBookAppointment_TimeSlotNotAvailable_ThrowsException() {
        BookAppointmentRequest request = new BookAppointmentRequest(1L, 1L, 1L);
        testTimeSlot.setAvailable(false);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(governmentProvider));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(testTimeSlot));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.bookAppointment(request);
        });

        assertEquals("Time slot is no longer available", exception.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testBookAppointment_PatientNotFound_ThrowsException() {
        BookAppointmentRequest request = new BookAppointmentRequest(1L, 1L, 1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.bookAppointment(request);
        });

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void testProcessPayment_Success() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

        testAppointment.setPaymentRequired(true);
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING_PAYMENT);
        testAppointment.setProvider(privateProvider);

        Appointment confirmedAppointment = new Appointment();
        confirmedAppointment.setId(1L);
        confirmedAppointment.setPatient(testPatient);
        confirmedAppointment.setProvider(privateProvider);
        confirmedAppointment.setTimeSlot(testTimeSlot);
        confirmedAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        confirmedAppointment.setBookingDateTime(LocalDateTime.now());
        confirmedAppointment.setConfirmationNumber("APT-12345678");
        confirmedAppointment.setPaymentRequired(true);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(confirmedAppointment);

        AppointmentResponse result = appointmentService.processPayment(paymentRequest);

        assertNotNull(result);
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, result.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testProcessPayment_PaymentNotRequired_ThrowsException() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.processPayment(paymentRequest);
        });

        assertEquals("Payment not required for this appointment", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testGetAppointmentByConfirmationNumber() {
        when(appointmentRepository.findByConfirmationNumber("APT-12345678"))
                .thenReturn(Optional.of(testAppointment));

        AppointmentResponse result = appointmentService.getAppointmentByConfirmationNumber("APT-12345678");

        assertNotNull(result);
        assertEquals("APT-12345678", result.getConfirmationNumber());
        assertEquals(testPatient.getName(), result.getPatientName());
    }

    @Test
    void testGetPatientAppointments() {
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(Arrays.asList(testAppointment));

        List<AppointmentResponse> result = appointmentService.getPatientAppointments(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPatient.getName(), result.get(0).getPatientName());
    }
}

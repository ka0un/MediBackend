package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService{

    private final SmsGateway smsGateway;

    @Override
    public void sendEmailAppointmentConfirmation(Appointment appointment) {

    }

    @Override
    public void sendSmsAppointmentConfirmation(Appointment appointment) {
        smsGateway.sendSms(appointment.getPatient().getPhone(),
                "Dear " + appointment.getPatient().getName() + ",\n\n" +
                "Your appointment has been confirmed.\n" +
                "Date & Time: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(appointment.getBookingDateTime()) + "\n" +
                "Location: " + appointment.getProvider().getHospitalName() + "\n" +
                "Appointment Code: " + appointment.getConfirmationNumber() + "\n\n" +
                "Thank you,\n" +
                appointment.getProvider().getHospitalName() + ".");

    }
}

package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                "Dear " + appointment.getPatient().getName() +
                        ", your appointment is confirmed for " + appointment.getBookingDateTime().toString() +
                        ". Thank you!");

    }
}

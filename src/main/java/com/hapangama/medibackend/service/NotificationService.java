package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.Appointment;

public interface NotificationService {

    public void sendEmailAppointmentConfirmation(Appointment appointment);
    public void sendSmsAppointmentConfirmation(Appointment appointment);
}

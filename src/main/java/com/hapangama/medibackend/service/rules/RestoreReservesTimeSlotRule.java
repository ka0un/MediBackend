package com.hapangama.medibackend.service.rules;

import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.TimeSlot;
import com.hapangama.medibackend.repository.TimeSlotRepository;
import com.hapangama.medibackend.service.AppointmentStatusRule;
import org.springframework.stereotype.Component;

@Component
public class RestoreReservesTimeSlotRule implements AppointmentStatusRule {
    private final TimeSlotRepository timeSlotRepository;

    public RestoreReservesTimeSlotRule(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    public boolean supports(Appointment.AppointmentStatus current, Appointment.AppointmentStatus next) {
        return current == Appointment.AppointmentStatus.CANCELLED && next != Appointment.AppointmentStatus.CANCELLED;
    }

    @Override
    public void apply(Appointment appointment) {
        TimeSlot timeSlot = appointment.getTimeSlot();
        if (timeSlot != null && Boolean.TRUE.equals(timeSlot.getAvailable())) {
            timeSlot.setAvailable(false);
            timeSlotRepository.save(timeSlot);
        }
    }
}

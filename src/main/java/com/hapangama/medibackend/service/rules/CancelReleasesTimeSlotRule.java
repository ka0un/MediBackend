package com.hapangama.medibackend.service.rules;

import com.hapangama.medibackend.model.Appointment;
import com.hapangama.medibackend.model.TimeSlot;
import com.hapangama.medibackend.repository.TimeSlotRepository;
import com.hapangama.medibackend.service.AppointmentStatusRule;
import org.springframework.stereotype.Component;

@Component
public class CancelReleasesTimeSlotRule implements AppointmentStatusRule {
    private final TimeSlotRepository timeSlotRepository;

    public CancelReleasesTimeSlotRule(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    public boolean supports(Appointment.AppointmentStatus current, Appointment.AppointmentStatus next) {
        return next == Appointment.AppointmentStatus.CANCELLED;
    }

    @Override
    public void apply(Appointment appointment) {
        TimeSlot timeSlot = appointment.getTimeSlot();
        if (timeSlot != null && Boolean.FALSE.equals(timeSlot.getAvailable())) {
            timeSlot.setAvailable(true);
            timeSlotRepository.save(timeSlot);
        }
    }
}


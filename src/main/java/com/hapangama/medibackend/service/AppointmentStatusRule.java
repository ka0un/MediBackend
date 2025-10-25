package com.hapangama.medibackend.service;

import com.hapangama.medibackend.model.Appointment;

/**
 * Rule interface to handle side effects when changing appointment status.
 * Enables Open/Closed design by allowing new rules without modifying AppointmentService.
 */
public interface AppointmentStatusRule {
    /**
     * Whether this rule applies for a given transition.
     */
    boolean supports(Appointment.AppointmentStatus current, Appointment.AppointmentStatus next);

    /**
     * Apply the rule's side effects for the transition. Implementations may persist related entities as needed.
     */
    void apply(Appointment appointment);
}


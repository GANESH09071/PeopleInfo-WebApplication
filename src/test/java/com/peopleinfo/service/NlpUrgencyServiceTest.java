package com.peopleinfo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NlpUrgencyServiceTest {

    private NlpUrgencyService urgencyService;

    @BeforeEach
    public void setUp() {
        urgencyService = new NlpUrgencyService();
    }

    @Test
    public void testUrgentPhrases() {
        assertTrue(urgencyService.isUrgent("I had a car accident and I am currently in the hospital."));
        assertTrue(urgencyService.isUrgent("Going to attend my grandmother's funeral tomorrow."));
        assertTrue(urgencyService.isUrgent("Need medical leave due to sudden severe illness."));
    }

    @Test
    public void testNonUrgentPhrases() {
        assertFalse(urgencyService.isUrgent("Going for a family trip to the beach."));
        assertFalse(urgencyService.isUrgent("Routine casual leave to rest at home."));
        assertFalse(urgencyService.isUrgent(null));
        assertFalse(urgencyService.isUrgent(""));
    }

    @Test
    public void testNegations() {
        assertFalse(urgencyService.isUrgent("This is not an emergency, just a routine dentist appointment."));
        assertFalse(urgencyService.isUrgent("Routine medical checkup, no emergency at all."));
    }
}

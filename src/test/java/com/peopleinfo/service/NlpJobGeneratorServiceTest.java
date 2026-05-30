package com.peopleinfo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NlpJobGeneratorServiceTest {

    private NlpJobGeneratorService generatorService;

    @BeforeEach
    public void setUp() {
        generatorService = new NlpJobGeneratorService();
    }

    @Test
    public void testMapExperienceLevelToYears() {
        // We can test this indirectly by calling generateJobDetails and checking requirements content
        GeneratedJobDetails detailsEntry = generatorService.generateJobDetails("Developer", null, "ENTRY");
        assertTrue(detailsEntry.getRequirements().contains("entry-level professional experience"));

        GeneratedJobDetails detailsSenior = generatorService.generateJobDetails("Developer", null, "SENIOR");
        assertTrue(detailsSenior.getRequirements().contains("5+ years of professional experience"));

        GeneratedJobDetails detailsDirectYears = generatorService.generateJobDetails("Developer", 4, "SENIOR");
        assertTrue(detailsDirectYears.getRequirements().contains("4+ years of professional experience"));
    }

    @Test
    public void testSoftwareDeveloperFallback() {
        GeneratedJobDetails details = generatorService.generateJobDetails("Senior Software Engineer", 7, "SENIOR");
        assertNotNull(details);
        assertTrue(details.getDescription().contains("engineering team"));
        assertTrue(details.getDescription().contains("Senior Software Engineer"));
        assertTrue(details.getRequirements().contains("7+ years of"));
        assertTrue(details.getRequirements().contains("programming languages"));
        assertTrue(details.getResponsibilities().contains("Develop, test, and deploy"));
    }

    @Test
    public void testManagerFallback() {
        GeneratedJobDetails details = generatorService.generateJobDetails("Project Manager", 10, "MANAGER");
        assertNotNull(details);
        assertTrue(details.getDescription().contains("direct projects"));
        assertTrue(details.getRequirements().contains("10+ years of"));
        assertTrue(details.getResponsibilities().contains("Supervise daily activities"));
    }

    @Test
    public void testHRFallback() {
        GeneratedJobDetails details = generatorService.generateJobDetails("HR Specialist", 3, "MID");
        assertNotNull(details);
        assertTrue(details.getDescription().contains("HR professional"));
        assertTrue(details.getRequirements().contains("3+ years of"));
        assertTrue(details.getResponsibilities().contains("recruiting"));
    }

    @Test
    public void testDefaultFallback() {
        GeneratedJobDetails details = generatorService.generateJobDetails("Marketing Specialist", 2, "MID");
        assertNotNull(details);
        assertTrue(details.getDescription().contains("Marketing Specialist"));
        assertTrue(details.getRequirements().contains("2+ years of"));
        assertTrue(details.getResponsibilities().contains("Execute day-to-day tasks"));
    }

    @Test
    public void testNullOrBlankTitle() {
        GeneratedJobDetails detailsNull = generatorService.generateJobDetails(null, 2, "MID");
        assertEquals("", detailsNull.getDescription());
        assertEquals("", detailsNull.getRequirements());
        assertEquals("", detailsNull.getResponsibilities());

        GeneratedJobDetails detailsBlank = generatorService.generateJobDetails("   ", 2, "MID");
        assertEquals("", detailsBlank.getDescription());
        assertEquals("", detailsBlank.getRequirements());
        assertEquals("", detailsBlank.getResponsibilities());
    }
}

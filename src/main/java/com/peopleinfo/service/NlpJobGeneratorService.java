package com.peopleinfo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NlpJobGeneratorService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public GeneratedJobDetails generateJobDetails(String jobTitle, Integer yearsOfExperience, String experienceLevel) {
        if (jobTitle == null || jobTitle.isBlank()) {
            return new GeneratedJobDetails("", "", "");
        }

        int years = (yearsOfExperience != null) ? yearsOfExperience : mapExperienceLevelToYears(experienceLevel);

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiApi(jobTitle, years);
            } catch (Exception e) {
                log.warn("Gemini API call failed for job generator, falling back to local templates: {}", e.getMessage());
            }
        } else {
            log.info("No Gemini API key configured. Using local template generator fallback.");
        }

        return generateLocalFallback(jobTitle, years);
    }

    private int mapExperienceLevelToYears(String experienceLevel) {
        if (experienceLevel == null) return 2;
        switch (experienceLevel.toUpperCase()) {
            case "ENTRY": return 0;
            case "JUNIOR": return 1;
            case "MID": return 3;
            case "SENIOR": return 5;
            case "LEAD": return 8;
            case "MANAGER": return 10;
            case "DIRECTOR": return 15;
            default: return 2;
        }
    }

    private GeneratedJobDetails callGeminiApi(String jobTitle, int years) throws Exception {
        String prompt = "You are an HR Assistant. Generate job details for a position with:\n" +
                "Job Title: \"" + jobTitle.replace("\"", "\\\"") + "\"\n" +
                "Years of Experience required: " + years + " years\n\n" +
                "Provide the output in valid JSON format with exactly three fields (keys must be exactly as specified):\n" +
                "- \"description\": A paragraph describing the job and why it's exciting.\n" +
                "- \"requirements\": A bulleted list (one requirement per line, no leading numbers/dashes/asterisks) of qualifications.\n" +
                "- \"responsibilities\": A bulleted list (one responsibility per line, no leading numbers/dashes/asterisks) of day-to-day duties.\n\n" +
                "Respond with only the raw JSON. Do not include markdown code block formatting (like ```json or ```).";

        // Construct JSON payload
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> contents = Map.of("contents", List.of(parts));
        
        String jsonPayload = objectMapper.writeValueAsString(contents);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(12))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                String text = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText()
                        .trim();
                
                // Clean up markdown block wraps if model adds them
                if (text.startsWith("```json")) {
                    text = text.substring(7);
                } else if (text.startsWith("```")) {
                    text = text.substring(3);
                }
                if (text.endsWith("```")) {
                    text = text.substring(0, text.length() - 3);
                }
                text = text.trim();

                JsonNode generatedJson = objectMapper.readTree(text);
                String desc = generatedJson.path("description").asText("").trim();
                String reqs = generatedJson.path("requirements").asText("").trim();
                String resps = generatedJson.path("responsibilities").asText("").trim();

                log.info("Successfully generated job details using Gemini for title: {}", jobTitle);
                return new GeneratedJobDetails(desc, reqs, resps);
            }
        } else {
            throw new RuntimeException("Gemini API returned status code " + response.statusCode() + ": " + response.body());
        }

        throw new RuntimeException("Empty response from Gemini API");
    }

    private GeneratedJobDetails generateLocalFallback(String jobTitle, int years) {
        String lower = jobTitle.toLowerCase();
        String desc;
        String reqs;
        String resps;

        String expStr = years == 0 ? "entry-level" : years + "+ years of";

        if (lower.contains("developer") || lower.contains("engineer") || lower.contains("programmer") || lower.contains("software") || lower.contains("tech")) {
            desc = "We are seeking a talented " + jobTitle + " to join our engineering team. In this role, you will help design, build, and maintain high-performance software applications, collaborating closely with other engineers and product managers to deliver scalable solutions.";
            reqs = expStr + " professional experience in software engineering.\n" +
                   "Strong proficiency in modern programming languages (e.g. Java, JavaScript, Python, C#).\n" +
                   "Experience working with RESTful APIs, relational databases (SQL), and cloud architectures.\n" +
                   "Solid understanding of software design patterns and version control (Git).";
            resps = "Develop, test, and deploy clean, maintainable, and efficient code.\n" +
                    "Collaborate with product designers and business analysts to translate requirements into technical specifications.\n" +
                    "Debug and troubleshoot issues across the application stack.\n" +
                    "Participate in sprint planning, retrospective meetings, and collaborative code reviews.";
        } else if (lower.contains("manager") || lower.contains("lead") || lower.contains("director") || lower.contains("head") || lower.contains("chief")) {
            desc = "We are looking for an experienced " + jobTitle + " to direct projects, lead team execution, and align operational tasks with company milestones. You will serve as a leader and mentor, ensuring that team output meets high standards of quality and efficiency.";
            reqs = expStr + " experience in a leadership or managerial capacity within a related field.\n" +
                   "Proven track record of managing projects, resources, and teams successfully.\n" +
                   "Exceptional leadership, communication, and conflict-resolution skills.\n" +
                   "Strategic mindset with a focus on metrics, project deadlines, and operational efficiency.";
            resps = "Supervise daily activities of the team and provide constructive mentorship.\n" +
                    "Collaborate with department heads and executive stakeholders to set project goals.\n" +
                    "Define key performance indicators (KPIs) and monitor progress toward targets.\n" +
                    "Manage timelines, project scope, and resource allocation to optimize team output.";
        } else if (lower.contains("hr") || lower.contains("human") || lower.contains("recruiter") || lower.contains("talent") || lower.contains("people")) {
            desc = "We are seeking an HR professional to join our team as a " + jobTitle + ". You will play a vital role in executing talent acquisition, administering company policies, and supporting employee onboarding and relations to foster a thriving workplace environment.";
            reqs = expStr + " experience in Human Resources, recruiting, or talent management.\n" +
                   "Solid knowledge of labor legislation, employment guidelines, and HR best practices.\n" +
                   "Outstanding interpersonal, negotiation, and written communication skills.\n" +
                   "Strong ethical standards and the ability to maintain absolute confidentiality.";
            resps = "Manage full-cycle recruiting processes, from sourcing to onboarding new hires.\n" +
                    "Serve as a primary point of contact for employees regarding policies, benefits, and workplace inquiries.\n" +
                    "Assist in organizing performance reviews, training sessions, and employee engagement events.\n" +
                    "Maintain accurate HR database records and ensure compliance with regulatory standards.";
        } else {
            // Default fallback
            desc = "We are seeking a motivated " + jobTitle + " to join our team. In this role, you will perform core operational tasks, collaborate with cross-functional team members, and contribute directly to the successful execution of department initiatives.";
            reqs = expStr + " experience in a similar role or related field.\n" +
                   "Strong organizational and task management skills with attention to detail.\n" +
                   "Proficiency with office productivity tools and standard software platforms.\n" +
                   "Ability to work effectively both independently and as part of a collaborative team.";
            resps = "Execute day-to-day tasks and operational goals associated with the " + jobTitle + " position.\n" +
                    "Collaborate with internal team members to execute project requirements.\n" +
                    "Generate reports, document workflows, and maintain clean project records.\n" +
                    "Identify opportunities for operational efficiency and support continuous improvement efforts.";
        }

        return new GeneratedJobDetails(desc, reqs, resps);
    }
}

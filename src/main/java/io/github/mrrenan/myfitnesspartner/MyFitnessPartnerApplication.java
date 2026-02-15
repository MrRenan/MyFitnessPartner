package io.github.mrrenan.myfitnesspartner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * MyFitnessPartner - Your AI-powered fitness partner via WhatsApp
 * Main application class that bootstraps the Spring Boot application.
 * Features:
 * - WhatsApp Business API integration for messaging
 * - Google Gemini AI for intelligent responses
 * - Calorie calculation and meal tracking
 * - Fitness goal management and progress tracking
 *
 * @author Renan
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class MyFitnessPartnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyFitnessPartnerApplication.class, args);
    }
}
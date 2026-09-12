package com.king.eschool.shared.sms;



import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

@Service
@Slf4j
public class SmsServiceImpl {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.trial-number}")
    private String fromPhoneNumber;

    @Value("${app.front-url}")
    private String appFrontUrl;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Envoie un SMS générique de manière asynchrone via Twilio.
     */
    @Async
    public void sendSms(String candidatePhone, String messageSms) {
        if (candidatePhone == null || candidatePhone.isBlank()) {
            log.warn("Tentative d'envoi de SMS annulée : numéro de téléphone nul ou vide.");
            return;
        }

        try {
            Message message = Message.creator(
                new PhoneNumber(candidatePhone),
                new PhoneNumber(fromPhoneNumber),
                messageSms
            ).create();

            log.info("SMS envoyé avec succès à {} ! SID: {}", candidatePhone, message.getSid());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS Twilio à {}: {}", candidatePhone, e.getMessage());
        }
    }

    /**
     * Envoie la notification SMS d'inscription avec le token de suivi (valide 3 mois).
     */
    @Async
    public void sendEnrollmentNotificationSms(String toPhoneNumber, String registrationNo, String trackingToken) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            return;
        }

        String trackingUrl = appFrontUrl + "/verify-status?token=" + trackingToken;

        String messageBody = String.format(
            "eSchool: Votre demande d'inscription N° %s a été bien reçue.\n" +
            "Suivez votre dossier (accès direct 3 mois) : %s",
            registrationNo,
            trackingUrl
        );

        sendSms(toPhoneNumber, messageBody);
    }
}
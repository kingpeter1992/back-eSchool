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

    // @Value("${app.frontend.url:https://mon-ecole.com}")
    // private String frontendUrl;

       @Value("${app.front-url}")
    private String appFrontUrl;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Envoie la notification SMS au candidat après création de sa demande.
     */
    @Async
    public void sendEnrollmentNotificationSms(String toPhoneNumber, String registrationNo, LocalDate submissionDate) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            return;
        }

        String formattedDate = submissionDate != null ? submissionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String verificationUrl = appFrontUrl + "/verify-status?registrationNo=" + registrationNo;

        String messageBody = String.format(
            "eSchool: Votre demande d'inscription a été enregistrée avec succès.\n" +
            "- N° Dossier: %s\n" +
            "- Date: %s\n" +
            "Suivez l'avancement de votre dossier ici : %s",
            registrationNo,
            formattedDate,
            verificationUrl
        );

        try {
            Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),  // Destinataire
                new PhoneNumber(fromPhoneNumber), // Numéro Twilio
                messageBody
            ).create();

            log.info("SMS envoyé avec succès ! SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS Twilio à {}: {}", toPhoneNumber, e.getMessage());
        }
    }
}
package com.king.eschool.Modules.School.ServiceImplement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Modules.School.Models.Subscription;
import com.king.eschool.Modules.School.Repository.SubscriptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailServiceImpl  emailService;

    // 🟢 Exécution automatique tous les jours à 08h00 du matin (Format CRON : Secondes Minutes Heures Jour Mois JourSemaine)
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkAndSendExpirationReminders() {
        log.info("Démarrage du CRON : Vérification des expirations d'abonnements...");

        // Relances préventives : 30 jours, 15 jours, 7 jours, 1 jour avant
        checkAndSendForDaysRemaining(30);
        checkAndSendForDaysRemaining(15);
        checkAndSendForDaysRemaining(7);
        checkAndSendForDaysRemaining(1);

        // Jour J : Expiration
        checkAndSendForDaysRemaining(0);
    }

    private void checkAndSendForDaysRemaining(int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Subscription> subscriptions = subscriptionRepository.findExpiringSubscriptionsBetween(startOfDay, endOfDay);

        for (Subscription sub : subscriptions) {
            String schoolEmail = sub.getSchool().getEmail();
            String schoolName = sub.getSchool().getName();
            String expirationDateFormatted = sub.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            try {
                if (days == 0) {
                    // Mettre à jour le statut en EXPIRED si arrivé à terme
                    sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(sub);
                    
                    emailService.sendCustomSchoolEmail(
                        schoolEmail,
                        "Expiration de votre abonnement - " + schoolName,
                        "Votre abonnement a expiré aujourd'hui (" + expirationDateFormatted + "). Veuillez le renouveler pour continuer à accéder aux services."
                    );
                } else {
                    emailService.sendCustomSchoolEmail(
                        schoolEmail,
                        "Rappel : Expiration de votre abonnement dans " + days + " jour(s)",
                        "Bonjour " + schoolName + ",\n\nVotre abonnement prendra fin le " + expirationDateFormatted + 
                        " (dans " + days + " jours). Pensez à le renouveler afin d'éviter toute interruption de service."
                    );
                }
                log.info("Email de relance (J-{}) envoyé à l'école {}", days, schoolName);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'email de relance à l'école {}: {}", schoolName, e.getMessage());
            }
        }
    }
}
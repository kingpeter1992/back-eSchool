package com.king.eschool.Core.config;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl {

    // =========================================================
    // SMTP / BREVO
    // =========================================================

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.front-url}")
    private String appFrontUrl;

    // =========================================================
    // CONSTRUCTEUR
    // =========================================================

    public EmailServiceImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;

        log.info(
                "EmailServiceImpl initialisé avec SMTP Brevo.");
    }

    // =========================================================
    // EMAIL ACTIVATION COMPTE
    // =========================================================

    public void sendActivationEmail(
            String toEmail,
            String firstName,
            String activationToken) {

        String activationLink = appFrontUrl
                + "/activate?token="
                + activationToken;

        String subject = "Activation de votre compte - eSchool";

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="fr">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Activation du compte</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background:#f8fafc;
                    font-family:Arial,sans-serif;
                ">

                <div style="
                    max-width:600px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:16px;
                    padding:40px;
                    border:1px solid #e2e8f0;
                ">

                    <h2 style="
                        color:#0f172a;
                        margin-bottom:20px;
                    ">
                        Bienvenue sur eSchool 👋
                    </h2>

                    <p style="
                        color:#475569;
                        font-size:16px;
                        line-height:1.6;
                    ">
                        Bonjour <strong>%s</strong>,
                    </p>

                    <p style="
                        color:#475569;
                        font-size:15px;
                        line-height:1.6;
                    ">
                        Votre compte eSchool vient d'être créé.
                        Pour commencer à utiliser votre espace,
                        veuillez activer votre compte.
                    </p>

                    <div style="
                        text-align:center;
                        margin:35px 0;
                    ">

                        <a href="%s"
                           style="
                           display:inline-block;
                           padding:14px 28px;
                           background:#2563eb;
                           color:#ffffff;
                           text-decoration:none;
                           border-radius:10px;
                           font-weight:bold;
                           ">
                            Activer mon compte
                        </a>

                    </div>

                    <p style="
                        color:#64748b;
                        font-size:13px;
                    ">
                        Ce lien est valable pendant 24 heures.
                    </p>

                    <p style="
                        color:#94a3b8;
                        font-size:12px;
                        margin-top:30px;
                    ">
                        Si vous n'êtes pas à l'origine de cette
                        création de compte, vous pouvez ignorer
                        cet e-mail.
                    </p>

                </div>

                </body>
                </html>
                """
                .formatted(
                        safe(firstName),
                        activationLink);

        sendHtmlEmail(
                toEmail,
                subject,
                htmlContent);
    }

    // =========================================================
    // EMAIL RESET PASSWORD
    // =========================================================

    public void sendPasswordResetEmail(
            String toEmail,
            String firstName,
            String resetToken) {

        String resetLink = appFrontUrl
                + "/reset-password?token="
                + resetToken;

        String subject = "Réinitialisation de votre mot de passe - eSchool";

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="fr">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Réinitialisation du mot de passe</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background:#f8fafc;
                    font-family:Arial,sans-serif;
                ">

                <div style="
                    max-width:600px;
                    margin:40px auto;
                    background:#ffffff;
                    border-radius:16px;
                    padding:40px;
                    border:1px solid #e2e8f0;
                ">

                    <h2 style="
                        color:#0f172a;
                        margin-bottom:20px;
                    ">
                        Réinitialisation du mot de passe
                    </h2>

                    <p style="
                        color:#475569;
                        font-size:15px;
                        line-height:1.6;
                    ">
                        Bonjour <strong>%s</strong>,
                    </p>

                    <p style="
                        color:#475569;
                        font-size:15px;
                        line-height:1.6;
                    ">
                        Une demande de réinitialisation de votre
                        mot de passe a été effectuée.
                    </p>

                    <div style="
                        text-align:center;
                        margin:35px 0;
                    ">

                        <a href="%s"
                           style="
                           display:inline-block;
                           padding:14px 28px;
                           background:#2563eb;
                           color:#ffffff;
                           text-decoration:none;
                           border-radius:10px;
                           font-weight:bold;
                           ">
                            Réinitialiser mon mot de passe
                        </a>

                    </div>

                    <p style="
                        color:#64748b;
                        font-size:13px;
                    ">
                        Ce lien est valable pendant
                        <strong>30 minutes</strong>.
                    </p>

                    <p style="
                        color:#94a3b8;
                        font-size:12px;
                        margin-top:30px;
                    ">
                        Si vous n'avez pas demandé cette
                        réinitialisation, ignorez simplement
                        cet e-mail.
                    </p>

                </div>

                </body>
                </html>
                """
                .formatted(
                        safe(firstName),
                        resetLink);

        sendHtmlEmail(
                toEmail,
                subject,
                htmlContent);
    }

    // =========================================================
    // METHODE GENERIQUE SMTP BREVO
    // =========================================================

    private void sendHtmlEmail(
            String toEmail,
            String subject,
            String htmlContent) {

        validateEmail(toEmail);

        try {

            log.info(
                    "📧 SMTP SEND | from={} | to={} | subject={}",
                    fromEmail,
                    toEmail,
                    subject);

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8");

            helper.setFrom(
                    fromEmail,
                    fromName);

            helper.setTo(toEmail);

            helper.setSubject(subject);

            helper.setText(
                    htmlContent,
                    true);

            mailSender.send(message);

            log.info(
                    "✅ Email envoyé avec succès | destinataire={} | sujet={}",
                    toEmail,
                    subject);

        } catch (MessagingException e) {

            log.error(
                    "❌ ERREUR SMTP | destinataire={} | sujet={} | message={}",
                    toEmail,
                    subject,
                    e.getMessage(),
                    e);

            throw new RuntimeException(
                    "Erreur SMTP lors de l'envoi de l'email : "
                            + e.getMessage(),
                    e);

        } catch (Exception e) {

    log.error(
        "❌ ERREUR SMTP COMPLÈTE | destinataire={} | sujet={}",
        toEmail,
        subject,
        e
    );

    throw new RuntimeException(
        "Erreur SMTP : " + e.getMessage(),
        e
    );

        }
    }

    // =========================================================
    // EMAIL ACTIVATION ABONNEMENT
    // =========================================================

    @Async
    public void sendSubscriptionActivationEmail(
            String toEmail,
            String schoolName,
            String planName,
            String expiresAt) {

        log.info(
                "Envoi email activation abonnement | destinataire={} | école={}",
                toEmail,
                schoolName);

        String subject = "Activation de votre abonnement - "
                + schoolName;

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="fr">

                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    font-family:Arial,sans-serif;
                    background-color:#f8fafc;
                    padding:20px;
                ">

                <div style="
                    max-width:600px;
                    margin:auto;
                    background:white;
                    padding:30px;
                    border-radius:10px;
                    border:1px solid #e2e8f0;
                ">

                    <h2 style="color:#2563eb;">
                        Félicitations ! 🎉
                    </h2>

                    <p style="
                        color:#334155;
                        font-size:16px;
                    ">
                        Votre abonnement au plan
                        <strong>%s</strong>
                        a été activé avec succès.
                    </p>

                    <div style="
                        background:#f1f5f9;
                        padding:15px;
                        border-radius:8px;
                        margin:20px 0;
                    ">

                        <p style="
                            margin:0;
                            color:#475569;
                        ">
                            Date d'expiration :
                            <strong style="color:#0f172a;">
                                %s
                            </strong>
                        </p>

                    </div>

                    <p style="
                        color:#64748b;
                        font-size:14px;
                    ">
                        Merci de votre confiance.
                    </p>

                    <p style="
                        color:#94a3b8;
                        font-size:12px;
                    ">
                        eSchool - Gestion scolaire
                    </p>

                </div>

                </body>
                </html>
                """
                .formatted(
                        safe(planName),
                        safe(expiresAt));

        try {

            sendHtmlEmail(
                    toEmail,
                    subject,
                    htmlContent);

            log.info(
                    "Email abonnement envoyé avec succès à {}",
                    toEmail);

        } catch (Exception e) {

            log.error(
                    "Erreur email abonnement à {}",
                    toEmail,
                    e);
        }
    }

    // =========================================================
    // EMAIL PERSONNALISE
    // =========================================================

    @Async
    public void sendCustomSchoolEmail(
            String toEmail,
            String subject,
            String messageContent) {

        try {

            sendHtmlEmail(
                    toEmail,
                    subject,
                    messageContent);

            log.info(
                    "Email personnalisé envoyé à {}",
                    toEmail);

        } catch (Exception e) {

            log.error(
                    "Erreur email personnalisé à {}",
                    toEmail,
                    e);
        }
    }

    // =========================================================
    // EMAIL ACTIVATION SUCCESS
    // =========================================================

    public void sendActivationSuccessEmail(
            String toEmail,
            String firstName) {

        String subject = "Compte activé avec succès - eSchool";

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="fr">

                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    background:#f8fafc;
                    font-family:Arial,sans-serif;
                    padding:20px;
                ">

                <div style="
                    max-width:600px;
                    margin:0 auto;
                    background:#ffffff;
                    padding:30px;
                    border:1px solid #e2e8f0;
                    border-radius:10px;
                ">

                    <h2 style="color:#2563eb;">
                        Félicitations %s ! 👋
                    </h2>

                    <p>
                        Votre compte
                        <strong>eSchool</strong>
                        a été activé avec succès.
                    </p>

                    <p>
                        Vous pouvez désormais vous connecter
                        à votre espace sécurisé.
                    </p>

                    <div style="
                        text-align:center;
                        margin:30px 0;
                    ">

                        <a href="%s/login"
                           style="
                           background-color:#2563eb;
                           color:white;
                           padding:12px 24px;
                           text-decoration:none;
                           border-radius:6px;
                           font-weight:bold;
                           display:inline-block;
                           ">
                            Accéder à mon espace
                        </a>

                    </div>

                    <hr style="
                        border:none;
                        border-top:1px solid #e2e8f0;
                        margin:20px 0;
                    " />

                    <p style="
                        font-size:12px;
                        color:#64748b;
                        text-align:center;
                    ">
                        eSchool - Gestion scolaire
                    </p>

                </div>

                </body>
                </html>
                """
                .formatted(
                        safe(firstName),
                        appFrontUrl);

        try {

            sendHtmlEmail(
                    toEmail,
                    subject,
                    htmlContent);

        } catch (Exception e) {

            log.error(
                    "Échec email confirmation activation à {}",
                    toEmail,
                    e);
        }
    }

    // =========================================================
    // VALIDATION EMAIL
    // =========================================================

    private void validateEmail(String email) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "L'adresse email est obligatoire.");
        }
    }

    // =========================================================
    // PROTECTION VALEURS NULL
    // =========================================================

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }
}
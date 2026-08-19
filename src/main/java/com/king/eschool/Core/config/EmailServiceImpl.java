package com.king.eschool.Core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

@Service
@Slf4j // 🟢 Pour les logs
public class EmailServiceImpl {

    private final JavaMailSender    mailSender;

    @Value("${app.front-url}")
    private String appFrontUrl;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username:noreply@eschool.com}") // 🟢 Expéditeur obligatoire pour le SMTP
    private String fromEmail;

    // =====================================================
    // EMAIL ACTIVATION
    // =====================================================

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
                <html>
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
                        firstName,
                        activationLink);

        sendHtmlEmail(
                toEmail,
                subject,
                htmlContent);
    }

    // =====================================================
    // EMAIL RESET PASSWORD
    // =====================================================

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
                <html>
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
                        Ce lien est valable pendant 30 minutes.
                    </p>

                    <p style="
                        color:#94a3b8;
                        font-size:12px;
                        margin-top:30px;
                    ">
                        Si vous n'avez pas demandé cette
                        réinitialisation, ignorez simplement cet e-mail.
                    </p>

                </div>

                </body>
                </html>
                """
                .formatted(
                        firstName,
                        resetLink);

        sendHtmlEmail(
                toEmail,
                subject,
                htmlContent);
    }


   
    // =====================================================
    // METHODE GENERIQUE
    // =====================================================

    private void sendHtmlEmail(
            String toEmail,
            String subject,
            String htmlContent) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8");

            helper.setTo(toEmail);

            helper.setSubject(subject);

            helper.setText(
                    htmlContent,
                    true);

            mailSender.send(message);
            System.out.println("mail envoyer");
        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Erreur lors de l'envoi de l'e-mail : "
                            + e.getMessage(),
                    e);
        }
    }



    @Async
    public void sendSubscriptionActivationEmail(String toEmail, String schoolName,
                                                String planName, String expiresAt) {
        try {
            log.info("Tentative d'envoi d'email d'activation d'abonnement à : {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail); // 🟢 DEFINIR L'EXPÉDITEUR (Très important !)
            helper.setTo(toEmail);
            helper.setSubject("Activation de votre abonnement - " + schoolName);
            
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; border: 1px solid #e2e8f0;">
                        <h2 style="color: #2563eb;">Félicitations %s ! 🎉</h2>
                        <p style="color: #334155; font-size: 16px;">
                            Votre abonnement au plan <strong>%s</strong> a été activé avec succès.
                        </p>
                        <div style="background: #f1f5f9; padding: 15px; border-radius: 8px; margin: 20px 0;">
                            <p style="margin: 0; color: #475569;">
                                Date d'expiration : <strong style="color: #0f172a;">%s</strong>
                            </p>
                        </div>
                        <p style="color: #64748b; font-size: 14px;">Merci de votre confiance.</p>
                    </div>
                </body>
                </html>
                """.formatted(schoolName, planName, expiresAt);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Email d'activation d'abonnement envoyé avec succès à : {}", toEmail);
        } catch (Exception e) {
            log.error("❌ ERREUR lors de l'envoi de l'email d'activation à {} : ", toEmail, e);
        }
    }

    // 🟢 Implémentation de la méthode d'envoi d'email personnalisé
    @Async
    public void sendCustomSchoolEmail(String toEmail, String subject, String messageContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(messageContent, true);

            mailSender.send(message);
            log.info("Email personnalisé envoyé avec succès à : {}", toEmail);
        } catch (Exception e) {
            log.error("❌ ERREUR lors de l'envoi de l'email personnalisé à {} : ", toEmail, e);
        }
    }

  public void sendActivationSuccessEmail(String toEmail, String firstName) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Compte activé avec succès - eSchool");

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;">
                <h2 style="color: #2563eb;">Félicitations %s ! 👋</h2>
                <p>Votre compte <strong>eSchool</strong> a été activé avec succès.</p>
                <p>Vous pouvez désormais vous connecter à votre espace sécurisé en utilisant votre adresse e-mail et le mot de passe que vous venez de définir.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="https://votre-domaine.com/login" style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Accéder à mon espace</a>
                </div>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                <p style="font-size: 12px; color: #64748b; text-align: center;">Si vous n'êtes pas à l'origine de cette action, veuillez contacter immédiatement l'administration de votre établissement.</p>
            </div>
            """.formatted(firstName);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    } catch (MessagingException e) {
        log.error("Échec de l'envoi de l'e-mail de confirmation d'activation à {}", toEmail, e);
    }
}
}

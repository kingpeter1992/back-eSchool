package com.king.eschool.Core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailServiceImpl {

    private final JavaMailSender mailSender;

    @Value("${app.front-url}")
    private String appFrontUrl;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    // =====================================================
    // EMAIL ACTIVATION
    // =====================================================

    public void sendActivationEmail(
            String toEmail,
            String firstName,
            String activationToken) {

        String activationLink = appFrontUrl
                + "/auth/activate?token="
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

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Erreur lors de l'envoi de l'e-mail : "
                            + e.getMessage(),
                    e);
        }
    }
}

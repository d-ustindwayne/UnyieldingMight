package com.example.unyieldingmight.Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.unyieldingmight.BuildConfig;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public abstract class EmailFunction {
    private String responseString;
    private String fromEmail;
    private String fromName;
    private String toEmail;
    private String toName;
    private MimeMessage emailMessage;
    protected String subjectText;
    protected String bodyText;

    public String getResponseString() {
        return responseString;
    }

    public EmailFunction() {
    }

    public EmailFunction setSender(String senderName, String senderEmail) {
        this.fromName = senderName;
        this.fromEmail = senderEmail;
        return this;
    }

    public EmailFunction setReceiver(String receiverName, String receiverEmail) {
        this.toName = receiverName;
        this.toEmail = receiverEmail;
        return this;
    }

    public void createEmail(String subject, String body) {
        this.subjectText = subject;
        this.bodyText = body;
        createEmail();
    }

    public EmailFunction createEmail() {
        try {
            Properties props = getProperties();

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(BuildConfig.SMTP_USER, BuildConfig.SMTP_PASS);
                }
            });

            // Email content configuration
            emailMessage = new MimeMessage(session); // Multipurpose Internet Mail Extensions Message Class
            emailMessage.setFrom(new InternetAddress(fromEmail, fromName));
            emailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            emailMessage.setSubject(subjectText);
            emailMessage.setContent(String.format(
                            "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>%s</body></html>", bodyText),
                    "text/html; charset=utf-8"
            );

        } catch (Exception e) {
            Log.e("EMAIL FUNCTION ERROR", e.getMessage());
            this.responseString = "Creation Error: " + e.getMessage();
        }
        return this;
    }

    @NonNull
    private static Properties getProperties() {
        // SMTP Configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", BuildConfig.SMTP_HOST);
        props.put("mail.smtp.port", BuildConfig.SMTP_PORT);
        props.put("mail.smtp.socketFactory.port", BuildConfig.SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true");
        return props;
    }

    public void sendEmail() {
        if (emailMessage == null) {
            this.responseString = "Error: Email message not created.";
            return;
        }

        // Removed internal thread because the calling context (Observer) should handle
        // the background execution. This allows for immediate response string retrieval.
        try {
            Transport.send(emailMessage);
            this.responseString = "Success: Email sent successfully!";
        } catch (Exception e) {
            Log.e("EMAIL SENDING ERROR", e.getMessage());
            this.responseString = "Send Error: " + e.getMessage();
        }
    }
}
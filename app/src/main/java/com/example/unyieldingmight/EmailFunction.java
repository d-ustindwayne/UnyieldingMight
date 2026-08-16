package com.example.unyieldingmight;

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

    protected EmailFunction() {
    }

    protected EmailFunction setSender(String senderName, String senderEmail) {
        this.fromName = senderName;
        this.fromEmail = senderEmail;
        return this;
    }

    protected EmailFunction setReceiver(String receiverName, String receiverEmail) {
        this.toName = receiverName;
        this.toEmail = receiverEmail;
        return this;
    }

    protected EmailFunction createEmail(String subject, String body) {
        this.subjectText = subject;
        this.bodyText = body;
        return createEmail();
    }

    protected EmailFunction createEmail() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", BuildConfig.SMTP_HOST);
            props.put("mail.smtp.port", BuildConfig.SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(BuildConfig.SMTP_USER, BuildConfig.SMTP_PASS);
                }
            });

            emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(fromEmail, fromName));
            emailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            emailMessage.setSubject(subjectText);
            emailMessage.setContent(String.format(
                "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>%s</body></html>", bodyText), 
                "text/html; charset=utf-8"
            );

        } catch (Exception e) {
            e.printStackTrace();
            this.responseString = "Creation Error: " + e.getMessage();
        }
        return this;
    }

    protected void sendEmail() {
        if (emailMessage == null) {
            this.responseString = "Error: Email message not created.";
            return;
        }

        // Removed internal thread because the calling context (Observer) should handle 
        // the background execution. This allows for immediate response string retrieval.
        try {
            Transport.send(emailMessage);
            this.responseString = "Success: Email sent successfully!";
            android.util.Log.d("MAIL_DEBUG", "Email sent to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            this.responseString = "Send Error: " + e.getMessage();
            android.util.Log.e("MAIL_DEBUG", "Failed to send to " + toEmail + ": " + e.getMessage());
        }
    }
}

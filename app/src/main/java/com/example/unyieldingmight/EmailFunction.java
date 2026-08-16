package com.example.unyieldingmight;

import brevo.ApiClient;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import brevoModel.CreateSmtpEmail;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;

public abstract class EmailFunction {
    private String responseString;
    private SendSmtpEmailSender from;
    private SendSmtpEmailTo to;
    private SendSmtpEmail email;
    protected String subjectText;
    protected String bodyText;

    public String getResponseString() {
        return responseString;
    }

    protected EmailFunction() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth authenticateAPI = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        authenticateAPI.setApiKey(BuildConfig.BREVO_APIKEY);
    }

    protected EmailFunction setSender(String senderName, String senderEmail) {
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(senderEmail);
        sender.setName(senderName);
        this.from = sender;
        return this;
    }

    protected EmailFunction setReceiver(String receiverName, String receiverEmail) {
        SendSmtpEmailTo receiver = new SendSmtpEmailTo();
        receiver.setEmail(receiverEmail);
        receiver.setName(receiverName);
        this.to = receiver;
        return this;
    }

    protected EmailFunction createEmail(String subject, String body) {
        if (email != null) { return null; }
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(from);
        sendSmtpEmail.addToItem(to);
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setHtmlContent(body);
        this.email = sendSmtpEmail;
        return this;
    }

    protected EmailFunction createEmail() {
        if (email != null) { return null; }
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(from);
        sendSmtpEmail.addToItem(to);
        sendSmtpEmail.setSubject(subjectText);
        sendSmtpEmail.setHtmlContent(String.format(
            "<html>" +
                "<body>%s</body>" +
            "</html>"
        , bodyText)
        );
        this.email = sendSmtpEmail;
        return this;
    }

    protected void sendEmail() {
        try {
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
            CreateSmtpEmail result = apiInstance.sendTransacEmail(email);
            this.responseString = result.getMessageId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

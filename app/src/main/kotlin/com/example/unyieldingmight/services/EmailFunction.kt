package com.example.unyieldingmight.services

import android.util.Log
import com.example.unyieldingmight.BuildConfig
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

open class EmailFunction {
    var responseString: String? = null
        private set
    private var fromEmail: String? = BuildConfig.SMTP_USER
    private var fromName: String? = "Unyielding Might"
    private var toEmail: String? = null
    private var toName: String? = null
    private var emailMessage: MimeMessage? = null
    protected var subjectText: String? = null
    protected var bodyText: String? = null

    fun setSender(senderName: String?, senderEmail: String?): EmailFunction {
        this.fromName = senderName
        this.fromEmail = senderEmail
        return this
    }

    fun setReceiver(receiverEmail: String?, receiverName: String?): EmailFunction {
        this.toName = receiverName
        this.toEmail = receiverEmail
        return this
    }

    fun createEmail(subject: String?, body: String): EmailFunction {
        this.subjectText = subject
        this.bodyText = body
        return createEmail()
    }

    fun createEmail(): EmailFunction {
        try {
            val props: Properties = properties

            val session = Session.getInstance(props, object : Authenticator() {
                public override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(BuildConfig.SMTP_USER, BuildConfig.SMTP_PASS)
                }
            })

            emailMessage = MimeMessage(session)
            emailMessage?.setFrom(InternetAddress(fromEmail, fromName))
            emailMessage?.setRecipient(Message.RecipientType.TO, InternetAddress(toEmail, toName))
            emailMessage?.setSubject(subjectText)
            emailMessage?.setContent(
                String.format(
                    "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>%s</body></html>",
                    bodyText
                ),
                "text/html; charset=utf-8"
            )
        } catch (e: Exception) {
            Log.e("EMAIL FUNCTION ERROR", e.message ?: "Unknown error")
            this.responseString = "Creation Error: " + e.message
        }
        return this
    }

    fun sendEmail() {
        val message = emailMessage ?: run {
            this.responseString = "Error: Email message not created."
            return
        }

        Thread {
            try {
                Transport.send(message)
                this.responseString = "Success: Email sent successfully!"
            } catch (e: Exception) {
                Log.e("EMAIL SENDING ERROR", e.message ?: "Unknown error")
                this.responseString = "Send Error: " + e.message
            }
        }.start()
    }

    companion object {
        private val properties: Properties
            get() {
                val props = Properties()
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.host"] = BuildConfig.SMTP_HOST
                props["mail.smtp.port"] = BuildConfig.SMTP_PORT
                props["mail.smtp.socketFactory.port"] = BuildConfig.SMTP_PORT
                props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                props["mail.smtp.socketFactory.fallback"] = "false"
                props["mail.smtp.ssl.enable"] = "true"
                props["mail.smtp.ssl.protocols"] = "TLSv1.2"
                props["mail.debug"] = "true"
                return props
            }
    }
}

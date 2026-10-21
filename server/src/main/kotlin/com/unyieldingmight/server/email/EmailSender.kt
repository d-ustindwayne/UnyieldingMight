package com.unyieldingmight.server.email

import com.unyieldingmight.server.config.AppConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.Properties

data class Email(val to: String, val toName: String, val subject: String, val htmlBody: String)

interface EmailSender {
    /** Sends in the background; email failures never fail the request that triggered them. */
    fun send(email: Email)
}

class SmtpEmailSender(private val config: AppConfig.SmtpConfig) : EmailSender {
    private val log = LoggerFactory.getLogger(SmtpEmailSender::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            if (config.port == 465) {
                put("mail.smtp.ssl.enable", "true")
            } else {
                put("mail.smtp.starttls.enable", "true")
            }
            put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }
        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(config.user, config.password)
        })
    }

    override fun send(email: Email) {
        scope.launch {
            runCatching {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(config.user, config.fromName))
                    setRecipient(Message.RecipientType.TO, InternetAddress(email.to, email.toName))
                    subject = email.subject
                    setContent(EmailTemplates.wrap(email.htmlBody), "text/html; charset=utf-8")
                }
                Transport.send(message)
            }.onFailure { log.warn("Failed to send '{}' to {}: {}", email.subject, email.to, it.message) }
        }
    }
}

/** Used when SMTP isn't configured: the email is logged instead of sent. */
class LoggingEmailSender : EmailSender {
    private val log = LoggerFactory.getLogger(LoggingEmailSender::class.java)
    val sent = mutableListOf<Email>()

    override fun send(email: Email) {
        sent += email
        log.info("[email not sent: SMTP disabled] to={} subject='{}'", email.to, email.subject)
    }
}

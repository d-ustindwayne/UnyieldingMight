package com.unyieldingmight.server.email

import com.unyieldingmight.server.GymTime
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.NewsletterType
import com.unyieldingmight.shared.model.OrderDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** HTML email content. Newsletter copy is carried over from the original EmailNewsletter class. */
object EmailTemplates {
    private val zone: ZoneId = GymTime.zone
    private val whenFormat = DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").withZone(zone)

    fun wrap(body: String) =
        "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>$body" +
            "<p style='color:#888;font-size:12px;margin-top:32px'>Unyielding Might</p></body></html>"

    private fun escape(text: String) = text
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun greeting(name: String) = "<p style=\"font-size: 1.1rem; color: #444;\">Hello <b>${escape(name)}</b>,</p>"

    fun otp(code: String, purposeText: String) = Pair(
        "Your Unyielding Might code: $code",
        "<h1>Your verification code</h1><p>Use this code to $purposeText:</p>" +
            "<p style='font-size:32px;letter-spacing:8px;font-weight:bold'>$code</p>" +
            "<p>It expires in 10 minutes. If you didn't request it, you can ignore this email.</p>",
    )

    fun newsletter(type: NewsletterType, name: String, message: String? = null, bullets: List<String> = emptyList()): Pair<String, String> {
        val (subject, intro) = when (type) {
            NewsletterType.INFORMATIONAL -> "Unyielding Insight: Knowledge for Today" to
                "<h1>Unyielding Insight: Knowledge for Today</h1><p>We have some new fitness tips and nutritional insights ready for you to explore.</p>"
            NewsletterType.PROMOTIONAL -> "Unlock Your Exclusive Discount" to
                "<h1>Unlock Your Exclusive Discount</h1><p>As a thank you for being part of UnyieldingMight, we've prepared a special offer just for you.</p>"
            NewsletterType.UPDATES -> "New Update Alert!" to
                "<h1>New update alert!</h1><p>See what’s new in UnyieldingMight this week. We've added new equipment and expanded our class schedules.</p>"
            NewsletterType.BOOK_CONFIRMED -> "Booking Confirmed!" to
                "<h1 style=\"color: #007bff;\">Booking Confirmed!</h1><p>Your spot has been successfully reserved. We look forward to seeing you at the class!</p>"
            NewsletterType.CLASS_FINISHED -> "Class Completed: Well Done!" to
                "<h1 style=\"color: #28a745;\">Great job!</h1><p>You have successfully completed your gym class. Keep up that unyielding energy!</p>"
            NewsletterType.CLASS_CANCELLED -> "Urgent: Class Cancellation Notice" to
                "<h1 style=\"color: #dc3545;\">Class Cancelled</h1><p>We are sorry to inform you that your upcoming gym class has been cancelled. Please check the app for re-scheduling options.</p>"
        }
        val body = buildString {
            append(greeting(name)).append(intro)
            message?.takeIf { it.isNotBlank() }?.let { append("<p>").append(escape(it).replace("\n", "<br>")).append("</p>") }
            if (bullets.isNotEmpty()) {
                append("<ul style=\"padding-left: 20px; color: #555;\">")
                bullets.forEach { append("<li>").append(escape(it)).append("</li>") }
                append("</ul>")
            }
        }
        return subject to body
    }

    fun classDetails(className: String, startsAt: Instant) =
        "<p><b>${escape(className)}</b><br>${whenFormat.format(startsAt)}</p>"

    fun orderReceipt(name: String, order: OrderDto) = Pair(
        "Your Unyielding Might order #${order.id}",
        buildString {
            append(greeting(name))
            append("<h1>Thanks for your order!</h1><table style='border-collapse:collapse'>")
            order.lines.forEach {
                append("<tr><td style='padding:4px 12px 4px 0'>${it.quantity} × ${escape(it.name)}</td>")
                append("<td style='padding:4px 0;text-align:right'>${Money.format(it.unitPriceCents * it.quantity)}</td></tr>")
            }
            append("<tr><td style='padding-top:8px'><b>Total</b></td><td style='padding-top:8px;text-align:right'><b>${Money.format(order.totalCents)}</b></td></tr>")
            append("</table><p>Collect your items from the front desk on your next visit.</p>")
        },
    )
}

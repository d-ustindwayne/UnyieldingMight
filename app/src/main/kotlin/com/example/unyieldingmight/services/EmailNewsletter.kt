package com.example.unyieldingmight.services

import com.example.unyieldingmight.models.NewsletterType
import com.example.unyieldingmight.models.Observer

class EmailNewsletter @JvmOverloads constructor(
    val emailType: NewsletterType,
    customerName: String = "Valued Member"
) : EmailFunction(), Observer {
    private var introMessage: String? = null

    init {
        val greeting = "<p style=\"font-size: 1.1rem; color: #444;\">Hello <b>$customerName</b>,</p>"

        when (emailType) {
            NewsletterType.INFORMATIONAL -> {
                this.subjectText = "Unyielding Insight: Knowledge for Today"
                this.introMessage = greeting + "<h1>Unyielding Insight: Knowledge for Today</h1>" +
                        "<p>We have some new fitness tips and nutritional insights ready for you to explore.</p>"
            }

            NewsletterType.PROMOTIONAL -> {
                this.subjectText = "Unlock Your Exclusive Discount"
                this.introMessage = greeting + "<h1>Unlock Your Exclusive Discount</h1>" +
                        "<p>As a thank you for being part of UnyieldingMight, we've prepared a special offer just for you.</p>"
            }

            NewsletterType.UPDATES -> {
                this.subjectText = "New Update Alert!"
                this.introMessage = greeting + "<h1>New update alert!</h1>" +
                        "<p>See what’s new in UnyieldingMight this week. We've added new equipment and expanded our class schedules.</p>"
            }

            NewsletterType.BOOK_CONFIRMED -> {
                this.subjectText = "Booking Confirmed!"
                this.introMessage =
                    greeting + "<h1 style=\"color: #007bff;\">Booking Confirmed!</h1>" +
                            "<p>Your spot has been successfully reserved. We look forward to seeing you at the class!</p>"
            }

            NewsletterType.CLASS_FINISHED -> {
                this.subjectText = "Class Completed: Well Done!"
                this.introMessage = greeting + "<h1 style=\"color: #28a745;\">Great job!</h1>" +
                        "<p>You have successfully completed your gym class. Keep up that unyielding energy!</p>"
            }

            NewsletterType.CLASS_CANCELLED -> {
                this.subjectText = "Urgent: Class Cancellation Notice"
                this.introMessage =
                    greeting + "<h1 style=\"color: #dc3545;\">Class Cancelled</h1>" +
                            "<p>We are sorry to inform you that your upcoming gym class has been cancelled. Please check the app for re-scheduling options.</p>"
            }
        }
        this.bodyText = introMessage
    }

    fun updateList(vararg updates: String): EmailNewsletter? {
        if (emailType != NewsletterType.UPDATES) return null

        val updateList = StringBuilder()
        updateList.append("<ul style=\"padding-left: 20px; color: #555;\">")
        for (update in updates) {
            updateList.append("<li>$update</li>")
        }
        updateList.append("</ul>")
        this.bodyText = introMessage + updateList.toString()
        return this
    }

    override fun update() {
        this.createEmail().sendEmail()
    }
}

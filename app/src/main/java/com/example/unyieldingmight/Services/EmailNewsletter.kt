package com.example.unyieldingmight.Services;

import com.example.unyieldingmight.Models.NewsletterType;
import com.example.unyieldingmight.Models.Observer;

public class EmailNewsletter extends EmailFunction implements Observer {
    private final NewsletterType emailType;
    private String introMessage;

    public EmailNewsletter(NewsletterType type) {
        this(type, "Valued Member");
    }

    public EmailNewsletter(NewsletterType type, String customerName) {
        super();
        this.emailType = type;
        
        // Personalization greeting
        String greeting = String.format("<p style=\"font-size: 1.1rem; color: #444;\">Hello <b>%s</b>,</p>", customerName);
        
        switch (emailType) {
            case INFORMATIONAL:
                this.subjectText = "Unyielding Insight: Knowledge for Today";
                this.introMessage = greeting + "<h1>Unyielding Insight: Knowledge for Today</h1>" +
                        "<p>We have some new fitness tips and nutritional insights ready for you to explore.</p>";
                break;
            case PROMOTIONAL:
                this.subjectText = "Unlock Your Exclusive Discount";
                this.introMessage = greeting + "<h1>Unlock Your Exclusive Discount</h1>" +
                        "<p>As a thank you for being part of UnyieldingMight, we've prepared a special offer just for you.</p>";
                break;
            case UPDATES:
                this.subjectText = "New Update Alert!";
                this.introMessage = greeting + "<h1>New update alert!</h1>" +
                        "<p>See what’s new in UnyieldingMight this week. We've added new equipment and expanded our class schedules.</p>";
                break;
            case BOOK_CONFIRMED:
                this.subjectText = "Booking Confirmed!";
                this.introMessage = greeting + "<h1 style=\"color: #007bff;\">Booking Confirmed!</h1>" +
                        "<p>Your spot has been successfully reserved. We look forward to seeing you at the class!</p>";
                break;
            case CLASS_FINISHED:
                this.subjectText = "Class Completed: Well Done!";
                this.introMessage = greeting + "<h1 style=\"color: #28a745;\">Great job!</h1>" +
                        "<p>You have successfully completed your gym class. Keep up that unyielding energy!</p>";
                break;
            case CLASS_CANCELLED:
                this.subjectText = "Urgent: Class Cancellation Notice";
                this.introMessage = greeting + "<h1 style=\"color: #dc3545;\">Class Cancelled</h1>" +
                        "<p>We are sorry to inform you that your upcoming gym class has been cancelled. Please check the app for re-scheduling options.</p>";
                break;
        }
        this.bodyText = introMessage;
    }

    public EmailNewsletter updateList(String... updates) {
        // For newsletter feature.
        // Listing all updates that will be included in the newsletter created
        if (!emailType.equals(NewsletterType.UPDATES)) return null;

        StringBuilder updateList = new StringBuilder();
        updateList.append("<ul style=\"padding-left: 20px; color: #555;\">");
        for (String update: updates) {
            updateList.append(String.format("<li>%s</li>", update));
        }
        updateList.append("</ul>");
        this.bodyText = introMessage.concat(updateList.toString());
        return this;
    }

    public NewsletterType getEmailType() {
        return emailType;
    }

    @Override
    public void update() {
        // Sends the email to subscriber
        this.createEmail().sendEmail();
    }
}

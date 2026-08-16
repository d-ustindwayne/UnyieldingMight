    package com.example.unyieldingmight;

    public class EmailNewsletter extends EmailFunction implements Observer {
        private final NewsletterType emailType;
        private String introMessage;

        public EmailNewsletter(NewsletterType type) {
            super();
            this.emailType = type;
            switch (emailType) {
                case INFORMATIONAL:
                    this.introMessage = "<h1 style=\"1.5rem\">" +
                        "Unyielding Insight: Knowledge for Today" +
                    "</h1>";
                    break;
                case PROMOTIONAL:
                    this.introMessage = "<h1 style=\"1.5rem\">" +
                        "Unlock Your Exclusive Discount" +
                    "</h1>";
                    break;
                case UPDATES:
                    this.introMessage = "<h1 style=\"1.5rem\">" +
                        "New update alert! See what’s new in UnyieldingMight this week" +
                    "</h1>";
                    break;
                default:
                    break;
            }
        }

        public EmailNewsletter updateList(String... updates) {
            if (!emailType.equals(NewsletterType.UPDATES)) return null;

            StringBuilder updateList = new StringBuilder();
            updateList.append("<ul>");
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
            this.sendEmail();
        }
    }

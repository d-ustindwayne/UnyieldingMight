package com.example.unyieldingmight;

import java.util.Date;

public class GymBooking {
    private final int historyId;
    private final GymClass gymClass;
    private final String status;
    private final Date bookingDate;

    public GymBooking(Builder builder) {
        this.historyId = builder.historyId;
        this.gymClass = builder.gymClass;
        this.status = builder.status;
        this.bookingDate = builder.bookingDate;
    }

    public int getHistoryId() {
        return historyId;
    }

    public GymClass getGymClass() {
        return gymClass;
    }

    public String getStatus() {
        return status;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public static class Builder() {
        private int historyId;
        private GymClass gymClass;
        private ClassStatus status;
        private Date bookingDate;
        public Builder historyId(int historyId) {
            this.historyId = historyId;;
            return this;
        }
        public Builder gymClass(GymClass gymClass) {
            this.gymClass = gymClass;;
            return this;
        }
        public Builder status(ClassStatus status) {
            this.status = status;;
            return this;
        }
        public Builder bookingDate(Date bookingDate) {
            this.bookingDate = bookingDate;;
            return this;
        }
        public GymBooking build() {
            return new GymBooking(this);
        }
    }
}

package com.example.unyieldingmight.Models;

import java.util.ArrayList;
import java.util.List;

public class NewsletterSubscribers implements Subject {

    private List<Observer> newsletterSubscribers = new ArrayList<>();
    private NewsletterType latestUpdateType;

    public NewsletterSubscribers() {}

    public NewsletterType getLatestUpdateType() {
        return latestUpdateType;
    }

    public void setLatestUpdateType(NewsletterType latestUpdateType) {
        this.latestUpdateType = latestUpdateType;
    }

    @Override
    public void add(Observer obs) {
        newsletterSubscribers.add(obs);
    }

    @Override
    public void remove(Observer obs) {
        newsletterSubscribers.remove(obs);
    }

    @Override
    public void notifyObserver() {
        for (Observer o: newsletterSubscribers) {
            o.update();
        }
    }

    /**
     * Notifies a specific observer with a specific update type.
     * Useful for private actions like booking confirmations.
     */
    public void notifySpecificObserver(Observer obs, NewsletterType type) {
        this.setLatestUpdateType(type);
        obs.update();
    }
}

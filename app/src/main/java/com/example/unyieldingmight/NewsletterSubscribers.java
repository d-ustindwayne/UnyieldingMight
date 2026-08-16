package com.example.unyieldingmight;

import java.util.ArrayList;
import java.util.List;

public class NewsletterSubscribers implements Subject {

    private static NewsletterSubscribers instance;
    private List<Observer> newsletterSubscribers = new ArrayList<>();
    private NewsletterType latestUpdateType;
    private final List<String> executionLogs = new ArrayList<>();

    private NewsletterSubscribers() {}

    public static NewsletterSubscribers getInstance() {
        if (instance == null) {
            instance = new NewsletterSubscribers();
        }
        return instance;
    }

    public void log(String message) {
        executionLogs.add(message);
    }

    public List<String> getExecutionLogs() {
        return new ArrayList<>(executionLogs);
    }

    public void clearLogs() {
        executionLogs.clear();
    }

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

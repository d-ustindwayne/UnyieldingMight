package com.example.unyieldingmight;

import java.util.ArrayList;
import java.util.List;

public class NewsletterSubscribers implements Subject {

    private List<Observer> newsletterSubscribers = new ArrayList<>();

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
}

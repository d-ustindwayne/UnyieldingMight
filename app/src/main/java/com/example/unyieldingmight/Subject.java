package com.example.unyieldingmight;

public interface Subject {
    void add(Observer obs);
    void remove(Observer obs);
    void notifyObserver();
}

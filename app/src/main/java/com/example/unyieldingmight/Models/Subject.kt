package com.example.unyieldingmight.Models;

public interface Subject {
    void add(Observer obs);
    void remove(Observer obs);
    void notifyObserver();
}

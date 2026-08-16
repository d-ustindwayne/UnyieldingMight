package com.example.unyieldingmight.Models

interface Subject {
    fun add(obs: Observer?)
    fun remove(obs: Observer?)
    fun notifyObserver()
}

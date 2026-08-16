package com.example.unyieldingmight.models

interface Subject {
    fun add(obs: Observer?)
    fun remove(obs: Observer?)
    fun notifyObserver()
}


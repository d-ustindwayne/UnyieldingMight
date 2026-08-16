package com.example.unyieldingmight.Models

class NewsletterSubscribers : Subject {
    val newsletterSubscribers: MutableList<Observer> = mutableListOf()
    var latestUpdateType: NewsletterType? = null

    override fun add(observer: Observer?) {
        observer?.let { newsletterSubscribers.add(it) }
    }

    override fun remove(observer: Observer?) {
        newsletterSubscribers.remove(observer)
    }

    override fun notifyObserver() {
        for (observer in newsletterSubscribers) {
            observer.update()
        }
    }

    fun notifySpecificObserver(observer: Observer, updateType: NewsletterType?) {
        this.latestUpdateType = updateType
        observer.update()
    }
}

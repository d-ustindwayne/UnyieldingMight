package com.unyieldingmight.server.email

import com.unyieldingmight.shared.model.NewsletterType
import com.unyieldingmight.shared.model.OrderDto
import java.time.Instant

/** Something happened that members may want to hear about. */
sealed interface GymEvent {
    data class BookingConfirmed(val email: String, val name: String, val className: String, val startsAt: Instant) : GymEvent
    data class ClassCancelled(val email: String, val name: String, val className: String, val startsAt: Instant, val refunded: Boolean) : GymEvent
    data class ClassFinished(val email: String, val name: String, val className: String) : GymEvent
    data class OrderPaid(val email: String, val name: String, val order: OrderDto) : GymEvent
    data class Newsletter(val email: String, val name: String, val type: NewsletterType, val message: String, val bullets: List<String>) : GymEvent
}

/**
 * Observer pattern from the original design (Subject / Observer / NewsletterSubscribers):
 * services publish [GymEvent]s without knowing who listens, and observers such as
 * [EmailObserver] react. New channels (push notifications, analytics) can subscribe later.
 */
fun interface GymEventObserver {
    fun onEvent(event: GymEvent)
}

class NotificationCenter {
    private val observers = mutableListOf<GymEventObserver>()

    fun subscribe(observer: GymEventObserver) {
        observers += observer
    }

    fun unsubscribe(observer: GymEventObserver) {
        observers -= observer
    }

    fun publish(event: GymEvent) {
        observers.forEach { it.onEvent(event) }
    }
}

class EmailObserver(private val sender: EmailSender) : GymEventObserver {
    override fun onEvent(event: GymEvent) {
        val email = when (event) {
            is GymEvent.BookingConfirmed -> {
                val (subject, body) = EmailTemplates.newsletter(NewsletterType.BOOK_CONFIRMED, event.name)
                Email(event.email, event.name, subject, body + EmailTemplates.classDetails(event.className, event.startsAt))
            }
            is GymEvent.ClassCancelled -> {
                val (subject, body) = EmailTemplates.newsletter(NewsletterType.CLASS_CANCELLED, event.name)
                val refund = if (event.refunded) "<p>Your payment for this class has been refunded.</p>" else ""
                Email(event.email, event.name, subject, body + EmailTemplates.classDetails(event.className, event.startsAt) + refund)
            }
            is GymEvent.ClassFinished -> {
                val (subject, body) = EmailTemplates.newsletter(NewsletterType.CLASS_FINISHED, event.name)
                Email(event.email, event.name, subject, body)
            }
            is GymEvent.OrderPaid -> {
                val (subject, body) = EmailTemplates.orderReceipt(event.name, event.order)
                Email(event.email, event.name, subject, body)
            }
            is GymEvent.Newsletter -> {
                val (subject, body) = EmailTemplates.newsletter(event.type, event.name, event.message, event.bullets)
                Email(event.email, event.name, subject, body)
            }
        }
        sender.send(email)
    }
}

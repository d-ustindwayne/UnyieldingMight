package com.unyieldingmight.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.AddressInput
import com.unyieldingmight.app.ui.common.BodyInput
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.MembershipPreview
import com.unyieldingmight.shared.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

/**
 * Registration wizard, following the original app's flow:
 * account → verify email → "are you a member?" → (link membership) or (details → stats → activity) → done.
 */
class RegisterViewModel(private val container: AppContainer) : ViewModel() {

    enum class Step(val title: String, val subtitle: String) {
        ACCOUNT("Create your account", "It only takes a minute."),
        VERIFY("Verify your email", "We sent you a 6-digit verification code."),
        MEMBER("Are you a member?", "Members book every class for free. If you already have a membership card, link it now."),
        LINK("Link your membership", "Your Membership ID is on the back of your membership card, below your picture."),
        DETAILS("Complete your profile", "We use this to personalise class recommendations."),
        STATS("Physical stats", "Used to estimate your daily calorie needs."),
        ACTIVITY("Activity level", "How often do you exercise?"),
        DONE("All done!", "Your profile is set. Let's book your first class!"),
    }

    var step by mutableStateOf(Step.ACCOUNT)
        private set
    private val history = ArrayDeque<Step>()
    val loading = MutableStateFlow(false)
    var showErrors by mutableStateOf(false)

    // Account
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // Verification
    var code by mutableStateOf("")
    private var registrationToken: String? = null

    // Membership
    var membershipIdText by mutableStateOf("")
    var membership by mutableStateOf<MembershipPreview?>(null)
        private set

    // Profile
    var dateOfBirth by mutableStateOf("")
    var gender by mutableStateOf(Gender.MALE)
    val address = AddressInput()
    val body = BodyInput(units = container.preferences.units.value)
    var activityLevel by mutableStateOf(ActivityLevel.MODERATE)

    val stepNumber: Int get() = when (step) {
        Step.ACCOUNT -> 1
        Step.VERIFY -> 2
        Step.MEMBER, Step.LINK -> 3
        Step.DETAILS -> 4
        Step.STATS -> 5
        Step.ACTIVITY -> 6
        Step.DONE -> 7
    }
    val totalSteps = 7

    fun accountErrors() = mapOf(
        "first" to Validation.name(firstName, "First name"),
        "last" to Validation.name(lastName, "Last name"),
        "email" to Validation.email(email),
        "password" to Validation.password(password),
        "confirm" to if (confirmPassword != password) "Passwords don't match" else null,
    )

    fun dobError(): String? {
        val date = runCatching { LocalDate.parse(dateOfBirth) }.getOrNull() ?: return "Choose your date of birth"
        return if (Period.between(date, LocalDate.now()).years < 13) "You must be at least 13 to register" else null
    }

    private fun goTo(next: Step) {
        history.addLast(step)
        step = next
        showErrors = false
    }

    /** Returns false when there's no earlier step (the caller should leave the wizard). */
    fun back(): Boolean {
        val previous = history.removeLastOrNull() ?: return false
        step = previous
        showErrors = false
        return true
    }

    fun next() {
        showErrors = true
        when (step) {
            Step.ACCOUNT -> if (accountErrors().values.all { it == null }) sendCode()
            Step.VERIFY -> if (Validation.otp(code) == null) verifyCode()
            Step.MEMBER -> Unit
            Step.LINK -> membershipIdText.toIntOrNull()?.let { lookupMembership(it) }
            Step.DETAILS -> {
                address.showErrors = true
                if (dobError() == null && address.isValid()) goTo(Step.STATS)
            }
            Step.STATS -> {
                body.showErrors = true
                if (body.isValid()) goTo(Step.ACTIVITY)
            }
            Step.ACTIVITY -> goTo(Step.DONE)
            Step.DONE -> submit()
        }
    }

    fun answerMember(isMember: Boolean) {
        membership = null
        goTo(if (isMember) Step.LINK else Step.DETAILS)
    }

    fun resendCode() = sendCode(advance = false)

    private fun sendCode(advance: Boolean = true) = launch {
        val response = container.api.startRegistration(email)
        if (advance) goTo(Step.VERIFY) else container.messenger.info("Code sent", response.message)
    }

    private fun verifyCode() = launch {
        registrationToken = container.api.verifyRegistration(email, code).registrationToken
        goTo(Step.MEMBER)
    }

    private fun lookupMembership(id: Int) = launch {
        val preview = container.api.lookupMembership(registrationToken!!, id)
        membership = preview
        // Prefill everything the gym already knows so the member can skip straight to the end.
        dateOfBirth = preview.dateOfBirth
        gender = preview.gender
        preview.address?.let {
            address.street = it.street; address.city = it.city; address.region = it.region
            address.postcode = it.postcode; address.country = it.country
        }
        body.heightCm = preview.heightCm.toString()
        body.weight = preview.weightKg.toString()
        body.units = com.unyieldingmight.app.data.UnitSystem.METRIC
        activityLevel = preview.activityLevel
        goTo(if (preview.address == null) Step.DETAILS else Step.DONE)
    }

    private fun submit() {
        val token = registrationToken ?: return
        launch {
            val auth = container.api.register(
                RegisterRequest(
                    registrationToken = token,
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    address = address.toDto(),
                    heightCm = body.heightInCm()!!,
                    weightKg = body.weightInKg()!!,
                    activityLevel = activityLevel,
                    membershipId = membership?.membershipId,
                )
            )
            container.session.signIn(auth)
            container.messenger.success(
                "Registration complete",
                if (auth.user.isMember) "Your membership is linked and your profile is set." else "Your profile is set. Welcome to Unyielding Might!",
            )
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading.value = true
            try {
                block()
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                loading.value = false
            }
        }
    }
}

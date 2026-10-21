package com.unyieldingmight.app

import android.content.Context
import com.unyieldingmight.app.data.Cart
import com.unyieldingmight.app.data.Preferences
import com.unyieldingmight.app.data.SessionManager
import com.unyieldingmight.app.data.UmApi
import com.unyieldingmight.app.ui.common.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual dependency container; one instance lives in [UnyieldingMightApp]. */
class AppContainer(context: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val session = SessionManager(context.applicationContext, scope)
    val preferences = Preferences(context.applicationContext, scope)
    val cart = Cart()
    val messenger = Messenger()
    val api: UmApi get() = session.api
}

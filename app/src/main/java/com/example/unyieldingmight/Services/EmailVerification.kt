package com.example.unyieldingmight.Services

import android.util.Log
import com.example.unyieldingmight.BuildConfig
import com.example.unyieldingmight.Models.EmailVerificationData
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EmailVerification {
    private val apiKey: String? = BuildConfig.QEV_APIKEY
    private val apiURI = "https://api.quickemailverification.com/v1/verify?"
    private var email: String? = null
    private var responseString: StringBuilder? = null
    var responseCode: Int = 0
        private set

    fun email(email: String?): EmailVerification {
        this.email = email
        return this
    }

    fun verify(): EmailVerification? {
        val currentEmail = email ?: return null

        responseString = StringBuilder()
        val urlString = "${apiURI}email=$currentEmail&apikey=$apiKey"
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty("Accept", "application/json")
            responseCode = urlConnection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseString?.append(line)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VERIFICATION ERROR", e.message ?: "Unknown error")
        } finally {
            urlConnection?.disconnect()
        }

        return this
    }

    val data: EmailVerificationData?
        get() {
            val json = responseString?.toString() ?: return null
            return try {
                Gson().fromJson(json, EmailVerificationData::class.java)
            } catch (e: Exception) {
                Log.e("GSON ERROR", e.message ?: "Unknown error")
                null
            }
        }

    fun getResponseString(): String {
        return responseString?.toString() ?: ""
    }
}

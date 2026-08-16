package com.example.unyieldingmight.Utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Security {
    @Throws(Exception::class)
    fun hashData(text: String): String {
        // Implement SHA-256 one-way cryptographic hashing to securely store user credentials and ensure password integrity.
        val md = MessageDigest.getInstance("SHA-256")

        val hashBytes = md.digest(text.toByteArray(StandardCharsets.UTF_8))

        val hexString = StringBuilder()
        for (b in hashBytes) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}

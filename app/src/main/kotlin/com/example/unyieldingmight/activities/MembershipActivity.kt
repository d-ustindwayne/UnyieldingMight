package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityMembershipBinding

class MembershipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMembershipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.activityMembershipBtnNoMembership.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtras(this@MembershipActivity.intent.extras ?: Bundle())
            }
            startActivity(intent)
        }

        binding.activityMembershipBtnLinkMembership.setOnClickListener {
            val intent = Intent(this, LinkmembershipActivity::class.java).apply {
                putExtras(this@MembershipActivity.intent.extras ?: Bundle())
            }
            startActivity(intent)
        }
    }
}


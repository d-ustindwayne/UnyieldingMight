package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R

class MembershipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)
    }

    fun nextActivityProfile(v: View?) {
        val i = Intent(this, ProfileActivity::class.java)
        i.putExtras(getIntent().getExtras()!!)
        startActivity(i)
    }

    fun nextActivityLinkMembership(v: View?) {
        val i = Intent(this, LinkmembershipActivity::class.java)
        i.putExtras(getIntent().getExtras()!!)
        startActivity(i)
    }
}

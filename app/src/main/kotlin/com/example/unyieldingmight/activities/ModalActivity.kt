package com.example.unyieldingmight.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityModalBinding

class ModalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Success"
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val buttonText = intent.getStringExtra(EXTRA_BUTTON_TEXT) ?: "Back to Home"
        val targetActivityClassName = intent.getStringExtra(EXTRA_TARGET_ACTIVITY)

        binding.activityModalTvTitle.text = title
        if (message.isNullOrEmpty()) {
            binding.activityModalTvMessage.visibility = View.GONE
        } else {
            binding.activityModalTvMessage.text = message
        }
        binding.activityModalBtnAction.text = buttonText

        binding.activityModalBtnAction.setOnClickListener {
            val targetClass = if (targetActivityClassName != null) {
                try {
                    Class.forName(targetActivityClassName)
                } catch (e: Exception) {
                    HomeActivity::class.java
                }
            } else {
                HomeActivity::class.java
            }

            val intent = Intent(this, targetClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_MESSAGE = "MESSAGE"
        private const val EXTRA_BUTTON_TEXT = "BUTTON_TEXT"
        private const val EXTRA_TARGET_ACTIVITY = "TARGET_ACTIVITY"

        fun start(
            context: Context,
            title: String,
            message: String? = null,
            buttonText: String? = null,
            targetActivity: Class<*>? = null
        ) {
            val intent = Intent(context, ModalActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_BUTTON_TEXT, buttonText)
                putExtra(EXTRA_TARGET_ACTIVITY, targetActivity?.name)
            }
            context.startActivity(intent)
        }
    }
}


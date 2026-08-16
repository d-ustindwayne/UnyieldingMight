package com.example.unyieldingmight.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unyieldingmight.Models.Adapter
import com.example.unyieldingmight.Models.GymClass
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    private var gymClasses: ArrayList<GymClass>? = null
    private var adapter: Adapter? = null

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var isLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.activity_home_recyclerView)
        loadingSpinner = findViewById(R.id.home_loading_spinner)

        recyclerView.layoutManager = GridLayoutManager(this, 1)
    }

    override fun onResume() {
        super.onResume()
        loadGymClasses()
    }

    private fun loadGymClasses() {
        loadingSpinner.visibility = View.VISIBLE
        isLoaded = false

        timeoutHandler.postDelayed({
            if (!isLoaded) {
                loadingSpinner.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Error: Connection timeout. Could not load classes.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, 20000)

        Thread {
            try {
                var tdee = 2000f
                val current = Database.getCurrentUser()
                if (current != null) {
                    val customer = Database.getCustomer(current.email)
                    if (customer != null) {
                        tdee = customer.tdee
                        if (tdee <= 0) tdee = customer.calculateTDEE()
                    }
                }
                val finalTdee = tdee

                val fetchedClasses = Database.gymClassesAvailable

                runOnUiThread {
                    isLoaded = true
                    gymClasses = fetchedClasses
                    loadingSpinner.visibility = View.GONE
                    if (!fetchedClasses.isNullOrEmpty()) {
                        adapter = Adapter(this, fetchedClasses, finalTdee)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            this,
                            "No classes available at the moment.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error connecting to database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}

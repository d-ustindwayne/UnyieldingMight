package com.example.unyieldingmight.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.unyieldingmight.models.Adapter
import com.example.unyieldingmight.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private var adapter: Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.activityHomeRecyclerView.layoutManager = GridLayoutManager(this, 1)
    }

    private fun observeViewModel() {
        viewModel.gymClasses.observe(this) { classes ->
            if (!classes.isNullOrEmpty()) {
                adapter = Adapter(this, ArrayList(classes), viewModel.userTDEE.value ?: 2000f)
                binding.activityHomeRecyclerView.adapter = adapter
            } else {
                Toast.makeText(this, "No classes available at the moment.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.homeLoadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadGymClasses()
    }
}


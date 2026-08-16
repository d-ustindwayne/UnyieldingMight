package com.example.unyieldingmight;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.unyieldingmight.databinding.ActivitySampleViewDatabaseBinding;
import java.util.ArrayList;

public class SampleViewDatabase extends AppCompatActivity {

    private ActivitySampleViewDatabaseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySampleViewDatabaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final Button viewDBButton = binding.viewDBButton;
        final TextView textView8 = binding.textView8;

        if (viewDBButton != null && textView8 != null) {
            viewDBButton.setOnClickListener(v -> {
                StringBuilder debugInfo = new StringBuilder("--- Build Config Info ---\n");
                debugInfo.append("Host: ").append(BuildConfig.DB_HOST).append("\n");
                debugInfo.append("Port: ").append(BuildConfig.DB_PORT).append("\n");
                debugInfo.append("User: ").append(BuildConfig.DB_USER).append("\n");
                debugInfo.append("DB: ").append(BuildConfig.DB_NAME).append("\n");
                debugInfo.append("Password: ").append(BuildConfig.DB_PASSWORD).append("\n");
                debugInfo.append("-------------------------\n\n");
                debugInfo.append("Attempting to connect...");
                
                textView8.setText(debugInfo.toString());
                
                new Thread(() -> {
                    try {
                        // 1. Get the connection first as requested
                        final java.sql.Connection conn = Database.getConnection();
                        
                        if (conn == null) {
                            runOnUiThread(() -> {
                                String failMsg = textView8.getText().toString();
                                failMsg += "\n\nConnection Failed!\n\nCheck if your PC's IP (" + BuildConfig.DB_HOST + ") is correct and port 1433 is open in Firewall.";
                                textView8.setText(failMsg);
                            });
                            return;
                        }

                        runOnUiThread(() -> textView8.setText(textView8.getText().toString() + "\nConnected! Fetching classes..."));

                        // 2. Access database through the connection
                        final ArrayList<GymClass> classes = Database.getGymClassesAvailable();
                        
                        runOnUiThread(() -> {
                            if (classes == null || classes.isEmpty()) {
                                textView8.setText(textView8.getText().toString() + "\n\nConnected, but no classes found.\nCheck your 'GymClasses' table.");
                            } else {
                                StringBuilder sb = new StringBuilder(textView8.getText().toString());
                                sb.append("\n\nSuccess! Available Classes:\n");
                                for (GymClass gc : classes) {
                                    sb.append("- ").append(gc.getName()).append(" (ID: ").append(gc.getID()).append(")\n");
                                }
                                textView8.setText(sb.toString());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> textView8.setText(textView8.getText().toString() + "\n\nError: " + e.getMessage()));
                    }
                }).start();
            });
        }
    }
}

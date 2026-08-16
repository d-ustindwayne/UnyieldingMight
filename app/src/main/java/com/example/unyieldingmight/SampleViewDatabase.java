package com.example.unyieldingmight;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.unyieldingmight.databinding.ActivitySampleViewDatabaseBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SampleViewDatabase extends AppCompatActivity {

    private ActivitySampleViewDatabaseBinding binding;
    private static final String TAG = "DEBUG_DASHBOARD";

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

        setSupportActionBar(binding.toolbar);

        final TextView logs = binding.textView8;
        logs.setMovementMethod(new ScrollingMovementMethod());

        // --- DEBUG HELPER ---
        // REMOVE THIS AND ALL CALLS TO IT BEFORE DEPLOYMENT
        final java.util.function.Consumer<String> debugLog = (msg) -> {
            Log.d(TAG, msg);
            runOnUiThread(() -> {
                logs.append("\n" + msg);
            });
        };

        // 1. TEST REGISTER (JASMINE)
        binding.btnTestRegister.setOnClickListener(v -> {
            logs.setText("--- [START] TEST REGISTER (JASMINE) ---");
            String testEmail = "jasminesisontaboy015@gmail.com";
            debugLog.accept("Target Email: " + testEmail);
            
            new Thread(() -> {
                try {
                    debugLog.accept("Action: Calling QEV Verification API...");
                    EmailVerification ev = new EmailVerification().email(testEmail).verify();
                    
                    String rawJson = ev.getResponseString();
                    debugLog.accept("QEV RAW JSON: " + (rawJson != null ? rawJson : "NULL"));
                    
                    EmailVerificationData data = ev.getData();
                    if (data != null) {
                        debugLog.accept("Factor: Safe to send = " + data.safe_to_send());
                    }

                    debugLog.accept("Action: Starting SQL Register Transaction...");
                    boolean success = Database.registerCustomer(
                            "Jasmine", "Taboy", testEmail, "jasmine15",
                            "1/35 Clendon Avenue", "Papatoetoe", "Auckland", "New Zealand", "2121", 
                            160.0f, 46.7f, null, java.sql.Date.valueOf("2005-04-15"), "F"
                    );
                    
                    debugLog.accept("Result: SQL Registration " + (success ? "SUCCESS" : "FAILED"));
                    if (!success) {
                        debugLog.accept("Note: Check Logcat 'DATABASE_ERROR' for details. Email might already exist.");
                    }
                } catch (Exception e) {
                    debugLog.accept("CRASH in Register Thread: " + e.getMessage());
                }
            }).start();
        });

        // 1b. TEST REGISTER (JUSTIN)
        binding.btnTestRegisterJustin.setOnClickListener(v -> {
            logs.setText("--- [START] TEST REGISTER (JUSTIN) ---");
            String testEmail = "justindegrumal28@gmail.com";
            debugLog.accept("Target Email: " + testEmail);
            
            new Thread(() -> {
                try {
                    debugLog.accept("Action: Calling QEV Verification API...");
                    EmailVerification ev = new EmailVerification().email(testEmail).verify();
                    
                    String rawJson = ev.getResponseString();
                    debugLog.accept("QEV RAW JSON: " + (rawJson != null ? rawJson : "NULL"));
                    
                    EmailVerificationData data = ev.getData();
                    if (data != null) {
                        debugLog.accept("Factor: Safe to send = " + data.safe_to_send());
                    }

                    debugLog.accept("Action: Starting SQL Register Transaction...");
                    boolean success = Database.registerCustomer(
                            "Justin", "Grumal", testEmail, "password123",
                            "123 Mindanao St", "Surfer", "Manukau", "Philippines", "1234",
                            175.0f, 70.0f, null, java.sql.Date.valueOf("2005-11-28"), "M"
                    );
                    
                    debugLog.accept("Result: SQL Registration " + (success ? "SUCCESS" : "FAILED"));
                    if (!success) {
                        debugLog.accept("Note: Check Logcat 'DATABASE_ERROR'. If QEV says 'false', the registration will block.");
                    }
                } catch (Exception e) {
                    debugLog.accept("CRASH in Register Thread: " + e.getMessage());
                }
            }).start();
        });

        // 2. TEST LOGIN
        binding.btnTestLogin.setOnClickListener(v -> {
            logs.setText("--- [START] TEST LOGIN ---");
            String userEmail = "nepomucenodwayne@gmail.com";
            String pass = "root0x";
            debugLog.accept("Action: Logging in as " + userEmail);
            
            new Thread(() -> {
                try {
                    User user = Database.loginUser(userEmail, pass);
                    if (user != null) {
                        debugLog.accept("SUCCESS! User Object Details:");
                        debugLog.accept(" - ID: " + user.getId());
                        debugLog.accept(" - Username: " + user.getUsername());
                        debugLog.accept(" - Role: " + user.getUserClass());
                        debugLog.accept(" - Email: " + user.getEmail());
                        debugLog.accept(" - Hash in Object: " + user.getPassword());
                    } else {
                        debugLog.accept("FAILED: Credentials incorrect or User doesn't exist.");
                        debugLog.accept("Factor: Host=" + BuildConfig.DB_HOST);
                    }
                } catch (Exception e) {
                    debugLog.accept("CRASH in Login Thread: " + e.getMessage());
                }
            }).start();
        });

        // 3. TEST OBSERVER + EMAIL
        binding.btnTestClassUpdate.setOnClickListener(v -> {
            logs.setText("--- [START] TEST OBSERVER + MAIL ---");
            new Thread(() -> {
                try {
                    debugLog.accept("Factor: SMTP Host=" + BuildConfig.SMTP_HOST);
                    debugLog.accept("Factor: SMTP User=" + BuildConfig.SMTP_USER);
                    
                    debugLog.accept("Action: Fetching Dustin & Jasmine from DB...");
                    
                    // Reset Observers for clean test
                    NewsletterSubscribers.getInstance().notifyObserver(); 

                    Customer dustin = Database.getCustomer("nepomucenodwayne@gmail.com");
                    if (dustin != null) {
                        NewsletterSubscribers.getInstance().add(dustin);
                        debugLog.accept("Success: Registered Dustin as Observer.");
                    } else {
                        debugLog.accept("Error: Dustin not found in DB.");
                    }

                    Customer jasmine = Database.getCustomer("jasminesisontaboy015@gmail.com");
                    if (jasmine != null) {
                        NewsletterSubscribers.getInstance().add(jasmine);
                        debugLog.accept("Success: Registered Jasmine as Observer.");
                    } else {
                        debugLog.accept("Error: Jasmine not found in DB.");
                    }

                    debugLog.accept("Action: Updating Class ID 1 status to COMPLETE in SQL...");
                    boolean success = Database.updateClassStatus(1, ClassStatus.COMPLETE);
                    
                    if (!success) {
                        debugLog.accept("Retrying with Class ID 2...");
                        success = Database.updateClassStatus(2, ClassStatus.COMPLETE);
                    }

                    debugLog.accept("Result: DB Update " + (success ? "SUCCESS" : "FAILED"));
                    
                    // Give the background operations a tiny bit of time to log their results
                    Thread.sleep(1500);
                    
                    List<String> mailLogs = NewsletterSubscribers.getInstance().getExecutionLogs();
                    if (mailLogs.isEmpty()) {
                        debugLog.accept("Factor: No email execution logs found.");
                    } else {
                        for (String logMsg : mailLogs) {
                            debugLog.accept("Factor: " + logMsg);
                        }
                    }
                    NewsletterSubscribers.getInstance().clearLogs();
                } catch (Exception e) {
                    debugLog.accept("CRASH in Observer Thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        });

        // 4. REFRESH LIST
        binding.viewDBButton.setOnClickListener(v -> {
            logs.setText("--- [START] REFRESH CLASSES ---");
            new Thread(() -> {
                try {
                    ArrayList<GymClass> classes = Database.getGymClassesAvailable();
                    if (classes == null || classes.isEmpty()) {
                        debugLog.accept("Result: No 'ONGOING' classes found.");
                    } else {
                        debugLog.accept("Factor: Found " + classes.size() + " classes.");
                        for (GymClass gc : classes) {
                            debugLog.accept(String.format(Locale.getDefault(), 
                                    "[%d] %s | Cal:%.0f | %s",
                                    gc.getID(), gc.getName(), gc.getAvgCaloriesBurnedPerDay(), gc.getStatus()));
                        }
                    }
                } catch (Exception e) {
                    debugLog.accept("Error: " + e.getMessage());
                }
            }).start();
        });
    }
}

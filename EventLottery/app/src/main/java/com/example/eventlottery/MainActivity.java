package com.example.eventlottery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.data.FirebaseProfileRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.databinding.ActivityMainBinding;
import com.example.eventlottery.model.PushNotificationService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    private PushNotificationService pushService;
    private ProfileRepository profileRepository;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private String localDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Adjust UI padding for status bar
        binding.navHostFragmentContentMain.setOnApplyWindowInsetsListener((v, insets) -> {
            int height = insets.getInsets(WindowInsets.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), height - 56,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        profileRepository = new FirebaseProfileRepository();
        pushService = new PushNotificationService(this);

        // True current device ID
        localDeviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        requestNotificationPermission();
        startListeningForNotifications();
    }

    /**
     * This method starts listening for notifications for the current device
     */
    private void startListeningForNotifications() {
        if (localDeviceId != null && !localDeviceId.isEmpty()) {
            pushService.listenForNotifications(localDeviceId);
        }
    }

    /**
     * This method request the notification permission for the phone
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp()
                || super.onSupportNavigateUp();
    }
}
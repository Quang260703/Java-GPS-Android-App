package msu.edu.msuexplorer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Checks if user has already signed in if not launch login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else{
            appUser.getInstance().saveUser(currentUser);
        }
    }

    /**
     * Takes user to leaderboard view
     * @param view The view
     */
    public void onLeaderboard(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }

    /**
     * Takes user to Profile view
     * @param view The view
     */
    public void onProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Once clicked, goes to the GameActivity page. Asks the user to enable their location permissions.
     * Must enable both approximate and precise location. If the user doesn't enable the location permissions
     * in the pop-up, they must enable it in their settings.
     *
     * Citations: Used https://developer.android.com/training/permissions/requesting#request-permission
     * and https://developer.android.com/develop/sensors-and-location/location/permissions for the method.
     * @param view The view
     */
    public void onSubmit(View view) {
        // Check if precise and approximate location permissions are granted by the user.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If location permissions are granted, go to the game page
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        }
        // Precise or approximate permissions are not granted by the user. Ask user to enable them.
        else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

    }

    /**
     * Asks and checks if the user enabled precise and approximate location permissions.
     *
     * Citations: Used https://developer.android.com/develop/sensors-and-location/location/permissions
     * for the method.
     */
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        }
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // If location permissions are granted, go to the game page
                            Intent intent = new Intent(this, GameActivity.class);
                            startActivity(intent);
                        }
                        else {
                            this.requestPermissionsFromSettings();
                        }
                    }
            );


    /**
     * Dialog box explaining why location permissions are required for the application.
     *
     * Citations: Used https://developer.android.com/training/permissions/requesting#request-permission
     * and Bard for the method.
     */
    private void requestPermissionsFromSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_permission_header);
        builder.setMessage(R.string.location_permission_text);
        // Creates a positive button for the dialog box. (Links to the settings page - to enable location permissions)
        builder.setPositiveButton(R.string.location_permission_positive_button, (dialog, which) -> {
            // Open app settings where user can grant permissions
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            // Display settings page
            intent.setData(uri);
            startActivity(intent);
        });
        // Negative button cancels the box
        builder.setNegativeButton(R.string.location_permission_negative_button, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
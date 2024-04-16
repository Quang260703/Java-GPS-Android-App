package msu.edu.msuexplorer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.AlertDialog;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeDisplayActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    // The app user instance of the db
    appUser appUserInstance;

    // The current username
    String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_display);

        appUserInstance = appUser.getInstance();
        currentUsername = appUserInstance.getUsername();
    }

    /**
     * Takes user to Profile view
     * @param view The view
     */
    public void onProfile(View view) {
        view.invalidate();
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /**
     * When user submits, obtain new username value and save it to the db
     * @param view The view
     */
    public void onSubmit(View view) {
        EditText editCurrentUsername = findViewById(R.id.oldUsername);
        EditText editNewUsername = findViewById(R.id.newUsername);

        String inputtedCurrentUsername = editCurrentUsername.getText().toString();
        String inputtedNewUsername = editNewUsername.getText().toString();

        // Instantiate builder
        AlertDialog.Builder builder =
                new AlertDialog.Builder(view.getContext());

        // If either the inputted current or new usernames are empty
        if (inputtedNewUsername.equals("") || inputtedCurrentUsername.equals("")) {
            // Open a dialog box for invalid input
            builder.setTitle(R.string.invalid_input);
            builder.setMessage(R.string.input_non_empty);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        // If the inputted current username doesn't equal the actual username
        } else if (!inputtedCurrentUsername.equals(currentUsername)) {
            // Open a dialog box for invalid input
            builder.setTitle(R.string.invalid_input);
            builder.setMessage(R.string.match_username_error);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        // If the new username is equal to the current username
        } else if (inputtedNewUsername.equals(inputtedCurrentUsername)) {
            // Open a dialog box for invalid input
            builder.setTitle(R.string.invalid_input);
            builder.setMessage(R.string.usernames_cant_match_error);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        // Change is successful
        } else {
            appUserInstance.changeUsername(inputtedNewUsername);

            // Open dialog box for successful change
            builder.setTitle(R.string.success_title);
            builder.setMessage(R.string.success_change_info);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        // Set text fields to empty
        editCurrentUsername.setText("");
        editNewUsername.setText("");
    }

}
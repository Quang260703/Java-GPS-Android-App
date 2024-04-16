package msu.edu.msuexplorer;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    // The app user instance of the db
    appUser appUserInstance;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        appUserInstance = appUser.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
     * Get the new password and save it into the db
     * @param view The view
     */
    public void onSubmit(View view) {
        EditText editPassword = findViewById(R.id.newPassInput);
        EditText editPasswordVerification = findViewById(R.id.reenterPassInput);

        String newPassword = editPassword.getText().toString();
        String newPasswordVerification = editPasswordVerification.getText().toString();

        // Instantiate builder
        AlertDialog.Builder builder =
                new AlertDialog.Builder(view.getContext());

        // If either fields are empty
        if (newPassword.equals("") || newPasswordVerification.equals("")) {
            // Open a dialog box for invalid input
            builder.setTitle(R.string.invalid_input);
            builder.setMessage(R.string.password_nonempty_error);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        // If the password verification doesn't match
        } else if (!newPasswordVerification.equals(newPassword)) {
            // Open a dialog box for invalid input
            builder.setTitle(R.string.invalid_input);
            builder.setMessage(R.string.no_match_error);
            builder.setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else if (newPassword.length() < 6) {
            editPassword.setError("Password needs to have at least 6 characters");
            editPasswordVerification.setText("");

        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);

            // Open a dialog box for successful change
            builder.setTitle(R.string.success_title);
            builder.setMessage(R.string.password_success);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    appUserInstance.changePassword(currentUser, newPassword);
                    startActivity(loginIntent);
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        // Reset EditText fields
        editPassword.setText("");
        editPasswordVerification.setText("");
    }
}
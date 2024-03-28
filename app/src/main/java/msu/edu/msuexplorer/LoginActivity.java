package msu.edu.msuexplorer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnTouchListener {

    private FirebaseAuth auth;
    private EditText email;
    private EditText password;
    private EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        findViewById(R.id.layout).setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent m) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onChangeToLogin(View view) {
        setContentView(R.layout.activity_login);
        findViewById(R.id.layout).setOnTouchListener(this);
    }

    public void onRegister(View view) {
        setContentView(R.layout.activity_register);
        findViewById(R.id.layout).setOnTouchListener(this);
    }

    public void onLogin(View view){
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        Button button = (Button) findViewById(R.id.buttonLogin);
        if (email.length() == 0 || password.length() == 0) {
            if (email.length() == 0) {
                email.setError("Email cannot be empty");
            }
            if (password.length() == 0) {
                password.setError("Password cannot be empty");
            }
        }
        else {
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update appUser
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                appUser.getInstance().saveUser(user);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void onSignUp(View view){
        email = findViewById(R.id.editTextEmail);
        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        Button button = (Button) findViewById(R.id.buttonSignUp);
        if (email.length() == 0 || password.length() < 6 || username.length() == 0) {
            if (email.length() == 0) {
                email.setError("Email cannot be empty");
            }
            if (username.length() == 0) {
                username.setError("Username cannot be empty");
            }
            if (password.length() < 6) {
                password.setError("Password needs to have at least 6 characters");
            }
        }
        else {
            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update appUser
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                appUser db = appUser.getInstance();
                                db.setUsername(username.getText().toString());
                                db.saveUser(user);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
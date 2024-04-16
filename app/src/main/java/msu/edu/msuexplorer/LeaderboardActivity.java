package msu.edu.msuexplorer;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends AppCompatActivity {
    ArrayList<String> usernameList;
    appUser appUserInstance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        appUserInstance = appUser.getInstance();

        // Get the data to display
        ArrayList<Integer> points = appUserInstance.getPoints();

        // Update the total score for a user
        UpdateTotalScore(points);

        // Add previous game log to activity-leaderboard
        if (points.size() != 0) {
            for (int i = (points.size() - 1); i >= 0; i--) {
                AddRow(i + 1, points.get(i));
            }
        }
    }

    /**
     * Takes user back to the login page
     * @param view The view
     */
    public void onMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    /**
     *
     */
    public void UpdateTotalScore(ArrayList<Integer> points) {
        int sum = 0;

        for (int i = 0; i < points.size(); i++) {
            sum += points.get(i);
        }

        TextView scoreView = (TextView) findViewById(R.id.score);
        String sumString = Integer.toString(sum);
        scoreView.setText(sumString);
    }

    /**
     * Add a new row to the Leaderboard. Used to add and update user
     * scores based on highest accumulated score.
     * @param gameNum The number associated with the game
     * @param score The score earned from that game
     */
    private void AddRow(int gameNum, int score) {
        // Get the Leaderboard Table by the id
        LinearLayout leaderboardTable = findViewById(R.id.previous_game_layout);

        // Add a new layout param for gameNumLabel
        TableRow.LayoutParams gameParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        gameParams.setMargins(10, 10, 10, 10);

        String previousGameString = "Game " + Integer.toString(gameNum) + ": " + Integer.toString(score);
        TextView gameLabel = new TextView(this);
        gameLabel.setText(previousGameString);
        gameLabel.setTextSize(20);
        gameLabel.setLayoutParams(gameParams);

        leaderboardTable.addView(gameLabel);
    }
}
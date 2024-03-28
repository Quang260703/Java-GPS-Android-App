package msu.edu.msuexplorer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        int rank = 1;
        ArrayList<Integer> scores = appUser.getInstance().getPoints();

        for (int score : scores) {
            AddRow(rank, "user1", score);
            rank++;
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
     * Add a new row to the Leaderboard. Used to add and update user
     * scores based on highest accumulated score.
     * @param rank The rank of the user
     */
    private void AddRow(int rank, String username, int score) {
        // Get the Leaderboard Table by the id
        TableLayout leaderboardTable = findViewById(R.id.leaderboard_table);

        // Make a new row on the leaderboard
        TableRow userRow = new TableRow(this);
        userRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));
        userRow.setPadding(10, 10, 10, 10);

        // Add a new layout param for rankLabel
        TableRow.LayoutParams rankParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT, 0.1f
        );
        rankParams.setMargins(10, 10, 10, 10);

        // Add the rank to the row
        TextView rankLabel = new TextView(this);
        String rankString = rank + ".";
        rankLabel.setText(rankString);
        rankLabel.setTextSize(20);
        rankLabel.setLayoutParams(rankParams);

        // Add layout params for usernameLabel
        TableRow.LayoutParams usernameParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1f
        );
        usernameParams.setMargins(10, 10, 10, 10);

        // Add the username to the row
        TextView usernameLabel = new TextView(this);
        usernameLabel.setText(username);
        usernameLabel.setTextSize(20);
        usernameLabel.setGravity(Gravity.LEFT);
        usernameLabel.setLayoutParams(usernameParams);

        // Add a layout params for scoreLabel
        TableRow.LayoutParams scoreParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        scoreParams.setMargins(10, 10, 10, 10);

        // Add the score to the row
        String scoreText = Integer.toString(score);
        TextView scoreLabel = new TextView(this);
        scoreLabel.setText(scoreText);
        scoreLabel.setTextSize(20);
        scoreLabel.setLayoutParams(scoreParams);

        userRow.addView(rankLabel);
        userRow.addView(usernameLabel);
        userRow.addView(scoreLabel);

        leaderboardTable.addView(userRow);
    }
}
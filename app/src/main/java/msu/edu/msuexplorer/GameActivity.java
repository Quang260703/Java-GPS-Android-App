package msu.edu.msuexplorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameActivity extends AppCompatActivity implements UserLocation.LocationDeterminationListener {
    // The fused location client
    private FusedLocationProviderClient fusedLocationClient;

    // The user location class
    private UserLocation userLocation;

    // The custom image view in the game
    // From https://github.com/davemorrissey/subsampling-scale-image-view. Replaces the regular imageView
    // with a SubsamplingScaleImageView. Allows the user to zoom in/out of an image. (Approved by professor)
    SubsamplingScaleImageView imageView;

    // the view for the game
    private View gameView;

    // the view for when the game is over
    private View gameOverView;

    // text view to display the rounds in the top left corner
    private TextView currentRoundTextView;

    //text view to display the total points in the top right corner
    private TextView currentPointsTextView;

    // Holds the locations for use in the current game
    private final List<LocationEntity> locations = new ArrayList<LocationEntity>();

    // keeps track of the current image to use in the game
    private int currentLocationIdx = 0;

    private AlertDialog calculatingLocationDialog;

    private AlertDialog customRoundDialog;

    private int totalPoints = 0;

    private int currentRoundPoints = 0;

    private double currentRoundDistance = 0;

    private final static String IMAGEIDX = "Game.imageIdx";
    private final static String GAMEOVER = "Game.gameover";
    private final static String LOCATION1 = "Game.location1";
    private final static String LOCATION2 = "Game.location2";
    private final static String LOCATION3 = "Game.location3";
    private final static String TOTALSCORE = "Game.totalScore";
    private final static String ROUNDSCORE = "Game.roundScore";
    private final static String ROUNDDISTANCE = "Game.roundDistance";
    private final static String ROUNDDIALOG = "Game.roundDialog";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(IMAGEIDX, currentLocationIdx);
        bundle.putBoolean(GAMEOVER, currentLocationIdx >= locations.size());
        bundle.putParcelable(LOCATION1, locations.get(0));
        bundle.putParcelable(LOCATION2, locations.get(1));
        bundle.putParcelable(LOCATION3, locations.get(2));
        bundle.putInt(TOTALSCORE, totalPoints);
        bundle.putBoolean(ROUNDDIALOG, customRoundDialog != null && customRoundDialog.isShowing());
        bundle.putInt(ROUNDSCORE, currentRoundPoints);
        bundle.putDouble(ROUNDDISTANCE, currentRoundDistance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentPointsTextView = findViewById(R.id.gamePointsText);
        currentRoundTextView = findViewById(R.id.gameRoundsText);

        // The view model for the DB
        LocationViewModel db = new ViewModelProvider(this).get(LocationViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userLocation = new UserLocation();
        imageView = (SubsamplingScaleImageView)findViewById(R.id.gamedestinationimage);
        // use for random calculations
        Random rnd = new Random();

        // sets the listener in userLocation
        userLocation.setLocationDeterminationListener(this);

        // grab needed views
        gameView = findViewById(R.id.gamelayout);
        gameOverView = findViewById(R.id.gameoverlayout);

        if (savedInstanceState != null) {
            currentLocationIdx = savedInstanceState.getInt(IMAGEIDX);
            locations.add(savedInstanceState.getParcelable(LOCATION1));
            locations.add(savedInstanceState.getParcelable(LOCATION2));
            locations.add(savedInstanceState.getParcelable(LOCATION3));
            totalPoints = savedInstanceState.getInt(TOTALSCORE);
            currentRoundPoints = savedInstanceState.getInt(ROUNDSCORE);
            currentRoundDistance = savedInstanceState.getDouble(ROUNDDISTANCE);
            if (savedInstanceState.getBoolean(GAMEOVER)) {
                GameOver(true);
            } else {
                if(savedInstanceState.getBoolean(ROUNDDIALOG)){
                    onLocationDetermined(currentRoundPoints, currentRoundDistance);
                }
                // set the image
                UpdateImage(locations.get(currentLocationIdx));
                setRoundsAndPointsTextViews();
            }
        } else {
            int id;
            Set<Integer> selected = new HashSet<Integer>();
            for (int i = 0; i < 3; i++) {
                List<LocationEntity> allLocations = db.getAllLocations();

                id = rnd.nextInt(allLocations.size());
                while (selected.contains(id)) {
                    id = rnd.nextInt(allLocations.size());
                }

                selected.add(id);

                LocationEntity location = db.getLocationByID(id);

                if (location != null) {
                    locations.add(location);
                }
            }
        }
        if (currentLocationIdx < locations.size()) {
            UpdateImage(locations.get(currentLocationIdx));
        }
        setRoundsAndPointsTextViews();
    }

    void setRoundsAndPointsTextViews(){
        if(currentPointsTextView != null && currentRoundTextView != null) {
            String currentPointsText = getString(R.string.game_dlg_comment) + totalPoints;
            currentPointsTextView.setText(currentPointsText);
            String currentRoundText = (currentLocationIdx + 1) + " / 3";
            currentRoundTextView.setText(currentRoundText);
        }
    }

    /**
     * Update the image view to the given location
     *
     * @param loc The location to be used
     */
    void UpdateImage(LocationEntity loc) {
        // Get the resource ID associated with the image
        int id = getResources().getIdentifier(loc.GetResourceId(), "drawable", getPackageName());
        // From https://github.com/davemorrissey/subsampling-scale-image-view. Replaces the regular imageView
        // with a SubsamplingScaleImageView. Allows the user to zoom in/out of an image. (Approved by professor)
        imageView.setImage(ImageSource.resource(id));
    }

    /**
     * Checks location and pops up a dialog box to show points and asks to move to the next image.
     */
    public void checkLocation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Permission is already granted, perform the action
        // Show a dialog with "Calculating location..." while waiting for the location
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.game_calculating_dlg);
        builder.setCancelable(false); // Make the dialog non-cancelable so it stays open

        // Store the dialog so it can be accessed later
        calculatingLocationDialog = builder.create();
        calculatingLocationDialog.show();

        double[] destinationCoords = {locations.get(currentLocationIdx).latitude, locations.get(currentLocationIdx).longitude};
        //check location
        userLocation.determineLocation(this.fusedLocationClient, destinationCoords);
    }

    /**
     * Once clicked, goes to the GameActivity page. Asks the user to enable their location permissions.
     * Must enable both approximate and precise location. If the user doesn't enable the location permissions
     * in the pop-up, they must enable it in their settings.
     * <p>
     * Citations: Used <a href="https://developer.android.com/training/permissions/requesting#request-permission">...</a>
     * and <a href="https://developer.android.com/develop/sensors-and-location/location/permissions">...</a> for the method.
     *
     * @param view The view
     */
    public void onCheckLocation(View view) {
        // Check if the permission is already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.checkLocation();
        } else {
            finish();
            Toast.makeText(this.getApplicationContext(), getString(R.string.location_permissions_text_game_activity), Toast.LENGTH_LONG).show();
        }
    }

    private class NextDestinationListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Implement your onClick behavior here
            currentLocationIdx++;
            totalPoints += currentRoundPoints;
            if (currentLocationIdx >= locations.size()) {
                GameOver(false);
            } else {
                UpdateImage(locations.get(currentLocationIdx));
                setRoundsAndPointsTextViews();
            }
            customRoundDialog.dismiss();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    /**
     * Handles what happens after all images for the current game have been used
     */
    private void GameOver(boolean isLoadingFromSavedInstance) {
        if(!isLoadingFromSavedInstance){
            appUser.getInstance().savePoints(totalPoints);
        }
        TextView gameOverScoreText = findViewById(R.id.gameOverScore);
        String text = getString(R.string.gameover_scoretext) + totalPoints;
        gameOverScoreText.setText(text);
        gameView.setVisibility(View.GONE);
        gameOverView.setVisibility(View.VISIBLE);
    }

    /**
     * Starts a new game
     *
     * @param view the view
     */
    public void onNewGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Sends back to the main menu
     *
     * @param view the view
     */
    public void onMainMenu(View view) {
        finish();
    }

    /**
     * Callback function from UserLocation. This will be called when the
     * location of the user is acquired
     */
    @Override
    public void onLocationDetermined(int points, double distanceToLocation) {
        currentRoundDistance = distanceToLocation;
        currentRoundPoints = points;

        // Dismiss the "Calculating location..." dialog
        if(calculatingLocationDialog != null) {
            calculatingLocationDialog.dismiss();
        }

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.game_round_dialog, null);

        // Find the views to set content
        TextView locationName = dialogView.findViewById(R.id.gameDlgLocationName);
        TextView totalScore = dialogView.findViewById(R.id.gameDlgTotalScore);
        TextView distance = dialogView.findViewById(R.id.gameDlgDistance);
        TextView roundScore = dialogView.findViewById(R.id.gameDlgRoundScore);
        Button nextButton = dialogView.findViewById(R.id.nextButton);

        locationName.setText(locations.get(currentLocationIdx).locationName);
        String totalScoreText = "Total: " + (totalPoints + points);
        totalScore.setText(totalScoreText);
        roundScore.setText(String.valueOf(points));
        String distanceText = "Distance: " + ((int) distanceToLocation) + "m";
        distance.setText(distanceText);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        // Build and show the dialog
        customRoundDialog = builder.create();

        // Instantiate and set the listener to the button
        NextDestinationListener nextDestinationListener = new NextDestinationListener();
        nextButton.setOnClickListener(nextDestinationListener);

        customRoundDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


package msu.edu.msuexplorer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

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

    private int totalPoints = 0;

    private final static String IMAGEIDX = "Game.imageIdx";
    private final static String GAMEOVER = "Game.gameover";
    private final static String LOCATION1 = "Game.location1";
    private final static String LOCATION2 = "Game.location2";
    private final static String LOCATION3 = "Game2.location3";
    private final static String TOTALSCORE = "Game.totalScore";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(IMAGEIDX, currentLocationIdx);
        bundle.putBoolean(GAMEOVER, currentLocationIdx >= locations.size());
        bundle.putParcelable(LOCATION1, locations.get(0));
        bundle.putParcelable(LOCATION2, locations.get(1));
        bundle.putParcelable(LOCATION3, locations.get(2));
        bundle.putInt(TOTALSCORE, totalPoints);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        currentPointsTextView = findViewById(R.id.gamePointsText);
        currentRoundTextView = findViewById(R.id.gameRoundsText);

        if (savedInstanceState != null) {
            currentLocationIdx = savedInstanceState.getInt(IMAGEIDX);
            locations.add(savedInstanceState.getParcelable(LOCATION1));
            locations.add(savedInstanceState.getParcelable(LOCATION2));
            locations.add(savedInstanceState.getParcelable(LOCATION3));
            totalPoints = savedInstanceState.getInt(TOTALSCORE);
            if (savedInstanceState.getBoolean(GAMEOVER)) {
                GameOver(true);
            } else {
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
        UpdateImage(locations.get(currentLocationIdx));
        setRoundsAndPointsTextViews();
    }

    void setRoundsAndPointsTextViews(){
        currentPointsTextView.setText(getString(R.string.game_dlg_comment) + totalPoints);
        currentRoundTextView.setText((currentLocationIdx + 1) + " / 3");
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
     * Citations: Used https://developer.android.com/training/permissions/requesting#request-permission
     * and https://developer.android.com/develop/sensors-and-location/location/permissions for the method.
     *
     * @param view The view
     */
    public void onCheckLocation(View view) {
        // Check if the permission is already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.checkLocation();
        } else {
            // Permission has not been granted yet, request it
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Asks and checks if the user enabled precise and approximate location permissions.
     * <p>
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
                            this.checkLocation();
                        } else {
                            this.requestPermissionsFromSettings();
                        }
                    }
            );

    /**
     * Dialog box explaining why location permissions are required for the application.
     * <p>
     * Citations: Used https://developer.android.com/training/permissions/requesting#request-permission
     * and Bard for the method.
     */
    private void requestPermissionsFromSettings() {
        // NOTE: Used Bard to help me with creating the dialog box (Mostly with the buttons). The
        // first button is a link to the application's settings. The second button dismisses the dialog box.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_permission_header);
        builder.setMessage(R.string.location_permission_text);
        // Creates a positive button for the dialog box
        builder.setPositiveButton(R.string.location_permission_positive_button, (dialog, which) -> {
            // Open app settings where user can grant permissions
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        // Negative button cancels the box
        builder.setNegativeButton(R.string.location_permission_negative_button, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private class NextDestinationListener implements DialogInterface.OnClickListener {

        /**
         * When the dialog next is clicked a random new image will display.
         *
         * @param dialog the dialog that received the click
         * @param which  the button that was clicked (ex.
         *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
         *               of the item clicked
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            currentLocationIdx++;
            if (currentLocationIdx >= locations.size()) {
                GameOver(false);
            } else {
                UpdateImage(locations.get(currentLocationIdx));
                setRoundsAndPointsTextViews();
            }
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
        gameOverScoreText.setText(getString(R.string.gameover_scoretext) + totalPoints);
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
    }

    /**
     * Sends back to the main menu
     *
     * @param view the view
     */
    public void onMainMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    /**
     * Callback function from UserLocation. This will be called when the
     * location of the user is acquired
     */
    @Override
    public void onLocationDetermined(int points) {
        // Dismiss the "Calculating location..." dialog
        calculatingLocationDialog.dismiss();

        // Calculate the points from userLocation
        totalPoints += points;

        // Show a new dialog with the "Points..." message
        NextDestinationListener listener = new NextDestinationListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_dlg_title);
        String message = locations.get(currentLocationIdx).locationName +
                "\n" + getString(R.string.game_dlg_comment) + points +
                "\n" + "Total: " + totalPoints;
        builder.setMessage(message);
        builder.setPositiveButton(R.string.next, listener);
        AlertDialog pointsDialog = builder.create();
        pointsDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


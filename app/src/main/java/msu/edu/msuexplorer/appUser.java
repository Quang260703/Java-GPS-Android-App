package msu.edu.msuexplorer;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * A singleton instance of the user to handle communication with the remote database
 */
public class appUser {
    // The single instance of the class
    private static appUser instance;

    // the database object
    private final FirebaseFirestore db;

    // the users id
    private String uid;

    // the users email
    private String email;

    // the users username
    private String username;

    // the points the user has
    private ArrayList<Integer> points;

    // List of pairs containing username and total points
    private ArrayList<Pair<String, Integer>> userScoreList;

    // Private constructor to prevent instantiation from outside the class
    private appUser() {
        db = FirebaseFirestore.getInstance();
    }


    // Public method to access the singleton instance
    public static synchronized appUser getInstance() {
        if (instance == null) {
            // If the instance doesn't exist, create it
            instance = new appUser();
        }
        // Return the single instance
        return instance;
    }

    /**
     * Saves the user data to be used in the app.
     * @param user the current user
     */
    public void saveUser(FirebaseUser user) {
        if (user != null) {
            uid = user.getUid();
            email = user.getEmail();

            DocumentReference doc = db.collection("users").document(uid);

            doc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //handles if the user hasn't been added to the users table
                    if (document.getData() == null) {
                        points = new ArrayList<>();
                        Pair<String, Integer> userAndTotalPoints = new Pair<String, Integer>("A pair", 55);
                        points.add(0);
                        doc.set(toJson());
                    }
                    // if user has been added get the current points
                    else{
                        CompletableFuture<ArrayList<Integer>> pointsFuture = loadPoints();
                        pointsFuture.thenAccept(newPoints -> {
                            if (!newPoints.isEmpty()) {
                                points = newPoints;
                            }
                        }).exceptionally(ex -> {
                            // Handle any exceptions here
                            Log.d(TAG, "Failed to load points: ", ex);
                            return null;
                        });
                    }
                } else {
                    // Handle failure
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            });
        }
    }

    /**
     * Converts the data to a json format to be saved to the database
     * @return json format of the user
     */
    private Map<String, Object> toJson(){
        Map<String, Object> json = new HashMap<>();
        json.put("uid", uid);
        json.put("Email", email);
        json.put("points", points);
        json.put("username", username);
        return json;
    }

    /**
     * Loads the points from the database
     * @return a completableFuture Arraylist completes when pulling from the database is complete
     */
    private CompletableFuture<ArrayList<Integer>> loadPoints(){
        CompletableFuture<ArrayList<Integer>> futurePoints = new CompletableFuture<>();

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Get the username from the document
                    username = document.getString("username");

                    // loads in the numbers as longs from the db
                    List<Long> longList = (List<Long>) document.get("points");
                    points = new ArrayList<>();
                    // converts the longs back to int
                    if (longList != null) {
                        for (Long item : longList) {
                            points.add(item.intValue());
                        }
                    }
                    System.out.println("User points: " + points);
                    futurePoints.complete(points);
                } else {
                    System.out.println("No such document");
                    futurePoints.complete(points);
                }
            } else {
                futurePoints.completeExceptionally(task.getException());
                System.out.println("get failed with " + task.getException());
            }
        });
        return futurePoints;
    }

    /**
     * Save a new point to the db for the user.
     * sorts the values and only keeps the top 10 scores for the user
     * @param newPoint the new point value to be added
     */
    public void savePoints(Integer newPoint){
        DocumentReference doc = db.collection("users").document(uid);
        points.add(newPoint);
        Collections.sort(points);
        if(points.size() > 10){
            points.remove(0);
        }
        doc.set(toJson());
    }

    /**
     * Save a new username into the db,
     * used when the user changes their username
     * @param username The new username to save
     */
    public void changeUsername(String username) {
        DocumentReference uidRef = db.collection("users").document(getUid());
        this.username = username;

        // Set username field to the new username based on uid
        uidRef.update("username", this.username)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public void changePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword);
        LogOut();
    }


    /**
     * Gets the users points
     * @return an array list of the users points
     */
    public ArrayList<Integer> getPoints(){
        return points;
    }

    /**
     * Get the user id generated by Firebase
     * @return the UserId
     */
    public String getUid() { return uid; }

    /**
     * Get the email generated by Firebase
     * @return The user's email address as a string
     */
    public String getEmail() { return email; }

    /**
     * Set the users username
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }

    public void LogOut(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        uid = "";
        email = "";
        points = new ArrayList<>();
        username = "";
    }
}

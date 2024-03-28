package msu.edu.msuexplorer;

public class Points {
    // The maximum distance the user can earn points from. If greater, points = 0
    private static final double MAXIMUM_DISTANCE = 1850;

    // Maximum amount of points a user can receive based on a guess
    private static final int MAXIMUM_POINTS = 5000;

    // Points added to the guess (The user doesn't have to be in the EXACT location to earn full points)
    private static final int LEEWAY = 50;

    /**
     * Determines how many points a user should receive based on their guess. The closer the user is to 0,
     * the more points they will receive.
     * @param distance The distance between the two points
     * @return The points the user earned on their guess
     */
    public int calculatePoints(double distance) {
        // NOTE: Used Bard to help with the math
        // If the distance is greater than the maximum distance, the user will receive 0 points
        if (distance >= MAXIMUM_DISTANCE) {
            return 0;
        }

        // Normalizes the distance based on the maximum distance
        double normalize = distance / MAXIMUM_DISTANCE;

        // Multiply the Maximum points by the normalize amount to get a number between 0 - Maximum Points
        int points = (int)(MAXIMUM_POINTS * (1 - normalize));
        points += LEEWAY;

        // If the leeway goes above the maximum points, set it to the maximum points
        if (points >= MAXIMUM_POINTS) {
            points = MAXIMUM_POINTS;
        }

        return points;
    }
}

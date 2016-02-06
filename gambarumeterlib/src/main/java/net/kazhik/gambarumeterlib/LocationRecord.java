package net.kazhik.gambarumeterlib;

import android.location.Location;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.Distance;
import net.kazhik.gambarumeterlib.entity.SplitTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/12/02.
 */
public class LocationRecord {
    private Location prevLocation = null;
    private float realDistance = 0;
    private double elevationGain = 0;
    private List<Location> locations = new ArrayList<>();
    private List<SplitTime> splitTimes = new ArrayList<>();

    private float lapDistance = 1000;

    private static final String TAG = "LocationRecord";

    public void init(float lapDistance) {
        this.lapDistance = lapDistance;

        this.clear();
    }
    public void clear() {
        this.prevLocation = null;
        this.realDistance = 0;
        this.elevationGain = 0;
        this.locations.clear();
        this.splitTimes.clear();

    }
    private Distance calculateDistance(Location newLoc) {
        Distance distance = new Distance();

        if (this.prevLocation == null) {
            return distance;
        }
        float flatDistance = this.prevLocation.distanceTo(newLoc);

        /* accuracy is not reliable
        if (newLoc.getAccuracy() > flatDistance) {
            return distance;
        }
        */
        distance.setDistance(flatDistance);

        if (this.prevLocation.hasAltitude() && newLoc.hasAltitude()) {
            double elevation = Math.abs(newLoc.getAltitude() - this.prevLocation.getAltitude());

            distance.setElevation(elevation);
            distance.setDistance((float)this.calculateRealDistance(flatDistance, elevation));
        }

        return distance;

    }
    private double calculateRealDistance(float flatMove, double elevation) {
        return Math.sqrt((flatMove * flatMove) + (elevation * elevation));

    }
    public void addLap(long timestamp) {
        this.splitTimes.add(new SplitTime(timestamp, this.realDistance));
    }
    private long autoLap(long timestamp, float distance) {
        float oldDistance = this.realDistance;
        float newDistance = this.realDistance + distance;

        if (Math.floor(oldDistance / this.lapDistance) !=
                Math.floor(newDistance / this.lapDistance)) {
            this.splitTimes.add(new SplitTime(timestamp, newDistance));
            if (this.splitTimes.size() > 1) {
                return timestamp - this.splitTimes.get(this.splitTimes.size() - 2).getTimestamp();
            } else {
                return 0;
            }
        }
        return 0;

    }
    public long setNewLocation(Location location) {
        Log.d(TAG, "setNewLocation: " + location.getTime());

        Distance latestMove = this.calculateDistance(location);
        if (this.prevLocation != null) {
            if (latestMove.getDistance() == 0.0f) {
                Log.d(TAG, "Not moved");
                return 0;
            }
        }

        long lap = this.autoLap(location.getTime(), latestMove.getDistance());

        this.locations.add(new Location(location));

        if (this.prevLocation == null) {
            this.prevLocation = location;
        } else {
            if (latestMove.getDistance() > 0) {
                this.realDistance += latestMove.getDistance();
                this.prevLocation.setLatitude(location.getLatitude());
                this.prevLocation.setLongitude(location.getLongitude());
            }
            if (latestMove.getElevation() > 0) {
                this.elevationGain += latestMove.getElevation();
                this.prevLocation.setAltitude(location.getAltitude());

            }
        }
        return lap;

    }
    public float getDistance() {
        return this.realDistance;
    }
    public double getElevationGain() {
        return this.elevationGain;
    }

    public List<SplitTime> getSplits() {
        return this.splitTimes;
    }
    public List<Location> getLocationList() {
        return this.locations;
    }
}

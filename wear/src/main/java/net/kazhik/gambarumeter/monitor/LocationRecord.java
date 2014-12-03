package net.kazhik.gambarumeter.monitor;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/12/02.
 */
public class LocationRecord {
    private Location prevLocation = null;
    private float realDistance = 0;
    private double elevationGain = 0;
    private List<Location> locations = new ArrayList<Location>();
    private List<Long> laptimes = new ArrayList<Long>();

    private float lapDistance = 1000;

    private final int MIN_INTERVAL = 5 * 1000; // 5 seconds
    private final int MIN_ACCURACY = 10; // 10 metre

    public void init(float lapDistance) {
        this.lapDistance = lapDistance;

        this.clear();
    }
    public void clear() {
        this.prevLocation = null;
        this.realDistance = 0;
        this.elevationGain = 0;
        this.locations.clear();
        this.laptimes.clear();

    }
    private Distance calculateDistance(Location newLoc) {
        Distance distance = new Distance();

        if (this.prevLocation == null) {
            return distance;
        }
        float flatDistance = this.prevLocation.distanceTo(newLoc);

        if (newLoc.getAccuracy() > flatDistance) {
            return distance;
        }
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
        this.laptimes.add(timestamp);
    }
    private long autoLap(long timestamp, float distance) {
        float oldDistance = this.realDistance;
        float newDistance = this.realDistance + distance;

        if (Math.floor(oldDistance / this.lapDistance) !=
                Math.floor(newDistance / this.lapDistance)) {
            this.laptimes.add(timestamp);
            if (this.laptimes.size() > 1) {
                return timestamp - this.laptimes.get(this.laptimes.size() - 2).longValue();
            } else {
                return 0;
            }
        }
        return 0;

    }
    public long setCurrentLocation(Location location) {
        if (location.getAccuracy() > MIN_ACCURACY) {
            return 0;
        }

        Distance latestMove = this.calculateDistance(location);
        if (this.prevLocation != null) {
            if (latestMove.getDistance() == 0) {
                return 0;
            }
            if (location.getTime() - this.prevLocation.getTime() < MIN_INTERVAL) {
                return 0;
            }
        }

        long lap = this.autoLap(location.getTime(), latestMove.getDistance());

        this.locations.add(location);

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

}

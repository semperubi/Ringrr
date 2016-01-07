package android.semperubi.ringrr;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;

import org.json.JSONObject;

/**
 * Created by Herb on 12/27/2015.
 */
public class RingrrLocation {
    public Location location;
    public String provider;
    public Double longitude,latitude,altitude;
    public Float distance,accuracy,speed;
    public boolean hasDistance = false;
    public boolean hasAccuracy = false;
    public boolean hasAltitude = false;
    public boolean hasSpeed = false;
    public Integer satellites = 0;
    public Integer satellitesInFix = 0;
    public Integer timetofix=0;

    int bf;

   public RingrrLocation(Location loc,Location lastLoc,String prv) {
        location = loc;
        provider = prv;
        try {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if (location.hasAltitude()) {
                hasAltitude = true;
                altitude = location.getAltitude();
            }
            if (location.hasAccuracy()) {
                hasAccuracy = true;
                accuracy = location.getAccuracy();
            }
            if (location.hasSpeed()) {
                hasSpeed = true;
                speed = location.getSpeed();
            }
            if (lastLoc != null) {
                hasDistance = true;
                distance = location.distanceTo(lastLoc);
            }
        }
        catch (Exception e) {
            Utilities.handleCatch("Ringrr","LocationInfo",e);
        }
    }

    public long getTime() {
        return location.getTime();
    }

    public void setSatellites(GpsStatus gpsStatus) {
        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
        // Check number of satellites in list to determine fix state
        satellites = 0;
        for (GpsSatellite sat : sats) {
            satellites++;
        }
    }

    public void setSatellitesInFix(GpsStatus gpsStatus) {
        timetofix = gpsStatus.getTimeToFirstFix();
        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
        // Check number of satellites in list to determine fix state
        satellites = 0;
        satellitesInFix = 0;
        for (GpsSatellite sat : sats) {
            satellites++;
            if (sat.usedInFix()) {
                satellitesInFix++;
            }
        }
    }

    public String getJSON() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("LATITUDE", latitude);
            jsonObj.put("LONGITUDE",longitude);
            jsonObj.put("PROVIDER",provider);
            if (hasAltitude) {
                jsonObj.put("ALTITUDE", altitude);
            }
            if (hasAccuracy) {
                jsonObj.put("ACCURACY", accuracy);
            }
            if (hasSpeed) {
                jsonObj.put("SPEED", speed);
            }
            if (hasDistance) {
                jsonObj.put("DISTANCE", distance);
            }
            jsonObj.put("SATELLITES",satellites);
            jsonObj.put("SATELLITES_IN_FIX",satellitesInFix);
            jsonObj.put("FIX_TIME",timetofix);

            return jsonObj.toString();
        }
        catch (Exception e) {
            Utilities.handleCatch("RingrrLocation","getJSON",e);
        }
        return null;
    }
}

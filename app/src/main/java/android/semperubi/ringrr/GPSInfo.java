package android.semperubi.ringrr;

import android.app.Activity;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Herb on 12/21/2015.
 */
public class GPSInfo {
    static int bf;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static Location Location;
    public static int Satellites = -1;
    public static int FixSatellites = -1;
    private static StatisticsLog statLogger;


    public static void StartTracking(Activity activity)
    {
        final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        statLogger = StatisticsLog.getInstance();

        //listen for gps status changes.
        GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    switch(event) {
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            LogInfo("onGpsStatusChanged event: SATELLITE_STATUS");
                            break;
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            LogInfo("onGpsStatusChanged event: FIRST_FIX");
                            break;
                    }
                    GpsStatus status = locationManager.getGpsStatus(null);
                    Iterable<GpsSatellite> sats = status.getSatellites();
                    // Check number of satellites in list to determine fix state
                    Satellites = 0;
                    FixSatellites = 0;
                    for (GpsSatellite sat : sats) {
                        Satellites++;
                        if(sat.usedInFix()) {
                            FixSatellites++;
                        }

                    }
                    LogInfo("Satellites from GpsStatusListener: " + Satellites);
                    LogInfo("Satellites used in fix: " + FixSatellites);
                }
            }
        };
        try {
            locationManager.addGpsStatusListener(gpsStatusListener);
        }
        catch (SecurityException e) {
            bf = 1;
        }


        //listen for location changes.
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                try
                {
                    LogInfo("Location changed! Speed = " + location.getSpeed() + " & Satellites = " + location.getExtras().getInt("satellites"));
                    boolean isBetter = isBetterLocation(location, Location);
                    if(isBetter)
                    {
                        LogInfo("Set to new location");
                        Location = location;
                        Satellites = location.getExtras().getInt("satellites");
                        LogInfo("Satellites from LocationListener: " + Satellites);
                    }
                }
                catch(Exception exc)
                {
                    LogInfo(exc.getMessage());
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 5, locationListener);
        }
        catch (SecurityException e) {
            bf = 1;
        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        float accuracy,bestAccuracy;
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            LogInfo("New location is significantly newer");
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            LogInfo("New location is significantly older");
            return false;
        }

        // Check whether the new location fix is more or less accurate
        accuracy = location.getAccuracy();
        bestAccuracy = currentBestLocation.getAccuracy();
        LogInfo("Accuracy is " + accuracy + "  Best Accuracy is " + bestAccuracy);
        int accuracyDelta = (int) (accuracy - bestAccuracy);
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            LogInfo("New location quality is more accurate");
            return true;
        } else if (isNewer && !isLessAccurate) {
            LogInfo("New location quality is more accurate");
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            LogInfo("New location quality is more accurate");
            return true;
        }
        if (isSignificantlyLessAccurate) {
            LogInfo("New location quality is significantly less accurate");
        }
        else {
            LogInfo("New location quality is less accurate");
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        LogInfo("Provider 1 = " + provider1 + "  Provider 2 = " + provider2);
        return provider1.equals(provider2);
    }

    private static void LogInfo(String message) {

        statLogger.addStatLine(StatisticType.LOCATION,message);
    }

}

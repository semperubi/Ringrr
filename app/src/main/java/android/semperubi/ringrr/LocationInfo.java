package android.semperubi.ringrr;

/**
 * Created by Herb on 12/19/2015.
 */

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.ArrayList;

    @SuppressWarnings("CanBeFinal")
    public class LocationInfo implements LocationListener,GpsStatus.Listener {

        public ArrayList<RingrrLocation> locationHistory;
        public Integer historyIndex = 0;
        public String provider;

        final int MAXHISTORY = 10;
        private static final int TWO_MINUTES = 1000 * 60 * 2;
        StatisticType statType;
        Context mContext;
        private static StatisticsLog statLogger;
        private LocationManager locationManager;
        public RingrrLocation currentLocation,lastLocation,bestLocation;
        long sampleMilliseconds;
        int changeDistance;
        private int bf;

        public LocationInfo(Context ctx,int sampleMinutes,int delta)  {
            Location loc;
            statType = StatisticType.LOCATION;
            mContext = ctx;
            sampleMilliseconds = 60000*sampleMinutes;
            //sampleMilliseconds = 1000; //for debugging
            changeDistance = delta;
            statLogger = StatisticsLog.getInstance();
            locationManager = (LocationManager) (mContext.getSystemService(Context.LOCATION_SERVICE));
            Criteria criteria = new Criteria();
            locationHistory = new ArrayList<RingrrLocation>();
            try {
                provider = locationManager.getBestProvider(criteria, false);
                locationManager.requestLocationUpdates(provider, sampleMilliseconds, changeDistance, this);
                locationManager.addGpsStatusListener(this);
                loc = locationManager.getLastKnownLocation(provider);
                if (loc != null) {
                    addLocation(loc);
                    lastLocation = currentLocation;
                    bestLocation = lastLocation;
                    statLogger.addLogLine(LogMessageType.STATISTIC,StatisticType.LOCATION, currentLocation.getJSON(),true);
                }
            }
            catch (SecurityException e) {
                Utilities.handleCatch("LocationInfo","constructor",e);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                addLocation(location);
                if (isBetterLocation()) {
                    bestLocation = currentLocation;
                }
                lastLocation = currentLocation;
                statLogger.addLogLine(LogMessageType.STATISTIC,StatisticType.LOCATION, currentLocation.getJSON(),true);
            }
            catch (Exception e) {
                bf = 1;
            }
        }

        private void addLocation(Location location) {
            try {
                if (historyIndex > MAXHISTORY) {
                    clearHistory();
                }
                Location lastLoc = null;
                if (lastLocation != null) {
                    lastLoc = lastLocation.location;
                }
                currentLocation = new RingrrLocation(location, lastLoc, provider);
                locationHistory.add(historyIndex, currentLocation);
                historyIndex++;
            }
            catch (Exception e) {
                bf = 1;
            }
        }

        @Override
        public void onGpsStatusChanged(int event) {
            GpsStatus gpsStatus;
            try {
                switch (event) {
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        gpsStatus = locationManager.getGpsStatus(null);
                        currentLocation.setSatellites(gpsStatus);
                        LogInfo("Satellites from GpsStatusListener: " + currentLocation.satellites);
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        gpsStatus = locationManager.getGpsStatus(null);
                        currentLocation.setSatellitesInFix(gpsStatus);
                        LogInfo("Satellites from GpsStatusListener: " + currentLocation.satellites);
                        LogInfo("Satellites used in fix: " + currentLocation.satellitesInFix);
                        break;
                    default:
                        break;
                    }
            }
            catch (Exception e) {
                Utilities.handleCatch("LocationInfo","onGpsStatusChanged",e);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LogInfo("Status changed");

            // needed to satisfy class requirements
        }

        @Override
        public void onProviderEnabled(String provider) {
            // needed to satisfy class requirements
            LogInfo("Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogInfo("Provider disabled: " + provider);
            // needed to satisfy class requirements
        }

        protected boolean isBetterLocation() {
            float accuracy,bestAccuracy;
            if (bestLocation == null) {
                // A new location is always better than no location
                return true;
            }
            try {
                // Check whether the new location fix is newer or older
                long timeDelta = currentLocation.getTime() - bestLocation.getTime();
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
                accuracy = currentLocation.accuracy;
                bestAccuracy = bestLocation.accuracy;
                LogInfo("Accuracy is " + accuracy + "  Best Accuracy is " + bestAccuracy);
                int accuracyDelta = (int) (accuracy - bestAccuracy);
                boolean isLessAccurate = accuracyDelta > 0;
                boolean isMoreAccurate = accuracyDelta < 0;
                boolean isSignificantlyLessAccurate = accuracyDelta > 200;

                // Check if the old and new location are from the same provider
                boolean isFromSameProvider = isSameProvider(currentLocation.provider, bestLocation.provider);

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
                } else {
                    LogInfo("New location quality is less accurate");
                }
            }
            catch (Exception e) {
                bf = 1;
            }
            return false;
        }

        /** Checks whether two providers are the same */
        private static boolean isSameProvider(String provider1, String provider2) {
            try {
                if (provider1 == null) {
                    return provider2 == null;
                }
                LogInfo("Provider 1 = " + provider1 + "  Provider 2 = " + provider2);
                return provider1.equals(provider2);
            }
            catch (Exception e) {
                return false;
            }
        }

        private static void LogInfo(String message) {
            statLogger.addLogLine(LogMessageType.STATISTIC,StatisticType.LOCATION,message,true);
        }

        public void clearHistory() {
            locationHistory = null;
            locationHistory = new ArrayList<RingrrLocation>();
            historyIndex = 0;
        }
    }

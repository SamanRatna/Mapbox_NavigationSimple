package com.example.Yatri_Mapbox_Navigation_Simple;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import android.graphics.BitmapFactory;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.core.location.LocationEngineResult;

import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Feature;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;

// ----------------------------------------------- Main Activity starts here --------------------------------------------------------------------

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private static final String SOURCE_ID = "SOURCE_ID";
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;

    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    // variables for finding User Location
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
//  private LocationChangeListeningActivityLocationCallback callback = new LocationChangeListeningActivityLocationCallback(this);

    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {

            @Override
            public void onStyleLoaded(@NonNull Style style) {

                // To find User Location
                enableLocationComponent(style);

                // Add Destination Marker Styles
                addDestinationIconSymbolLayer(style);

                // Interface definition for a callback to be invoked when the user clicks on the map view.
                mapboxMap.addOnMapClickListener(MainActivity.this);

                button = findViewById(R.id.startButton);

                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        boolean simulateRoute = true;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();

                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);

                    }
                });
            }
        });
    }


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage("destination-icon-id", BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");

        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");

        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );

        loadedMapStyle.addLayer(destinationSymbolLayer);
    }


    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

// ----------------------------------------------------------------------------------------------------------------------------------------

//        LocationEngineResult result = null;
//        Location lastLocation = result.getLastLocation();
//        System.out.println("-------------------" + lastLocation.getLatitude());
//        System.out.println("-------------------" + lastLocation.getLongitude());

//        long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
//        long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
//
//        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);
//
//        LocationEngineRequest request = new LocationEngineRequest
//                .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
//                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
//                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
//                .build();
//
//        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
//        locationEngine.getLastLocation(callback);
//
//        double currentLatitude = lastLocation.getLatitude();
//        double currentLongitude = lastLocation.getLongitude();

//        LocationEngineResult result = null;
//        LocationEngineResult result = new LocationEngineResult();

//        double currentLatitude = result.getLastLocation().getLatitude();
//        System.out.println(currentLatitude);
//
//        double currentLongitude = result.getLastLocation().getLongitude();

//        System.out.println(locationEngine.getLastLocation(callback));

//        Point originPoint1 = Point.fromLngLat(myLongitude, myLatitude);

//        Point originPoint = Point.fromLngLat(location
//        Component.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());

//        if (originPoint == null){
//            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
//        }
// ----------------------------------------------------------------------------------------------------------------------------------------


        // ----------------------------- Fake Origin Point Starts here ----------------------------------------------------------------

        // Latitude and Longitude set manually to test if route is being generated
        double currentLatitude = 27;
        double currentLongitude = 85;

        // originPoint being generated in 'Point' format
        Point originPoint = Point.fromLngLat(currentLongitude, currentLatitude);

        System.out.println("--------------- Origin Point is ----------------------");
        System.out.println(originPoint);

    // ------------------------------ Fake origin point ends here---------------------------------------------------------------


        // destinationPoint being generated in 'Point' format
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        System.out.println("--------------- Destination Point is ----------------------");
        System.out.println(destinationPoint);

        // getRoute requests Mapbox Directions API for Route and a current Route is saved and displayed
        getRoute(originPoint, destinationPoint);

        // 'Start Navigation' Button enabled
        button.setEnabled(true);
        button.setBackgroundResource(R.color.mapboxBlue);

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");

        if (source != null) {

            source.setGeoJson(Feature.fromGeometry(destinationPoint));
            System.out.println("---------------SOURCE_ID IS PRESENT---------------");

        }

        return true;
    }


    // Get Route between User Location and Destination Marker
    private void getRoute(Point origin, Point destination) {

        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {

                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                        System.out.println("------------------ Current route status --------------------");

                        if (response.body() == null) {

                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            System.out.println("No routes found, make sure you set the right user and access token.--------------------");

                        } else if (response.body().routes().size() < 1) {

                            Log.e(TAG, "No routes found");
                            System.out.println("No routes found,--------------------.");
                        }

                        currentRoute = response.body().routes().get(0);

                        if (navigationMapRoute != null) {

                            navigationMapRoute.removeRoute();

                        } else {

                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);

                        }

                        navigationMapRoute.addRoute(currentRoute);

                        if (currentRoute == null){

                            System.out.println("=-------------------Current route is null -------------------");

                        }else{

                            System.out.println("------------------- Current route is ---------------------------------");
                            System.out.println(currentRoute);

                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

//    -----------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Enable the most basic pulsing styling by ONLY using
            // the `.pulseEnabled()` method
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.GPS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

//    -----------------------------------------------------------------------------------------------------------------------------------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}


//    ----------------------------------------------- CALLBACK FUNCTION HERE --------------------------------------------------------------------------------

//    private static class LocationChangeListeningActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {
//
//        private final WeakReference<MainActivity> activityWeakReference;
//
//        LocationChangeListeningActivityLocationCallback(MainActivity activity) {
//            this.activityWeakReference = new WeakReference<>(activity);
//        }
//
//        /**
//         * The LocationEngineCallback interface's method which fires when the device's location has changed.
//         *
//         * @param result the LocationEngineResult object which has the last known location within it.
//         */
//
//        @SuppressLint("StringFormatInvalid")
//        @Override
//        public void onSuccess(LocationEngineResult result) {
//
//            Location lastLocation = result.getLastLocation();
//            System.out.println("---------My Latitude ----------" + lastLocation.getLatitude());
//            System.out.println("---------My Longitude----------" + lastLocation.getLongitude());
//            MainActivity activity = activityWeakReference.get();
//
//            double myLatitude = lastLocation.getLatitude();
//            double myLongitude = lastLocation.getLongitude();

//            THESE LATITUDE AND LONGITUDE ARE REAL ORIGIN POINTS, AND CAN BE OBTAINED FROM INSIDE THIS CALLBACK, BUT NOT BEING ABLE TO SEND IT TO getRoute()
//
//            if (activity != null) {
//                Location location = result.getLastLocation();
//                System.out.println("--------------------- My location is-------------------------------");
//                System.out.println(location);
//            }
//
//            if (lastLocation == null) {
//                System.out.println("---------------------Failed to obtain My location -------------------------------");
//                return;
//            }
//
//            // Create a Toast which displays the new location's coordinates
//            Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
//                    String.valueOf(result.getLastLocation().getLatitude()),
//                    String.valueOf(result.getLastLocation().getLongitude())),
//                    Toast.LENGTH_SHORT).show();
//
//            // Pass the new location to the Maps SDK's LocationComponent
//            if (activity.mapboxMap != null && result.getLastLocation() != null) {
//                activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
//                System.out.println("---------------Map and User Location is obtained successfully------------------");
//            }
//
//            if (activity.mapboxMap != null ) {
//                activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
//                System.out.println("--------------- Map is generated successfully ----------------");
//            }
//
//            if (result.getLastLocation() != null) {
//                activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
//                System.out.println("--------------Last Location is obtained successfully -----------------");
//            }
//        }
//
//        /**
//         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
//         *
//         * @param exception the exception message
//         */
//
//        @Override
//        public void onFailure(@NonNull Exception exception) {
//
//            System.out.println("failed due to : "+exception.getLocalizedMessage());
//        }
//    }

//    ----------------------------------------------------------------------------------------------------------------------------

package lk.flavourdash;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliverLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private GoogleMap map;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public static final String TAG = MainActivity.class.getName();
    private double selectedLatitude;
    private double selectedLongitude;
    private Marker selectedLocationMarker;
    private TextView selectedLocationTextView;
    private FrameLayout mapContainer;
    private final String API_KEY="***";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_location);

        selectedLocationTextView = findViewById(R.id.textViewLocation);

        Places.initialize(getApplicationContext().getApplicationContext(), API_KEY);
        PlacesClient placesClient = Places.createClient(getApplicationContext());

        // Init AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // getting the latitude and longitude
                if (place.getLatLng() != null) {
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() + ", LatLng: " + selectedLatitude + ", " + selectedLongitude);

                    // Update the map for the selected location
                    updateMap();
                    updateSelectedLocationTextView();
                } else {
                    Toast.makeText(DeliverLocationActivity.this, "Select another place", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() + ", LatLng: Not available");
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mapContainer = findViewById(R.id.mapContainer);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        Submit Button
        findViewById(R.id.userLocationConfirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("selectedLatitude",selectedLatitude);
                intent.putExtra("selectedLongitude",selectedLongitude);
                intent.putExtra("selectedAddress",selectedLocationTextView.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

//        Close Btn
        Button closeButton = findViewById(R.id.closeAddressDeliveryLocation);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateMap() {
        if (map != null) {
            // Clear existing markers
            map.clear();

//           + marker and -> the camera
            LatLng location = new LatLng(selectedLatitude, selectedLongitude);
            selectedLocationMarker = map.addMarker(new MarkerOptions().position(location).title("Selected Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

            // click listener for the map
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    // selected coordinates of map click
                    selectedLatitude = latLng.latitude;
                    selectedLongitude = latLng.longitude;

                    // Update position
                    if (selectedLocationMarker != null) {
                        selectedLocationMarker.setPosition(latLng);
                        updateSelectedLocationTextView();
                    }

                    Log.i(TAG, "Updated LatLng: " + selectedLatitude + ", " + selectedLongitude);
                }
            });
        }
    }

    private void updateSelectedLocationTextView() {
        if (selectedLocationMarker != null) {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        selectedLatitude, selectedLongitude, 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String selectedAddress = address.getAddressLine(0);
                    Toast.makeText(this, "selectedAddress: "+selectedAddress, Toast.LENGTH_SHORT).show();
                    selectedLocationTextView.setText(selectedAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setIndoorEnabled(true);

//        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_style);
//        map.setMapStyle(styleOptions);


        if (checkPermission()) {
            map.setMyLocationEnabled(true);
            getLastLocation();
        } else {
            requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

    }

    private boolean checkPermission() {
        boolean permission = false;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        }else {
            // Request permissions using the launcher
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
        return permission;
    }

    private void getLastLocation() {
        if (checkPermission()) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 50));

                        selectedLatitude = currentLocation.getLatitude();
                        selectedLongitude = currentLocation.getLongitude();
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String selectedAddress = address.getAddressLine(0);
                                selectedLocationTextView.setText(selectedAddress);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void moveCamera(LatLng latLng) {
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(20f)
                .build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            } else {
//                Snackbar.make(findViewById(R.id.map), "Location Permission Denied", Snackbar.LENGTH_INDEFINITE).setAction("Settings", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                }).show();
//            }
//        }
//    }

    ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            // Handle the result of the permissions request
            boolean allGranted = true;
            for (boolean granted : result.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Permissions granted, proceed with your logic
                map.setMyLocationEnabled(true);
                getLastLocation();
            } else {
                // Permissions denied, handle accordingly (e.g., show a Snackbar)
                Snackbar.make(requireViewById(R.id.deliveryLocationView), "Location Permission Denied", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openAppSettings();
                            }
                        })
                        .show();
            }
        }
    });

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    void sendResultBack() {
        Intent intent = new Intent();
        // Put your data in the intent
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
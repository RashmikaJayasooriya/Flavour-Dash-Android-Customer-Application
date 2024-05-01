package lk.flavourdash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.PermissionChecker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lk.flavourdash.Model.Branch;
import lk.flavourdash.adapters.BranchAdapter;
import lk.flavourdash.listeners.OnItemClickListener;
import lk.flavourdash.services.DirectionApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class BranchesLocationFragment extends Fragment implements OnItemClickListener<Branch>, OnMapReadyCallback {

    BranchAdapter branchAdapter;
    private ArrayList<Branch> branches;
    private FirebaseFirestore firebaseFirestore;
    public static final String TAG=MainActivity.class.getName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private GoogleMap map;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker_current,marker_pin;
    private Polyline polyline;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_branches_location, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseFirestore=FirebaseFirestore.getInstance();

        branches=new ArrayList<>();
        RecyclerView recyclerView= view.findViewById(R.id.branchesRecycleView);
        branchAdapter=new BranchAdapter(branches,getContext(),this);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(branchAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapBranchLocation);
        mapFragment.getMapAsync(this);

        setupFirestoreListener();
    }

    private void setupFirestoreListener() {
        firebaseFirestore.collection("restaurant_branches").orderBy("name").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Branch branch = change.getDocument().toObject(Branch.class);
                    branch.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            branches.add(branch);
                            break;
                        case MODIFIED:
                            updateModifiedBranch(change);
                            break;
                        case REMOVED:
                            branches.removeIf(i -> i.getId().equals(branch.getId()));
                            break;
                    }
                }

                branchAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateModifiedBranch(DocumentChange change) {
        Branch updatedBranch = change.getDocument().toObject(Branch.class);
        updatedBranch.setId(change.getDocument().getId());

        for (int i = 0; i < branches.size(); i++) {
            Branch existingBranch = branches.get(i);

            if (existingBranch.getId().equals(updatedBranch.getId())) {
                if (!existingBranch.getName().equals(updatedBranch.getName())) {
                    existingBranch.setName(updatedBranch.getName());
                }

                if (!existingBranch.getLatitude().equals(updatedBranch.getLatitude())) {
                    existingBranch.setLatitude(updatedBranch.getLatitude());
                }

                if (!existingBranch.getLongitude().equals(updatedBranch.getLongitude())) {
                    existingBranch.setLongitude(updatedBranch.getLongitude());
                }

                branchAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onItemClick(Branch item) {
        Toast.makeText(getActivity(), item.getName(), Toast.LENGTH_SHORT).show();

        LatLng end=new LatLng(item.getLatitude(),item.getLongitude());

        if(marker_pin==null){
            MarkerOptions markerOptions=new MarkerOptions();
            markerOptions.position(end);
            marker_pin= map.addMarker(markerOptions);
        }else {
            marker_pin.setPosition(end);
        }

        LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        getDirection(start, end);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setIndoorEnabled(true);


        if (checkPermission()) {
            map.setMyLocationEnabled(true);
            getLastLocation();
        } else {
            requireActivity().requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

//    private boolean checkPermission() {
//        boolean permission = false;
//        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
//                getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            permission = true;
//        }else {
//            requestPermissions();
//        }
//        return permission;
//    }

    private boolean checkPermission() {
        boolean permission = false;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        } else {
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

                    }
                }
            });

        }
    }

    public void getDirection(LatLng start, LatLng end) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionApi directionApi = retrofit.create(DirectionApi.class);
        String origin = start.latitude + "," + start.longitude;
        String destination = end.latitude + "," + end.longitude;
        String key = "AIzaSyB-sC-I1HeY8flPCKGl3m1J0oOW7DUi_Yg";
        Call<JsonObject> json = directionApi.getJson(origin, destination, true, key);
        json.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.i(TAG, response.body().toString());
                JsonObject body = response.body();
                JsonArray routes = body.getAsJsonArray("routes");

                JsonObject route = routes.get(0).getAsJsonObject();
                JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");

                List<LatLng> points = PolyUtil.decode(overviewPolyline.get("points").getAsString());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (polyline==null) {
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.width(20);
                            polylineOptions.color(getActivity().getColor(android.R.color.holo_blue_bright));
                            polylineOptions.addAll(points);
                            polyline=map.addPolyline(polylineOptions);
                        }else {
                            polyline.setPoints(points);
                        }
                    }
                });

                Log.i(TAG,overviewPolyline.toString());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }


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
                getLastLocation();
            } else {
                // Permissions denied, handle accordingly (e.g., show a Snackbar)
                Snackbar.make(requireView(), "Location Permission Denied", Snackbar.LENGTH_INDEFINITE)
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



    public void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}
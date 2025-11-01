package com.example.khanghvse184160;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final LatLng DEFAULT_LOCATION = new LatLng(10.762622, 106.660172);
    private static final float DEFAULT_ZOOM = 14f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchInput;
    private MaterialButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        searchInputLayout = findViewById(R.id.search_input_layout);
        searchInput = findViewById(R.id.input_search_location);
        searchButton = findViewById(R.id.button_search_location);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchButton.setOnClickListener(v -> handleSearch());
        searchInput.setOnEditorActionListener((TextView v, int actionId, android.view.KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
    }

    private void handleSearch() {
        String query = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
        if (query.isEmpty()) {
            searchInputLayout.setError("Enter a location");
            return;
        }
        searchInputLayout.setError(null);

        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "Geocoder not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses == null || addresses.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show());
                    return;
                }
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                runOnUiThread(() -> {
                    if (googleMap != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void enableMyLocation() {
        if (googleMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setMyLocationEnabled();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @SuppressLint("MissingPermission")
    private void setMyLocationEnabled() {
        if (googleMap == null) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        getDeviceLocation();
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        moveCameraToLocation(location);
                    } else {
                        moveCameraToDefault();
                    }
                })
                .addOnFailureListener(e -> moveCameraToDefault());
    }

    private void moveCameraToLocation(@NonNull Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
    }

    private void moveCameraToDefault() {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(DEFAULT_LOCATION).title("Ho Chi Minh City"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocationEnabled();
            } else {
                Toast.makeText(this, "Location permission is required to show your position", Toast.LENGTH_SHORT).show();
                moveCameraToDefault();
            }
        }
    }
}

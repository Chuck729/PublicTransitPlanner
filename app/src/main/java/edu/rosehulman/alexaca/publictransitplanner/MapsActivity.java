package edu.rosehulman.alexaca.publictransitplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PLACE_PICKER_REQUEST = 1;
    private GoogleMap mMap;
    private String mEndLocAddr;
    private LatLng mEndLoc;
    private String mStartLocAddr;
    private LatLng mStartLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mStartLoc = getIntent().getParcelableExtra(MainActivity.CUURENT_LOC_EXTRA);
        mEndLoc = getIntent().getParcelableExtra(MainActivity.DESTINATION_EXTRA);
        mStartLocAddr = getIntent().getStringExtra(MainActivity.LOC_NAME_EXTRA);
        mEndLocAddr = getIntent().getStringExtra(MainActivity.DESTINATION_NAME_EXTRA);
        Button placePickerButton = (Button)findViewById(R.id.place_picker_button);
        if (mStartLoc == null || mEndLoc == null)
            Log.d("PTP", "Locations were null (Should not happen");
        else {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        placePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker();
            }
        });
    }

    private void startPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                mMap.addMarker(new MarkerOptions().snippet(place.getAddress().toString()).position(place.getLatLng()).title("Title"));
            }
        }
    }

    private void createMarker(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_marker_title, latLng.latitude, latLng.longitude));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_marker, null, false);
        final EditText editTextTitle = (EditText) view.findViewById(R.id.dialog_add_marker_edit_text_title);
        final EditText editTextSnippet = (EditText) view.findViewById(R.id.dialog_add_marker_edit_text_snippet);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String snippet = editTextSnippet.getText().toString();

                mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        LatLng start = mStartLoc;
        LatLng end = mEndLoc;
        mMap.addMarker(new MarkerOptions().position(start).title("Start").snippet(mStartLocAddr));
        mMap.addMarker(new MarkerOptions().position(end).title("End").snippet(mEndLocAddr));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16.0f));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createMarker(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Marker mMarker = marker;
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.map_layout), "Click to edit marker", Snackbar.LENGTH_LONG);
                mySnackbar.setAction("Edit", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editMarker(mMarker);
                    }
                });
                mySnackbar.show();

                return false;
            }
        });
    }

    private void editMarker(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_marker_title));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_marker, null, false);
        final EditText editTextTitle = (EditText) view.findViewById(R.id.dialog_add_marker_edit_text_title);
        editTextTitle.setText(marker.getTitle());
        final EditText editTextSnippet = (EditText) view.findViewById(R.id.dialog_add_marker_edit_text_snippet);
        editTextSnippet.setText(marker.getSnippet());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String snippet = editTextSnippet.getText().toString();
                marker.setTitle(title);
                marker.setSnippet(snippet);
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(getString(R.string.remove_marker), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();
            }
        });
        builder.create().show();
    }
}

package edu.rosehulman.alexaca.publictransitplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PLACE_PICKER_REQUEST = 1;
    private GoogleMap mMap;
    private String mEndLocAddr;
    private LatLng mEndLoc;
    private String mStartLocAddr;
    private LatLng mStartLoc;
    private ArrayList<Marker> mMarkers;
    private DatabaseReference mDBRef;
    private FirebaseUser mUser;
    private String lastSavedName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mMarkers = new ArrayList<>();
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
        mDBRef = FirebaseDatabase.getInstance().getReference();
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
                mMarkers.add(mMap.addMarker(new MarkerOptions().snippet(place.getAddress().toString()).position(place.getLatLng()).title("Title")));
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

                mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet)));
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
        mMarkers.add(mMap.addMarker(new MarkerOptions().position(start).title("Start").snippet(mStartLocAddr)));
        mMarkers.add(mMap.addMarker(new MarkerOptions().position(end).title("End").snippet(mEndLocAddr)));
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
        //code to load a saved maps
//        mDBRef.child("Users").child(mUser.getUid()).child("route1").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("datasnapshotcount", ""+dataSnapshot.getChildrenCount());
//                for (DataSnapshot d : dataSnapshot.getChildren()) {
//                    HashMap hash = (HashMap) d.getValue();
//                    Log.d("Hashmap", hash.toString());
//                    String title = (String)(hash.get("title"));
//                    String snippet = (String)(hash.get("snippet"));
//                    HashMap position = (HashMap) hash.get("position");
//                    LatLng latLng = new LatLng((double)position.get("latitude"), (double)position.get("longitude"));
//                    mMarkers.add(mMap.addMarker(new MarkerOptions().title(title).position(latLng).snippet(snippet)));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_logout:
                finish();
                break;
            case R.id.action_save:
                saveMap();
                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void saveMap() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a name to save the map's current state");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_save_map, null, false);
        final EditText editTextTitle = (EditText) view.findViewById(R.id.dialog_save_map_title);
        editTextTitle.setText(lastSavedName);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String name = editTextTitle.getText().toString();
                lastSavedName = name;
                if (name.length() > 0) {
                    mDBRef.child("Users").child(mUser.getUid()).child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                AlertDialog.Builder overrideBuilder = new AlertDialog.Builder(MapsActivity.this);
                                overrideBuilder.setTitle("Map already exists with this name.\nDo you wish to override it?");
                                overrideBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDBRef.child("Users").child(mUser.getUid()).child(name).setValue(mMarkers);
                                    }
                                });
                                overrideBuilder.setNegativeButton(android.R.string.cancel, null);
                                View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.dialog_override_map, null, false);
                                overrideBuilder.setView(view);
                                overrideBuilder.show();
                            } else
                                mDBRef.child("Users").child(mUser.getUid()).child(name).setValue(mMarkers);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        builder.show();
    }
}

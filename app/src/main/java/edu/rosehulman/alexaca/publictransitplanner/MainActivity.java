package edu.rosehulman.alexaca.publictransitplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    public static final String CUURENT_LOC_EXTRA = "CURRENT_LOC";
    public static final String DESTINATION_EXTRA = "DESTINATION";
    public static final String LOC_NAME_EXTRA = "LOC_NAME";
    public static final String DESTINATION_NAME_EXTRA = "DEST_NAME";
    public static final String MAP_NAME_EXTRA = "MAP_NAME_EXTRA";
    private GoogleMap mMap;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST;
    private TextView mDisplayLocationTV;
    private TextView mDisplayDestingationTV;
    private LatLng currentLocation = null;
    private String currentLocationAddr = null;
    private LatLng currentDestination = null;
    private String currentDestinationAddr = null;
    private LatLng currentUserLocation = null;
    private DatabaseReference mDBRef;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("test");
        Button currentLocationButton = (Button) findViewById(R.id.current_location_button);
        Button chooseLocationButton = (Button) findViewById(R.id.choose_location_button);
        Button chooseDestinationButton = (Button) findViewById(R.id.destination_button);
        Button startButton = (Button)findViewById(R.id.start_button);
        Button loadButton = (Button)findViewById(R.id.load_map_button);
        mDisplayLocationTV = (TextView) findViewById(R.id.display_location_text_view);
        mDisplayDestingationTV = (TextView) findViewById(R.id.display_destination_text_view);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
        chooseLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker(1);
            }
        });
        chooseDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacePicker(2);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySavedMaps();
            }
        });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mDBRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void displaySavedMaps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a saved map name");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

        mDBRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    arrayAdapter.add(d.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = arrayAdapter.getItem(which);
                startLoadedMap(name);
            }
        });
        builder.show();
    }

    private void startLoadedMap(String name) {
        Intent mapIntent = new Intent(this, MapsActivity.class);

        mapIntent.putExtra(MAP_NAME_EXTRA, name);
        startActivity(mapIntent);
    }

    private void startMapActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        if (currentLocation == null || currentDestination == null) {
            Toast.makeText(this, "Must choose start and end locations", Toast.LENGTH_LONG).show();
            return;
        }
        mapIntent.putExtra(CUURENT_LOC_EXTRA, currentLocation);
        mapIntent.putExtra(LOC_NAME_EXTRA, currentLocationAddr);
        mapIntent.putExtra(DESTINATION_EXTRA, currentDestination);
        mapIntent.putExtra(DESTINATION_NAME_EXTRA, currentDestinationAddr);
        startActivityForResult(mapIntent, PLACE_PICKER_REQUEST);
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    if (likelyPlaces.getCount() > 0) {
                        PlaceLikelihood placeLike = likelyPlaces.get(0);

                        currentUserLocation = placeLike.getPlace().getLatLng();
                        updateStartLocation(currentUserLocation, placeLike.getPlace());
                        likelyPlaces.release();
                    }
                }
            });
        }
    }

    private void updateStartLocation(LatLng lat, Place p) {
        mDisplayLocationTV.setText("Start: " + p.getAddress().toString());
        currentLocation = lat;
        currentLocationAddr = p.getAddress().toString();
    }

    private void startPlacePicker(int request) {
        PLACE_PICKER_REQUEST = request;
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

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                updateStartLocation(place.getLatLng(), place);
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                updateEndLocation(place.getLatLng(), place);
            }
        }
    }

    private void updateEndLocation(LatLng lat, Place p) {

        mDisplayDestingationTV.setText("End: " + p.getAddress().toString());
        currentDestination = lat;
        currentDestinationAddr = p.getAddress().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);
//            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//
//            final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//            mLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
//            LatLng latLang = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 16));
//            FloatingActionButton myLocationFab = (FloatingActionButton)findViewById(R.id.my_location_fab);
//            myLocationFab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                            || ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        mLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 16));
//                    }
//                }
//            });
//        }
//
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Connection", "Failed");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}

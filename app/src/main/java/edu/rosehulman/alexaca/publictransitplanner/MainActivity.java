package edu.rosehulman.alexaca.publictransitplanner;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST;
    private TextView mDisplayLocationTV;
    private TextView mDisplayDestingationTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("test");
        Button currentLocationButton = (Button)findViewById(R.id.current_location_button);
        Button chooseLocationButton = (Button)findViewById(R.id.choose_location_button);
        Button chooseDestinationButton = (Button)findViewById(R.id.destination_button);
//        Button viewMapButton = (Button)findViewById(R.id.view_map_button);
        mDisplayLocationTV = (TextView)findViewById(R.id.display_location_text_view);
        mDisplayDestingationTV = (TextView)findViewById(R.id.display_destination_text_view);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStartLocation("Current Location");
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
//        viewMapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        Intent intent = getIntent();
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        final Intent searchIntent = new Intent(this, SearchActivity.class);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(searchIntent);
//            }
//        });

//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void updateStartLocation(String message) {
        mDisplayLocationTV.setText("Start: " + message);
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
                Place place = PlacePicker.getPlace(data, this);
                updateStartLocation(place.getAddress().toString());
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                updateEndLocation(place.getAddress().toString());
            }
        }
    }

    private void updateEndLocation(String s) {
        mDisplayDestingationTV.setText("End: " + s);
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
}

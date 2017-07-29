package edu.rosehulman.alexaca.publictransitplanner;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchFragment.OnButtonPressed {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SearchFragment searchFragment = new SearchFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_view, searchFragment);
        ft.commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onGetDirectionsPressed(String location, String destination) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        if (geocoder.isPresent()) {
            try {
                addresses = geocoder.getFromLocationName(location, 10);
                if (addresses.size() == 0) {
                    Toast.makeText(this, "Place not found", Toast.LENGTH_LONG).show();
                    return;
                }
                for (int x = 0;x < addresses.size(); x++) {
                    Log.d("Search", addresses.get(x).toString());
                }
                Address address = addresses.get(0);
                LatLng geoLocation = new LatLng(address.getLatitude(), address.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
            } catch (IOException e) {
                Toast.makeText(this, "Network connection to geocoder not working", Toast.LENGTH_LONG).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "No place entered", Toast.LENGTH_LONG).show();
            }
        }
        ResultsFragment resultsFragment = ResultsFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_view, resultsFragment);
        ft.commit();
    }
}

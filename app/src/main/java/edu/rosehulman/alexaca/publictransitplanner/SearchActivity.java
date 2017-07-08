package edu.rosehulman.alexaca.publictransitplanner;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SearchActivity extends AppCompatActivity implements SearchFragment.OnButtonPressed {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SearchFragment searchFragment = new SearchFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_view, searchFragment);
        ft.commit();
    }

    @Override
    public void onGetDirectionsPressed(String locAndDest) {
        Log.d("Search", locAndDest);
    }
}

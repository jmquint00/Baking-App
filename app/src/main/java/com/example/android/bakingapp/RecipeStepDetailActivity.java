package com.example.android.bakingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.example.android.bakingapp.model.Recipe;

import com.example.android.bakingapp.model.Recipe;
import com.example.android.bakingapp.model.Step;
import com.google.android.exoplayer2.SimpleExoPlayer;

import static android.content.ContentValues.TAG;

public class RecipeStepDetailActivity extends AppCompatActivity {

    public static final String ARG_DATA = "step_data";

    private Recipe mRecipe;
    private SimpleExoPlayer player;
    private Step mStep;
    private int playbackPosition;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG, "(onCreate) savedInstanceState is NULL: " + playbackPosition);
        setContentView(R.layout.activity_recipe_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            Log.d(TAG, "savedInstanceState is NULL" + " "+ playbackPosition);
            Integer position = getIntent().getIntExtra("POSITION",0);
            Integer length = getIntent().getIntExtra("LENGTH",0);

            arguments = new Bundle();
            arguments.putString(RecipeStepDetailFragment.ARG_DATA,
                    getIntent().getStringExtra(RecipeStepDetailFragment.ARG_DATA));
            arguments.putInt("POSITION", position);
            arguments.putInt("LENGTH", length);
            RecipeStepDetailFragment fragment = new RecipeStepDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().add(R.id.frame_container, fragment).commit();

        }else {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (player!=null){
            outState.putLong("playback_position",  player.getCurrentPosition());
            outState.putBundle(ARG_DATA, arguments);

        }

    }

}


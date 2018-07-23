package com.example.android.bakingapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.android.bakingapp.adapter.RecipeAdapter;
import com.example.android.bakingapp.db.RecipeContract;

import com.example.android.bakingapp.model.Recipe;
import com.example.android.bakingapp.util.NetCheckUtils;
import com.example.android.bakingapp.util.NetRetrofitManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity implements RecipeAdapter.RecipeClickListener {


    /*@Nullable
    private SimpleIdlingResource mIdlingResource;

    *//**
     * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
     *//*
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;


    }
*/


    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private RecipeAdapter recipeAdapter;
    private NetRetrofitManager netRetrofitManager;

    private boolean PORTRAIT = true;

    private static final String EXTRA_DATA = "EXTRA_DATA";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.refresh)
    FloatingActionButton refresh;

    private ArrayList<Recipe> recipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getIdlingResource();

        ButterKnife.bind(this);

        PORTRAIT = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        setupList();

        refresh.setOnClickListener(v -> {
            loadRecipes();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        int firstVisiblePosition = 0;

        if(PORTRAIT) {
            LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
            firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        } else {
            GridLayoutManager layoutManager = ((GridLayoutManager) recyclerView.getLayoutManager());
            firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        }
        outState.putInt(EXTRA_POSITION, firstVisiblePosition);
        outState.putParcelableArrayList(EXTRA_DATA, recipes);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        recipes = savedInstanceState.getParcelableArrayList(EXTRA_DATA);
        int pos = savedInstanceState.getInt(EXTRA_POSITION);
        recyclerView.setAdapter(recipeAdapter);
        recipeAdapter.reset();
        recipeAdapter.setData(recipes);
        recipeAdapter.notifyDataSetChanged();
        if(PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }

        recyclerView.scrollToPosition(pos);

    }

    private void setupList() {
        recipeAdapter = new RecipeAdapter(this);

        if(PORTRAIT) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            //changed grid to 2
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        }


        recyclerView.setHasFixedSize(true);
        recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        recyclerView.setAdapter(recipeAdapter);

        netRetrofitManager = new NetRetrofitManager();

        loadRecipes();
    }

    private void loadRecipes() {
        if(NetCheckUtils.isNetworkAvailable(this)) {
            refresh.setVisibility(View.GONE);
            Call<List<Recipe>> recipeCall = netRetrofitManager.getAPIService().recipe();
            recipeCall.enqueue(new Callback<List<Recipe>>() {
                @Override
                public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                    if (response.isSuccessful()) {
                        List<Recipe> recipeList = response.body();

                        for (int i = 0; i < recipeList.size(); i++) {
                            recipeAdapter.addItem(recipeList.get(i));
                            getContentResolver().delete(uriBuilder(recipeList.get(i).getId()),
                                    null, null);
                            insertDb(recipeList.get(i));
                        }
                        recipeAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<Recipe>> call, Throwable t) {
                    Log.e("FAILURE", t.getMessage());
                    openSnack(t.getMessage());
                }
            });

        } else {
            refresh.setVisibility(View.VISIBLE);
            openSnack("Check network settings and refresh");

        }
    }

    private void openSnack(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private Uri uriBuilder(long id) {
        return ContentUris.withAppendedId(RecipeContract.RecipeEntry.CONTENT_URI, id);
    }


    private void insertDb(Recipe recipe) {

        String ingredients = new Gson().toJson(recipe.getIngredients());

        ContentValues cv = new ContentValues();
        cv.put(RecipeContract.RecipeEntry._ID, recipe.getId());
        cv.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_NAME, recipe.getName());
        cv.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_IMAGE, recipe.getImage());
        cv.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_INGREDIENT, ingredients);
        getContentResolver().insert(RecipeContract.RecipeEntry.CONTENT_URI, cv);
    }

    @Override
    public void onClick(int position) {
        Recipe recipe = recipeAdapter.getSelectedItem(position);
        String recipeData = new Gson().toJson(recipe);

        if(recipeData!=null) {
            Intent intent = new Intent(getApplicationContext(), RecipeIngredientStepListActivity.class);
            intent.putExtra("data", recipeData);
            startActivity(intent);
        }

    }
}

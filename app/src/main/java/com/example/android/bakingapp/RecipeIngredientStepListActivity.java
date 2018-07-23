package com.example.android.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.bakingapp.adapter.StepAdapter;
import com.example.android.bakingapp.model.Ingredient;
import com.example.android.bakingapp.model.Recipe;
import com.example.android.bakingapp.model.Step;
import com.example.android.bakingapp.util.NetRetrofitManager;
import com.example.android.bakingapp.widget.AppWidgetProvider;
import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.bakingapp.Constants.PREF_INGREDIENT;
import static com.example.android.bakingapp.Constants.PREF_NAME;
import static com.example.android.bakingapp.Constants.PREF_RECIPE;

public class RecipeIngredientStepListActivity extends AppCompatActivity implements StepAdapter.StepClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static StepAdapter stepAdapter;

    private NetRetrofitManager netRetrofitManager;

    private Recipe recipe;

    private StringBuilder ingredientList;

    @BindView(R.id.tv_ingredients)
    TextView tvIngredients;

    private CoordinatorLayout coordinatorLayout;


    SharedPreferences mSharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        recipe = new Gson().fromJson(
                getIntent().getStringExtra("data"),
                Recipe.class
        );

        if(recipe!=null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(recipe.getName());
            setupIngredient();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View recyclerView = findViewById(R.id.recipe_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.recipe_detail_container) != null) {
            // large-screen layouts (res/values-w900dp).
            mTwoPane = true;
        }

    }

    private void setupIngredient() {
        List<Ingredient> ingredients = recipe.getIngredients();
        ingredientList = new StringBuilder();
        for(int i=0; i<ingredients.size(); i++) {

            String quantity = ingredients.get(i).getQuantity();
            String measure = ingredients.get(i).getMeasure();
            String name = ingredients.get(i).getIngredient();
            String ingredient = quantity + " " + measure + " ~ " + name + "\n";

            ingredientList.append(ingredient);

        }

        tvIngredients.setText(ingredientList);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        stepAdapter = new StepAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(stepAdapter);

        //netRetrofitManager = new NetRetrofitManager();

        if(recipe!=null) {
            loadSteps();
        }
    }

    private void loadSteps() {
        List<Step> stepList = recipe.getSteps();

        for (int i=0; i<stepList.size(); i++) {
            stepAdapter.addItem(stepList.get(i));
        }
        stepAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(int position) {


        String stepData = new Gson().toJson(stepAdapter.getSelectedItem(position));

        if (mTwoPane) {
            Bundle arguments = new Bundle();

            arguments.putString(RecipeStepDetailFragment.ARG_DATA, stepData);
            arguments.putBoolean("TWOPANE", mTwoPane);
            arguments.putInt("POSITION", position);
            arguments.putInt("LENGTH", stepAdapter.getItemCount());
            RecipeStepDetailFragment fragment = new RecipeStepDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_detail_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else {
            Intent intent = new Intent(getApplicationContext(), RecipeStepDetailActivity.class);
            intent.putExtra(RecipeStepDetailFragment.ARG_DATA, stepData);
            intent.putExtra("POSITION", position);
            intent.putExtra("LENGTH", stepAdapter.getItemCount());

            startActivity(intent);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id==R.id.menu_widget) {

            int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                    .getAppWidgetIds(new ComponentName(getApplicationContext(), AppWidgetProvider.class));

            Intent intent = new Intent(this,AppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            sendBroadcast(intent);


            mSharedPreferences.edit().putString(PREF_RECIPE, recipe.getName()).apply();
            mSharedPreferences.edit().putString(PREF_INGREDIENT, String.valueOf(ingredientList)).apply();

            openSnack();

        } else if(id==R.id.menu_about){
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSnack() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Ingredients added to widget", Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    public static String getStep(Integer position) {
        Step step = stepAdapter.getSelectedItem(position);
        return new Gson().toJson(step);
    }

}


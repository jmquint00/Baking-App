package com.example.android.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.android.bakingapp.R;

import static com.example.android.bakingapp.Constants.PREF_INGREDIENT;
import static com.example.android.bakingapp.Constants.PREF_NAME;
import static com.example.android.bakingapp.Constants.PREF_RECIPE;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {


        // RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_list_widget);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String recipe = sharedPreferences.getString(PREF_RECIPE, "");
        String ingredients = sharedPreferences.getString(PREF_INGREDIENT, "");

        // Set up the intent
        Intent intent = new Intent(context, AppWidgetIntentService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        if (recipe.equals("")) {
            views.setTextViewText(R.id.widget_recipe_name,"EMPTY");
            views.setTextViewText(R.id.widget_info,
                    "You've have not added any recipe ingredients");
        } else {
            views.setTextViewText(R.id.widget_info,"");
            views.setTextViewText(R.id.widget_recipe_name, recipe + " Ingredients");
            views.setTextViewText(R.id.widget_ingredients, ingredients);
        }


        // update widget through manager
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }
}


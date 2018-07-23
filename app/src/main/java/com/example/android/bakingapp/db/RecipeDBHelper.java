
package com.example.android.bakingapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.bakingapp.db.RecipeContract;

import static com.example.android.bakingapp.db.RecipeContract.RecipeEntry.COLUMN_RECIPE_IMAGE;
import static com.example.android.bakingapp.db.RecipeContract.RecipeEntry.COLUMN_RECIPE_INGREDIENT;
import static com.example.android.bakingapp.db.RecipeContract.RecipeEntry.COLUMN_RECIPE_NAME;
import static com.example.android.bakingapp.db.RecipeContract.RecipeEntry.TABLE_RECIPES_NAME;

public class RecipeDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bakingapp.db";
    private static final int DATABASE_VERSION = 2;

    public RecipeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TABLE_RECIPES = "CREATE TABLE " + TABLE_RECIPES_NAME + "(" +
                RecipeContract.RecipeEntry._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                COLUMN_RECIPE_INGREDIENT + " TEXT, " +
                COLUMN_RECIPE_IMAGE+ " TEXT);";
        db.execSQL(SQL_CREATE_TABLE_RECIPES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES_NAME);
        onCreate(db);
    }
}



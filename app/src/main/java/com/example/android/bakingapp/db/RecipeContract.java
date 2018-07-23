
package com.example.android.bakingapp.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class RecipeContract {

    public static final String AUTHORITY = "com.example.android.bakingapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_LISTS = "lists";

    public static final class RecipeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LISTS).build();

        public static final String TABLE_RECIPES_NAME = "recipes";
        public static final String COLUMN_RECIPE_NAME = "name";
        public static final String COLUMN_RECIPE_IMAGE = "image";
        public static final String COLUMN_RECIPE_INGREDIENT = "ingredient";

    }


}



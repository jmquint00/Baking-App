package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final RecipeClickListener mListener;
    private List<Recipe> mLists;

    public RecipeAdapter(RecipeClickListener listener) {
        mLists = new ArrayList<>();
        mListener = listener;
    }

    public Recipe getSelectedItem(int position) {
        return mLists.get(position);
    }

    public void reset() {
        mLists.clear();
        notifyDataSetChanged();
    }

    public void addItem(Recipe item) {
        mLists.add(item);
        notifyDataSetChanged();
    }

    public void setData(List<Recipe> data) {
        mLists.addAll(data);

        notifyDataSetChanged();
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recipe_card, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = mLists.get(position);
        String name = recipe.getName();
        holder.txtTitle.setText(name);

        int servings = recipe.getServings().shortValue();
        int steps = recipe.getSteps().size();

        holder.txtDesc1.setText(steps+" Steps");
        holder.txtDesc.setText(servings+" Servings");

        String imgUrl = recipe.getImage();

        if(!TextUtils.isEmpty(imgUrl)) {
            Context context = holder.imgThumb.getContext();
            Glide.with(context)
                    .load(imgUrl)
                    .thumbnail(0.1f)
                    .into(holder.imgThumb);

        } else {
            holder.imgThumb.setImageResource(R.drawable.donut);

        }

    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public interface RecipeClickListener {
        void onClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtTitle, txtDesc, txtDesc1;
        private ImageView imgThumb = (ImageView) itemView.findViewById(R.id.img_thumb);

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            txtTitle = itemView.findViewById(R.id.tv_title);
            txtDesc = itemView.findViewById(R.id.tv_desc);
            txtDesc1 = itemView.findViewById(R.id.tv_desc_1);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mListener.onClick(getLayoutPosition());
        }
    }
}

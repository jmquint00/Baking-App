package com.example.android.bakingapp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private final StepClickListener mListener;
    private List<Step> mLists;

    public StepAdapter(StepClickListener listener) {
        mLists = new ArrayList<>();
        mListener = listener;
    }

    public Step getSelectedItem(int position) {
        return mLists.get(position);
    }

    public void reset() {
        mLists.clear();
        notifyDataSetChanged();
    }

    public void addItem(Step item) {
        mLists.add(item);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_step_card, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Step step = mLists.get(position);



        String videoUrl = step.getVideoURL();
        String name = step.getShortDescription();
        Objects.requireNonNull(holder).txtTitle.setText(name);



    }


    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public interface StepClickListener {
        void onClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtTitle;


        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            txtTitle = itemView.findViewById(R.id.tv_title);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(getLayoutPosition());
        }
    }
}

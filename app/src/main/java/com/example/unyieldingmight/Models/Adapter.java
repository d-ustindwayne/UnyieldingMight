package com.example.unyieldingmight.Models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unyieldingmight.Activities.InfoActivity;
import com.example.unyieldingmight.R;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private Context context;
    private ArrayList<GymClass> dataList;
    private float userTdee;

    public void setSearchList(ArrayList<GymClass> dataSearchList) {
        this.dataList = dataSearchList;
        notifyDataSetChanged();
    }

    public Adapter(Context context, ArrayList<GymClass> dataList, float userTdee) {
        this.context = context;
        this.dataList = dataList;
        this.userTdee = userTdee;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GymClass gymClass = dataList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        holder.recImage.setImageResource(R.drawable.boxfit);
        holder.recTitle.setText(gymClass.getName());
        holder.recInstructor.setText(gymClass.getTrainer() != null ? gymClass.getTrainer().getName() : "Unknown");

        GymClass.Intensity intensity = gymClass.getIntensity(userTdee);
        holder.recIntensity.setText(intensity.name());

        holder.recDate.setText(gymClass.getStartDateTime() != null ? dateFormat.format(gymClass.getStartDateTime()) : "N/A");
        holder.recStartTime.setText(gymClass.getStartDateTime() != null ? timeFormat.format(gymClass.getStartDateTime()) : "N/A");
        holder.recEndTime.setText(gymClass.getEndDateTime() != null ? timeFormat.format(gymClass.getEndDateTime()) : "N/A");

        holder.recDesc.setText(gymClass.getDescription());
        holder.recCurCap.setText(String.valueOf(gymClass.getCurrentCapacity()));
        holder.recMaxCap.setText(String.valueOf(gymClass.getMaxCapacity()));

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                GymClass current = dataList.get(pos);

                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra("Image", R.drawable.boxfit);
                intent.putExtra("Title", current.getName());
                intent.putExtra("Instructor", current.getTrainer() != null ? current.getTrainer().getName() : "Unknown");
                intent.putExtra("Intensity", current.getIntensity(userTdee).name());
                intent.putExtra("Date", current.getStartDateTime() != null ? dateFormat.format(current.getStartDateTime()) : "N/A");
                intent.putExtra("StartTime", current.getStartDateTime() != null ? timeFormat.format(current.getStartDateTime()) : "N/A");
                intent.putExtra("EndTime", current.getEndDateTime() != null ? timeFormat.format(current.getEndDateTime()) : "N/A");
                intent.putExtra("CurCap", String.valueOf(current.getCurrentCapacity()));
                intent.putExtra("MaxCap", String.valueOf(current.getMaxCapacity()));
                intent.putExtra("Desc", current.getDescription());
                intent.putExtra("AvgCalorie", current.getAvgCaloriesBurnedPerDay());
                intent.putExtra("ClassId", current.getID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView recCard;
        ImageView recImage;
        TextView recTitle, recInstructor, recIntensity, recDate, recStartTime, recEndTime, recCurCap, recMaxCap, recDesc;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recCard = itemView.findViewById(R.id.recycler_class_card);
            recImage = itemView.findViewById(R.id.recycler_class_iv_class);
            recTitle = itemView.findViewById(R.id.recycler_class_tv_title);
            recInstructor = itemView.findViewById(R.id.recycler_class_tv_instructor);
            recIntensity = itemView.findViewById(R.id.recycler_class_tv_intensity);
            recDate = itemView.findViewById(R.id.recycler_class_tv_date);
            recStartTime = itemView.findViewById(R.id.recycler_class_tv_startTime);
            recEndTime = itemView.findViewById(R.id.recycler_class_tv_endTime);
            recCurCap = itemView.findViewById(R.id.recycler_class_tv_currentCapacity);
            recMaxCap = itemView.findViewById(R.id.recycler_class_tv_maxCapacity);
            recDesc = itemView.findViewById(R.id.classDescription);
        }
    }

}
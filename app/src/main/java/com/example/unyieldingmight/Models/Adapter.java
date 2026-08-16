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
//    ArrayList of class GymClass is defined as object dataList
    private ArrayList<GymClass> dataList;
    private float userTdee;

    public Adapter(Context context, ArrayList<GymClass> dataList, float userTdee) {
        this.context = context;
        this.dataList = dataList;
        this.userTdee = userTdee;
    }

//    Wraps the view object inside of ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_class, parent, false);
        return new ViewHolder(view);
    }

//    Binds the data from <GymClass> to the views within RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Gets the position of class within dataList and uses the position to retrieve an object of GymClass
        GymClass gymClass = dataList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        Sets the image,texts, and values to the corresponding views within the holder
        holder.recImage.setImageResource(R.drawable.boxfit);
        holder.recTitle.setText(gymClass.getName());
        holder.recInstructor.setText(gymClass.getTrainer().getName());

        GymClass.Intensity intensity = gymClass.getIntensity(userTdee);
        holder.recIntensity.setText(intensity.name());

        holder.recDate.setText(dateFormat.format(gymClass.getStartDateTime()));
        holder.recStartTime.setText(timeFormat.format(gymClass.getStartDateTime()));
        holder.recEndTime.setText(timeFormat.format(gymClass.getEndDateTime()));

        holder.recDesc.setText(gymClass.getDescription());
        holder.recCurCap.setText(String.valueOf(gymClass.getCurrentCapacity()));
        holder.recMaxCap.setText(String.valueOf(gymClass.getMaxCapacity()));
//        Listens if a card gets clicked
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Gets the current position of the card
                int pos = holder.getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                GymClass current = dataList.get(pos);
//                New intent to start the activity InfoActivity
                Intent intent = new Intent(context, InfoActivity.class);
//                Transfers the class data to InfoActivity so it can be displayed
                intent.putExtra("Image", R.drawable.boxfit);
                intent.putExtra("Title", current.getName());
                intent.putExtra("Instructor", current.getTrainer().getName());
                intent.putExtra("Intensity", current.getIntensity(userTdee).name());
                intent.putExtra("Date", dateFormat.format(current.getStartDateTime()));
                intent.putExtra("StartTime", timeFormat.format(current.getStartDateTime()));
                intent.putExtra("EndTime", timeFormat.format(current.getEndDateTime()));
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
//    Holds the view for each item in the recycler view
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
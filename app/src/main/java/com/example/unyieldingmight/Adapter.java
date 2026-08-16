package com.example.unyieldingmight;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
//    A list of type class
    private List<Class> dataList;
    public void setSearchList(List<Class> dataSearchList){
        this.dataList = dataSearchList;
        notifyDataSetChanged();
    }
    public Adapter(Context context, List<Class> dataList){
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_class, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.recImage.setImageResource(dataList.get(position).getImage());
        holder.recTitle.setText(dataList.get(position).getTitle());
        holder.recInstructor.setText(dataList.get(position).getInstructor());
        holder.recIntensity.setText(dataList.get(position).getIntensity());
        holder.recDate.setText(dataList.get(position).getDate());
        holder.recStartTime.setText(dataList.get(position).getStartTime());
        holder.recEndTime.setText(dataList.get(position).getEndTime());
        holder.recDesc.setText(dataList.get(position).getDescription());
        holder.recCurCap.setText(dataList.get(position).getCurCap());
        holder.recMaxCap.setText(dataList.get(position).getMaxCap());
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra("Image", dataList.get(holder.getAbsoluteAdapterPosition()).getImage());
                intent.putExtra("Title", dataList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                intent.putExtra("Instructor", dataList.get(holder.getAbsoluteAdapterPosition()).getInstructor());
                intent.putExtra("Intensity", dataList.get(holder.getAbsoluteAdapterPosition()).getIntensity());
                intent.putExtra("Date", dataList.get(holder.getAbsoluteAdapterPosition()).getDate());
                intent.putExtra("StartTime", dataList.get(holder.getAbsoluteAdapterPosition()).getStartTime());
                intent.putExtra("EndTime", dataList.get(holder.getAbsoluteAdapterPosition()).getEndTime());
                intent.putExtra("CurCap", dataList.get(holder.getAbsoluteAdapterPosition()).getCurCap());
                intent.putExtra("MaxCap", dataList.get(holder.getAbsoluteAdapterPosition()).getMaxCap());
                intent.putExtra("Desc", dataList.get(holder.getAbsoluteAdapterPosition()).getDescription());
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
// Finds the components from recycler_class via their IDs
class ViewHolder extends RecyclerView.ViewHolder{
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

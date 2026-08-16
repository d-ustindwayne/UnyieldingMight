package com.example.unyieldingmight.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.unyieldingmight.activities.InfoActivity
import com.example.unyieldingmight.R
import java.text.SimpleDateFormat
import java.util.Locale

class Adapter(
    private val context: Context,
    private val dataList: ArrayList<GymClass>?,
    private val userTDEE: Float
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_class, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gymClass = dataList?.get(position) ?: return
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        holder.recImage.setImageResource(R.drawable.boxfit)
        holder.recTitle.text = gymClass.name
        holder.recInstructor.text = gymClass.trainer?.name

        val intensity = gymClass.intensity
        holder.recIntensity.text = intensity.name

        holder.recDate.text = gymClass.startDateTime?.let { dateFormat.format(it) }
        holder.recStartTime.text = gymClass.startDateTime?.let { timeFormat.format(it) }
        holder.recEndTime.text = gymClass.endDateTime?.let { timeFormat.format(it) }

        holder.recDesc.text = gymClass.description
        holder.recCurCap.text = gymClass.currentCapacity.toString()
        holder.recMaxCap.text = gymClass.maxCapacity.toString()

        holder.recCard.setOnClickListener {
            val pos = holder.absoluteAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val current = dataList[pos]
            
            val intent = Intent(context, InfoActivity::class.java).apply {
                putExtra("Image", R.drawable.boxfit)
                putExtra("Title", current.name)
                putExtra("Instructor", current.trainer?.name)
                putExtra("Intensity", current.intensity.name)
                putExtra("Date", current.startDateTime?.let { dateFormat.format(it) })
                putExtra("StartTime", current.startDateTime?.let { timeFormat.format(it) })
                putExtra("EndTime", current.endDateTime?.let { timeFormat.format(it) })
                putExtra("CurCap", current.currentCapacity.toString())
                putExtra("MaxCap", current.maxCapacity.toString())
                putExtra("Desc", current.description)
                putExtra("AvgCalorie", current.avgCaloriesBurnedPerDay)
                putExtra("ClassId", current.ID)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = dataList?.size ?: 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recCard: CardView = itemView.findViewById(R.id.recycler_class_card)
        val recImage: ImageView = itemView.findViewById(R.id.recycler_class_iv_class)
        val recTitle: TextView = itemView.findViewById(R.id.recycler_class_tv_title)
        val recInstructor: TextView = itemView.findViewById(R.id.recycler_class_tv_instructor)
        val recIntensity: TextView = itemView.findViewById(R.id.recycler_class_tv_intensity)
        val recDate: TextView = itemView.findViewById(R.id.recycler_class_tv_date)
        val recStartTime: TextView = itemView.findViewById(R.id.recycler_class_tv_startTime)
        val recEndTime: TextView = itemView.findViewById(R.id.recycler_class_tv_endTime)
        val recCurCap: TextView = itemView.findViewById(R.id.recycler_class_tv_currentCapacity)
        val recMaxCap: TextView = itemView.findViewById(R.id.recycler_class_tv_maxCapacity)
        val recDesc: TextView = itemView.findViewById(R.id.classDescription)
    }
}


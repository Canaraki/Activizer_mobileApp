package com.example.activizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatsAdapter(private val statsList: List<ExerciseStats>) :
    RecyclerView.Adapter<StatsAdapter.StatsViewHolder>() {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val durationText: TextView = itemView.findViewById(R.id.durationText)
        val caloriesText: TextView = itemView.findViewById(R.id.caloriesText)
        val stepsText: TextView = itemView.findViewById(R.id.stepsText)
        val accuracyText: TextView = itemView.findViewById(R.id.accuracyText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val currentItem = statsList[position]
        
        holder.exerciseNameText.text = currentItem.exerciseName
        holder.dateText.text = currentItem.date
        holder.durationText.text = "Duration: ${currentItem.duration}s"
        holder.caloriesText.text = "Calories: ${currentItem.caloriesBurned}"
        holder.stepsText.text = "Steps: ${currentItem.steps}"
        holder.accuracyText.text = "Accuracy: ${currentItem.accuracy}%"
    }

    override fun getItemCount() = statsList.size
} 
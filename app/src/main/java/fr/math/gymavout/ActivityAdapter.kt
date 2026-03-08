package fr.math.gymavout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivityAdapter(private val activities: List<Map<String, Any>>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtActivityTitle)
        val category: TextView = view.findViewById(R.id.badgeCategory)
        val description: TextView = view.findViewById(R.id.txtDescription)
        val location: TextView = view.findViewById(R.id.txtActivityLocation)
        val dateTime: TextView = view.findViewById(R.id.txtActivityDateTime)
        val participants: TextView = view.findViewById(R.id.txtActivityParticipants)
        val level: TextView = view.findViewById(R.id.badgeLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.title.text = activity["titre"]?.toString() ?: "Sans titre"
        holder.category.text = activity["sport"]?.toString() ?: "Sport"
        holder.description.text = activity["description"]?.toString() ?: ""
        holder.location.text = activity["lieu"]?.toString() ?: "Non spécifié"
        
        val date = activity["date"]?.toString() ?: ""
        val heure = activity["heure"]?.toString() ?: ""
        holder.dateTime.text = if (date.isNotEmpty()) "$date à $heure" else "Date non définie"
        
        val maxPart = activity["maxParticipants"]?.toString() ?: "0"
        holder.participants.text = "0 / $maxPart participants"

        holder.level.text = activity["niveau"]?.toString() ?: "Tous niveaux"
    }

    override fun getItemCount() = activities.size
}

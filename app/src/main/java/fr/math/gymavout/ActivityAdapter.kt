package fr.math.gymavout

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ActivityAdapter(private var activities: List<Map<String, Any>>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtActivityTitle)
        val category: TextView = view.findViewById(R.id.badgeCategory)
        val description: TextView = view.findViewById(R.id.txtDescription)
        val location: TextView = view.findViewById(R.id.txtActivityLocation)
        val dateTime: TextView = view.findViewById(R.id.txtActivityDateTime)
        val participants: TextView = view.findViewById(R.id.txtActivityParticipants)
        val level: TextView = view.findViewById(R.id.badgeLevel)
        val imgOrganisateur: ImageView = view.findViewById(R.id.profileImage) // Assurez-vous que cet ID existe dans item_activity.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        val context = holder.itemView.context

        holder.title.text = activity["titre"]?.toString() ?: "Sans titre"
        holder.category.text = activity["sport"]?.toString() ?: "Sport"
        holder.description.text = activity["description"]?.toString() ?: ""
        holder.location.text = activity["lieu"]?.toString() ?: "Non spécifié"
        
        val date = activity["date"]?.toString() ?: ""
        val heure = activity["heure"]?.toString() ?: ""
        holder.dateTime.text = if (date.isNotEmpty()) "$date à $heure" else "Date non définie"
        
        val maxPart = activity["maxParticipants"]?.toString() ?: "0"
        holder.participants.text = "1 / $maxPart participants"
        holder.level.text = activity["niveau"]?.toString() ?: "Tous niveaux"

        // Chargement de l'image de l'organisateur (ou logo par défaut)
        val imageUrl = activity["organisateurImage"]?.toString()
        Glide.with(context)
            .load(if (!imageUrl.isNullOrEmpty()) imageUrl else R.drawable.logo_gymavout)
            .circleCrop()
            .placeholder(R.drawable.logo_gymavout)
            .error(R.drawable.logo_gymavout)
            .into(holder.imgOrganisateur)

        // Rendre l'item cliquable pour ouvrir les détails
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityDetailActivity::class.java)
            intent.putExtra("activityId", activity["id"]?.toString())
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = activities.size

    fun updateList(newList: List<Map<String, Any>>) {
        activities = newList
        notifyDataSetChanged()
    }
}

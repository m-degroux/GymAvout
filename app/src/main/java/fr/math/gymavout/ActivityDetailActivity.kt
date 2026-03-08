package fr.math.gymavout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityDetailActivity : AppCompatActivity() {
    private var organisateurId: String? = null
    private var activityTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)

        val activityId = intent.getStringExtra("activityId") ?: return
        val database = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val imgOrganisateur = findViewById<ImageView>(R.id.imgOrganisateur)
        val txtOrganisateurName = findViewById<TextView>(R.id.txtOrganisateurName)
        val txtTitle = findViewById<TextView>(R.id.txtDetailTitle)
        val txtDescription = findViewById<TextView>(R.id.txtDetailDescription)
        val txtLocation = findViewById<TextView>(R.id.txtDetailLocation)
        val txtDate = findViewById<TextView>(R.id.txtDetailDate)
        val txtTime = findViewById<TextView>(R.id.txtDetailTime)
        val txtParticipants = findViewById<TextView>(R.id.txtDetailParticipants)
        val tagSport = findViewById<TextView>(R.id.tagSport)
        val tagLevel = findViewById<TextView>(R.id.tagLevel)
        val btnParticiper = findViewById<Button>(R.id.btnParticiper)
        val btnMessage = findViewById<ImageButton>(R.id.btnMessageActivity)
        val avatarContainer = findViewById<LinearLayout>(R.id.participantsAvatarContainer)
        val progressBar = findViewById<ProgressBar>(R.id.progressParticipants)

        btnBack.setOnClickListener { finish() }

        database.child("activities").child(activityId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    activityTitle = snapshot.child("titre").value?.toString()
                    val sport = snapshot.child("sport").value?.toString() ?: ""
                    val desc = snapshot.child("description").value?.toString() ?: ""
                    val loc = snapshot.child("lieu").value?.toString() ?: ""
                    val date = snapshot.child("date").value?.toString() ?: ""
                    val time = snapshot.child("heure").value?.toString() ?: ""
                    val maxPartStr = snapshot.child("maxParticipants").value?.toString() ?: "10"
                    val maxPart = maxPartStr.toIntOrNull() ?: 10
                    val level = snapshot.child("niveau").value?.toString() ?: ""
                    organisateurId = snapshot.child("createurId").value?.toString()

                    val participantsSnapshot = snapshot.child("participants")
                    val nbInscrits = participantsSnapshot.childrenCount

                    txtTitle.text = activityTitle
                    tagSport.text = sport
                    txtDescription.text = desc
                    txtLocation.text = loc
                    txtDate.text = date
                    txtTime.text = time
                    tagLevel.text = level
                    txtParticipants.text = "$nbInscrits/$maxPart inscrits"

                    progressBar.max = maxPart
                    progressBar.progress = nbInscrits.toInt()

                    // Affichage des avatars
                    avatarContainer.removeAllViews()
                    for (participant in participantsSnapshot.children) {
                        val pId = participant.key ?: continue
                        database.child("users").child(pId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(uSnap: DataSnapshot) {
                                val url = uSnap.child("profileImageUrl").value?.toString()
                                val img = ShapeableImageView(this@ActivityDetailActivity)
                                val size = (35 * resources.displayMetrics.density).toInt()
                                val params = LinearLayout.LayoutParams(size, size)
                                params.setMargins(0, 0, (-10 * resources.displayMetrics.density).toInt(), 0)
                                img.layoutParams = params
                                img.scaleType = ImageView.ScaleType.CENTER_CROP
                                img.shapeAppearanceModel = img.shapeAppearanceModel.toBuilder()
                                    .setAllCornerSizes(50f * resources.displayMetrics.density)
                                    .build()

                                Glide.with(this@ActivityDetailActivity)
                                    .load(if (!url.isNullOrEmpty()) url else R.drawable.logo_gymavout)
                                    .placeholder(R.drawable.logo_gymavout)
                                    .into(img)
                                avatarContainer.addView(img)
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                    if (currentUserId != null && participantsSnapshot.hasChild(currentUserId)) {
                        btnParticiper.text = "Déjà inscrit"
                        btnParticiper.isEnabled = false
                        btnParticiper.alpha = 0.5f
                    }

                    organisateurId?.let { id ->
                        database.child("users").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val prenom = userSnapshot.child("prenom").value?.toString() ?: ""
                                val nom = userSnapshot.child("nom").value?.toString() ?: ""
                                val imageUrl = userSnapshot.child("profileImageUrl").value?.toString()
                                txtOrganisateurName.text = "$prenom $nom"
                                Glide.with(this@ActivityDetailActivity)
                                    .load(if (!imageUrl.isNullOrEmpty()) imageUrl else R.drawable.logo_gymavout)
                                    .circleCrop()
                                    .into(imgOrganisateur)
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        btnParticiper.setOnClickListener {
            if (currentUserId != null) {
                database.child("activities").child(activityId).child("participants").child(currentUserId).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                        // Envoyer message auto
                        envoieMessageAuto(currentUserId, organisateurId, activityTitle)
                    }
            }
        }

        btnMessage.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("partnerId", organisateurId)
            startActivity(intent)
        }
    }

    private fun envoieMessageAuto(myId: String, partnerId: String?, title: String?) {
        if (partnerId == null || myId == partnerId) return
        val db = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val chatId = if (myId < partnerId) "${myId}_${partnerId}" else "${partnerId}_${myId}"
        val msg = "Bonjour! Je souhaite participer à votre activité : $title"
        val messageData = mapOf(
            "senderId" to myId,
            "message" to msg,
            "timestamp" to System.currentTimeMillis()
        )
        db.child("chats").child(chatId).push().setValue(messageData)
    }
}

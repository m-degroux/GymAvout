package fr.math.gymavout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilFragment : Fragment(R.layout.fragment_profil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation des vues
        val txtProfileName = view.findViewById<TextView>(R.id.txtProfileName)
        val txtProfileLocation = view.findViewById<TextView>(R.id.txtProfileLocation)
        val txtProfileBio = view.findViewById<TextView>(R.id.txtProfileBio)
        val txtPoints = view.findViewById<TextView>(R.id.txtPoints)
        val txtCreated = view.findViewById<TextView>(R.id.txtCreated)
        val txtLevel = view.findViewById<TextView>(R.id.txtLevel)
        val chipGroupSports = view.findViewById<ChipGroup>(R.id.chipGroupSports)
        val imgProfile = view.findViewById<ImageView>(R.id.profileImage)
        val txtNote = view.findViewById<TextView>(R.id.txtNote)
        val btnEdit = view.findViewById<ImageView>(R.id.btnEdit)
        val txtJoinDate = view.findViewById<TextView>(R.id.txtJoinDate)
        val btnSettings = view.findViewById<ImageView>(R.id.btnSettings)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        
        if (user != null) {
            val dbRef = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/")
                .reference.child("users").child(user.uid)

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    if (snapshot.exists()) {
                        val prenom = snapshot.child("prenom").getValue(String::class.java) ?: ""
                        val nom = snapshot.child("nom").getValue(String::class.java) ?: ""
                        val pseudo = snapshot.child("pseudo").getValue(String::class.java) ?: "Utilisateur"
                        val lieu = snapshot.child("lieu").getValue(String::class.java) ?: "Non précisé"
                        val bio = snapshot.child("bio").getValue(String::class.java) ?: "Aucune bio rédigée."
                        val sportsStr = snapshot.child("sports").getValue(String::class.java) ?: ""
                        val niveau = snapshot.child("niveau").getValue(String::class.java) ?: "Tous niveaux"
                        
                        val points = snapshot.child("points").value?.toString() ?: "0"
                        val created = snapshot.child("creations").value?.toString() ?: "0"
                        val note = snapshot.child("note").value?.toString() ?: "4.8"

                        txtProfileName?.text = if (prenom.isNotEmpty()) "$prenom $nom" else pseudo
                        txtProfileLocation?.text = lieu
                        txtProfileBio?.text = bio
                        txtLevel?.text = niveau
                        txtPoints?.text = points
                        txtCreated?.text = created
                        txtNote?.text = note
                        txtJoinDate?.text = "Membre de l'équipe"
                        
                        // Affichage des sports avec le fond vert foncé (comme les stats)
                        chipGroupSports?.removeAllViews()
                        if (sportsStr.isNotEmpty()) {
                            val sportsList = sportsStr.split(", ")
                            for (sport in sportsList) {
                                val chip = Chip(requireContext())
                                chip.text = sport
                                chip.setChipBackgroundColorResource(R.color.gym_green_primary) // Même vert que Participations/Créées
                                chip.setTextColor(resources.getColor(R.color.white, null))
                                chip.chipStrokeWidth = 0f
                                chip.chipCornerRadius = 24f
                                chipGroupSports?.addView(chip)
                            }
                        }

                        val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                        if (imgProfile != null) {
                            Glide.with(this@ProfilFragment)
                                .load(imageUrl ?: R.drawable.logo_gymavout)
                                .circleCrop()
                                .placeholder(R.drawable.logo_gymavout)
                                .error(R.drawable.logo_gymavout)
                                .into(imgProfile)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Erreur de lecture: ${error.message}")
                }
            })
        }

        btnEdit?.setOnClickListener {
            startActivity(Intent(requireContext(), SetupProfileActivity::class.java))
        }

        btnSettings?.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }
}

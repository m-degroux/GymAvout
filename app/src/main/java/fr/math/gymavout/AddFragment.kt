package fr.math.gymavout

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AddFragment : Fragment(R.layout.fragment_add) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val auth = FirebaseAuth.getInstance()

        val btnPublish = view.findViewById<Button>(R.id.btnPublish)
        val etTitle = view.findViewById<EditText>(R.id.etActivityTitle)
        val spinnerSport = view.findViewById<Spinner>(R.id.sportSpinner)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etTime = view.findViewById<EditText>(R.id.etTime)
        val etMaxParticipants = view.findViewById<EditText>(R.id.etMaxParticipants)
        val spinnerLevels = view.findViewById<Spinner>(R.id.spinnerLevels)

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, yearSel, monthSel, daySel ->
                etDate.setText(String.format("%02d/%02d/%d", daySel, monthSel + 1, yearSel))
            }, year, month, day)
            dpd.show()
        }

        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            val tpd = TimePickerDialog(requireContext(), { _, hourSel, minSel ->
                etTime.setText(String.format("%02d:%02d", hourSel, minSel))
            }, hour, minute, true)
            tpd.show()
        }

        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val sport = spinnerSport.selectedItem?.toString() ?: ""
            val description = etDescription.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val maxParticipants = etMaxParticipants.text.toString().trim()
            val level = spinnerLevels.selectedItem?.toString() ?: ""
            val userId = auth.currentUser?.uid

            if (title.isEmpty() || sport.isEmpty() || description.isEmpty() || 
                location.isEmpty() || date.isEmpty() || time.isEmpty() || 
                maxParticipants.isEmpty() || level.isEmpty()) {
                
                Toast.makeText(context, "Veuillez remplir tous les champs obligatoires (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                btnPublish.isEnabled = false
                btnPublish.text = "Publication..."

                val activityId = database.child("activities").push().key
                
                database.child("users").child(userId).child("profileImageUrl").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userImageUrl = snapshot.getValue(String::class.java) ?: ""
                        
                        val activityData = mutableMapOf<String, Any>(
                            "id" to (activityId ?: ""),
                            "titre" to title,
                            "sport" to sport,
                            "description" to description,
                            "lieu" to location,
                            "date" to date,
                            "heure" to time,
                            "maxParticipants" to maxParticipants,
                            "niveau" to level,
                            "createurId" to userId,
                            "organisateurImage" to userImageUrl,
                            "timestamp" to System.currentTimeMillis(),
                            "nbInscrits" to 1 // Le créateur est le premier inscrit
                        )

                        activityId?.let { id ->
                            database.child("activities").child(id).setValue(activityData)
                                .addOnSuccessListener {
                                    // Ajouter le créateur à la liste des participants
                                    database.child("activities").child(id).child("participants").child(userId).setValue(true)

                                    // Incrémenter le compteur de créations de l'utilisateur
                                    database.child("users").child(userId).child("creations")
                                        .setValue(ServerValue.increment(1))
                                    
                                    Toast.makeText(context, "Activité publiée !", Toast.LENGTH_SHORT).show()
                                    btnPublish.isEnabled = true
                                    btnPublish.text = "Publier l'activité"
                                    
                                    etTitle.text.clear()
                                    etDescription.text.clear()
                                    etLocation.text.clear()
                                    etDate.text.clear()
                                    etTime.text.clear()
                                    etMaxParticipants.text.clear()
                                }
                                .addOnFailureListener { e ->
                                    btnPublish.isEnabled = true
                                    btnPublish.text = "Publier l'activité"
                                    Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        btnPublish.isEnabled = true
                        btnPublish.text = "Publier l'activité"
                    }
                })
            }
        }
    }
}

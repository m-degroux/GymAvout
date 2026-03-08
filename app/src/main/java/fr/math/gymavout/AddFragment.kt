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
import com.google.firebase.database.FirebaseDatabase
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

        // Gestion de la Date
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

        // Gestion de l'Heure
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

            // Vérification des champs obligatoires
            if (title.isEmpty() || sport.isEmpty() || description.isEmpty() || 
                location.isEmpty() || date.isEmpty() || time.isEmpty() || 
                maxParticipants.isEmpty() || level.isEmpty()) {
                
                Toast.makeText(context, "Veuillez remplir tous les champs obligatoires (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                val activityId = database.child("activities").push().key
                val activityData = mapOf(
                    "id" to activityId,
                    "titre" to title,
                    "sport" to sport,
                    "description" to description,
                    "lieu" to location,
                    "date" to date,
                    "heure" to time,
                    "maxParticipants" to maxParticipants,
                    "niveau" to level,
                    "createurId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )

                activityId?.let {
                    database.child("activities").child(it).setValue(activityData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Activité publiée !", Toast.LENGTH_SHORT).show()
                            // Vider les champs
                            etTitle.text.clear()
                            etDescription.text.clear()
                            etLocation.text.clear()
                            etDate.text.clear()
                            etTime.text.clear()
                            etMaxParticipants.text.clear()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}

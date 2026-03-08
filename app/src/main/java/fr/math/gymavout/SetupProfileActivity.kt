package fr.math.gymavout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbUrl = "https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/"
    
    private var selectedSports = mutableListOf<String>()
    private lateinit var tvSports: TextView
    private lateinit var imgProfile: ImageView
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imgProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance(dbUrl).reference
        val storage = FirebaseStorage.getInstance().reference

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etLocation = findViewById<EditText>(R.id.etUserLocation)
        val etBio = findViewById<EditText>(R.id.etBio)
        tvSports = findViewById(R.id.tvSelectedSports)
        val spinnerLevel = findViewById<Spinner>(R.id.spinnerUserLevel)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        imgProfile = findViewById(R.id.imgSetupProfile)

        // Charger les données existantes
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        etFirstName.setText(snapshot.child("prenom").getValue(String::class.java) ?: "")
                        etLastName.setText(snapshot.child("nom").getValue(String::class.java) ?: "")
                        etLocation.setText(snapshot.child("lieu").getValue(String::class.java) ?: "")
                        etBio.setText(snapshot.child("bio").getValue(String::class.java) ?: "")
                        
                        val sportsStr = snapshot.child("sports").getValue(String::class.java) ?: ""
                        if (sportsStr.isNotEmpty()) {
                            selectedSports = sportsStr.split(", ").toMutableList()
                            tvSports.text = sportsStr
                        }

                        val niveau = snapshot.child("niveau").getValue(String::class.java) ?: ""
                        val levels = resources.getStringArray(R.array.levels_array)
                        val levelIndex = levels.indexOf(niveau)
                        if (levelIndex >= 0) spinnerLevel.setSelection(levelIndex)

                        val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this@SetupProfileActivity)
                                .load(imageUrl)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        findViewById<View>(R.id.frameProfileImage).setOnClickListener {
            pickImage.launch("image/*")
        }

        tvSports.setOnClickListener {
            showSportsDialog()
        }

        btnSave.setOnClickListener {
            val first = etFirstName.text.toString().trim()
            val last = etLastName.text.toString().trim()
            val loc = etLocation.text.toString().trim()
            val bio = etBio.text.toString().trim()
            val level = spinnerLevel.selectedItem.toString()

            if (first.isEmpty() || last.isEmpty() || loc.isEmpty() || selectedSports.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId != null) {
                btnSave.isEnabled = false
                btnSave.text = "Enregistrement..."

                if (imageUri != null) {
                    val fileRef = storage.child("profile_images/$userId.jpg")
                    fileRef.putFile(imageUri!!).addOnSuccessListener {
                        fileRef.downloadUrl.addOnSuccessListener { url ->
                            saveUserData(database, userId, first, last, loc, bio, level, url.toString())
                        }
                    }.addOnFailureListener {
                        saveUserData(database, userId, first, last, loc, bio, level, null)
                    }
                } else {
                    saveUserData(database, userId, first, last, loc, bio, level, null)
                }
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveUserData(database: com.google.firebase.database.DatabaseReference, userId: String, first: String, last: String, loc: String, bio: String, level: String, imageUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "prenom" to first,
            "nom" to last,
            "lieu" to loc,
            "bio" to bio,
            "sports" to selectedSports.joinToString(", "),
            "niveau" to level,
            "profileComplete" to true
        )
        if (imageUrl != null) updates["profileImageUrl"] = imageUrl

        database.child("users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                findViewById<Button>(R.id.btnSaveProfile).isEnabled = true
                findViewById<Button>(R.id.btnSaveProfile).text = "Enregistrer"
            }
    }

    private fun showSportsDialog() {
        val sportsArray = resources.getStringArray(R.array.sport_array).filter { it != "Sélectionner un sport" }.toTypedArray()
        val checkedItems = BooleanArray(sportsArray.size) { i -> selectedSports.contains(sportsArray[i]) }

        AlertDialog.Builder(this)
            .setTitle("Choisissez vos sports")
            .setMultiChoiceItems(sportsArray, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    if (!selectedSports.contains(sportsArray[which])) selectedSports.add(sportsArray[which])
                } else {
                    selectedSports.remove(sportsArray[which])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                tvSports.text = if (selectedSports.isEmpty()) "Sélectionner vos sports" else selectedSports.joinToString(", ")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}

package fr.math.gymavout

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Start : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbUrl = "https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Si l'utilisateur est déjà connecté, vérifier s'il a complété son profil
        if (auth.currentUser != null) {
            checkProfileCompletion()
            return
        }

        setContentView(R.layout.start_page)

        val database = FirebaseDatabase.getInstance(dbUrl).reference

        val tabLogin = findViewById<Button>(R.id.tabLogin)
        val tabRegister = findViewById<Button>(R.id.tabRegister)
        val formLogin = findViewById<LinearLayout>(R.id.formLogin)
        val formRegister = findViewById<LinearLayout>(R.id.formRegister)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val emailLogin = findViewById<EditText>(R.id.etLoginEmail)
        val passLogin = findViewById<EditText>(R.id.etLoginPassword)
        val emailReg = findViewById<EditText>(R.id.etRegisterEmail)
        val passReg = findViewById<EditText>(R.id.etRegisterPassword)
        val pseudoReg = findViewById<EditText>(R.id.etRegisterName)

        btnRegister?.setOnClickListener {
            val email = emailReg.text.toString().trim()
            val password = passReg.text.toString().trim()
            val pseudo = pseudoReg.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6 && pseudo.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val userProfile = mapOf(
                                "pseudo" to pseudo,
                                "email" to email,
                                "niveau" to "Débutant",
                                "points" to 0,
                                "profileComplete" to false
                            )
                            userId?.let { database.child("users").child(it).setValue(userProfile) }

                            // Après inscription, on va à la page de configuration du profil
                            startActivity(Intent(this, SetupProfileActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs (MDP: 6 car. min)", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = emailLogin.text.toString().trim()
            val password = passLogin.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkProfileCompletion()
                        } else {
                            Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        tabLogin.setOnClickListener { toggleTabs(true, tabLogin, tabRegister, formLogin, formRegister) }
        tabRegister.setOnClickListener { toggleTabs(false, tabLogin, tabRegister, formLogin, formRegister) }
    }

    private fun checkProfileCompletion() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(dbUrl).reference.child("users").child(userId)
            dbRef.get().addOnSuccessListener { snapshot ->
                val complete = snapshot.child("profileComplete").getValue(Boolean::class.java) ?: false
                if (complete) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, SetupProfileActivity::class.java))
                }
                finish()
            }
        }
    }

    private fun toggleTabs(isLogin: Boolean, b1: Button, b2: Button, f1: View, f2: View) {
        val green = ContextCompat.getColor(this, R.color.gym_green_primary)
        val gray = ContextCompat.getColor(this, R.color.text_gray)
        b1.backgroundTintList = ColorStateList.valueOf(if (isLogin) green else Color.TRANSPARENT)
        b1.setTextColor(if (isLogin) Color.WHITE else gray)
        b2.backgroundTintList = ColorStateList.valueOf(if (isLogin) Color.TRANSPARENT else green)
        b2.setTextColor(if (isLogin) gray else Color.WHITE)
        f1.visibility = if (isLogin) View.VISIBLE else View.GONE
        f2.visibility = if (isLogin) View.GONE else View.VISIBLE
    }
}

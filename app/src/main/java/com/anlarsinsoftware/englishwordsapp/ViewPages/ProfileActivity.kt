package com.anlarsinsoftware.englishwordsapp.ViewPages



import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var profileImage: ImageView
    private lateinit var textName: TextView
    private lateinit var textEmail: TextView
    private lateinit var btnConnectReport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = Firebase.auth

        profileImage = findViewById(R.id.profileImage)
        textName = findViewById(R.id.textName)
        textEmail = findViewById(R.id.textEmail)
        btnConnectReport = findViewById(R.id.btnConnectReport)

        val currentUser = auth.currentUser

        currentUser?.let {
            textName.text = it.displayName ?: "İsim girilmedi"
            textEmail.text = it.email ?: "E-posta yok"


            it.photoUrl?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.profilIcon)
                    .into(profileImage)
            }
        }


        btnConnectReport.setOnClickListener {
            Toast.makeText(this, "Raporlama sistemine bağlanılıyor...", Toast.LENGTH_SHORT).show()

        }
    }
}

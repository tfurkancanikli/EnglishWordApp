package com.anlarsinsoftware.englishwordsapp.ViewPages



import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProfileActivity : BaseCompact() {

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
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(profileImage)
            }
        }


        btnConnectReport.setOnClickListener {
            bagla(RaporPage::class.java,false)
            Toast.makeText(this, "Raporlama sistemine bağlanılıyor...", Toast.LENGTH_SHORT).show()

        }
    }
    fun exitClick(view: View) {
        auth.signOut()
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Uygulamadan Çıkılsın mı?")
            .setMessage("Çıkış yapmak istediğinize emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                dialog.dismiss()
                auth.signOut()
                bagla(SignInActivity::class.java, true)
                finishAffinity()
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

}

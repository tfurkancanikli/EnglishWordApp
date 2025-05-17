package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
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

    private lateinit var imageSettings: ImageView // ayarlar imageview

    private val admin_kodu = "projeyazilimyapimi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = Firebase.auth

        profileImage = findViewById(R.id.profileImage)
        textName = findViewById(R.id.textName)
        textEmail = findViewById(R.id.textEmail)
        btnConnectReport = findViewById(R.id.btnConnectReport)
        imageSettings = findViewById(R.id.imageSettings) // ayarlar iconu bul

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

        imageSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ayarlar")
        builder.setMessage("Seçenekler")
        builder.setPositiveButton("Admin Girişi") { dialog, _ ->
            dialog.dismiss()
            showAdminLoginDialog()
        }
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showAdminLoginDialog() {
        val editText = EditText(this)
        editText.setText(admin_kodu) // önceden verilen admin kodu otomatik olarak girilir

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Admin Girişi")
        builder.setView(editText)
        builder.setPositiveButton("Giriş") { dialog, _ ->
            val enteredCode = editText.text.toString()
            if (enteredCode == admin_kodu) {
                Toast.makeText(this, "Hoş geldin admin", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Admin kodu yanlış!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
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

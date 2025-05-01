package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityHomePageBinding
import com.anlarsinsoftware.englishwordsapp.databinding.ActivitySignInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class HomePageActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var binding : ActivityHomePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth

        val bulmacaButon = findViewById<Button>(R.id.bulmacaButon)
        bulmacaButon.setOnClickListener {
            val intent = Intent(this, BulmacaOyunu::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
    fun exitClick(view: View){
        auth.signOut()
        bagla(SignInActivity::class.java)
    }
    fun kelimeEklePage(view:View){
        bagla(WordAddPage::class.java)
    }
    fun sozlukButtonClick(view:View){
        bagla(Sozluk::class.java)
    }
}





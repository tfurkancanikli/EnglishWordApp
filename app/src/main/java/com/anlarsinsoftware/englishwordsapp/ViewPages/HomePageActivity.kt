package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
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
    }
    fun exitClick(view: View){
        auth.signOut()
        intent = Intent(this,SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
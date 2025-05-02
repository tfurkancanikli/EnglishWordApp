package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRaporPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class RaporPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityRaporPageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var reportLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaporPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth
        db = FirebaseFirestore.getInstance()



        }
    }


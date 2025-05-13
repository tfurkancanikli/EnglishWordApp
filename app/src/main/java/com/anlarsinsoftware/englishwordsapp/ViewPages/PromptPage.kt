package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityPromptPageBinding

class PromptPage : AppCompatActivity() {
    private lateinit var binding: ActivityPromptPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityPromptPageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}
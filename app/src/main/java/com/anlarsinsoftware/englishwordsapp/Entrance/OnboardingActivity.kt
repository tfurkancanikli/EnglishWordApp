package com.anlarsinsoftware.englishwordsapp.Entrance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.anlarsinsoftware.englishwordsapp.Adapter.OnboardingAdapter
import com.anlarsinsoftware.englishwordsapp.Model.OnboardingItem
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var onboardingItems: List<OnboardingItem>
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        if (isOnboardingShown(this)) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabindicator)
        nextButton = findViewById(R.id.btnNext)

        onboardingItems = listOf(
            OnboardingItem(
                imageResId = R.drawable.logo_eng,
                title = "Penlingu'ya Hoş Geldin!",
                description = "Seninle ingilizce kelimeleri tek kalemde öğrenmek için sabırsızlanıyorum!🖋️"
            ),
            OnboardingItem(
                imageResId = R.drawable.quizpageonboard,
                title = "Hem Test Et Hem Yarış!",
                description = "Quiz sistemi ile öğrenmeni takip ediyoruz. Tekrarla, pekiştir ve sıralamaya girmeye çalış!"
            ),
            OnboardingItem(
                imageResId = R.drawable.yapayzekaonboard,
                title = "Hikâye ve Görsel Üret",
                description = "Seçtiğin kelimelerle hikâye oluştur, yapay zekâ sana resim çizsin!"
            ),
            OnboardingItem(
                imageResId = R.drawable.asamakurali,
                title = "Kalıcı Öğrenme",
                description = "6 aşamalı sistemle öğrendiklerini unutma! Hadi başlayalım!"
            )
        )

        adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter


        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        nextButton.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < onboardingItems.size) {
                viewPager.currentItem = nextItem
            } else {
                goToMainActivity()
            }
        }


    }

    private fun goToMainActivity() {
        setOnboardingShown(this)
        bagla(SignInActivity::class.java,true)
    }

    fun setOnboardingShown(context: Context) {
         getApplicationContext()
        val prefs =  getApplicationContext().getSharedPreferences("penlingu_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_shown", true).apply()
    }

    fun isOnboardingShown(context: Context): Boolean {
        val prefs =  getApplicationContext().getSharedPreferences("penlingu_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("onboarding_shown", false)
    }

}

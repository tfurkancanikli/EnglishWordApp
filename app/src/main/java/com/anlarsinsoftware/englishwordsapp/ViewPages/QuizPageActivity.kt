package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityQuizPageBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class QuizPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizPageBinding

    private lateinit var quizKelimeListesi: List<Kelime>
    private var currentIndex = 0
    private var hak = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getRandomWordsFromFirebase { kelimeler ->
            quizKelimeListesi = kelimeler
            gorselVeSoruGoster()
        }

        binding.dogrulaButton.setOnClickListener {
            dogrulaCevap()
        }
    }

    private fun getRandomWordsFromFirebase(onWordsReady: (List<Kelime>) -> Unit) {
        Firebase.firestore.collection("kelimeler").get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val allWords = snapshot.documents.mapNotNull { doc ->
                    val kullaniciAdi = doc.getString("kullaniciAdi") ?: return@mapNotNull null
                    val kelimeIng = doc.getString("ingilizceKelime") ?: return@mapNotNull null
                    val kelimeTur = doc.getString("turkceKarsiligi") ?: ""
                    val cumle1 = doc.getString("birinciCumle") ?: ""
                    val cumle2 = doc.getString("ikinciCumle") ?: ""
                    val gorselUrl = doc.getString("gorselUrl") ?: ""
                    Kelime(kullaniciAdi, kelimeIng, kelimeTur, cumle1, cumle2, gorselUrl)
                }.shuffled()

                onWordsReady(allWords.take(15))
            } else {
                Toast.makeText(this, "Hiç kelime bulunamadı.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Veri alınamadı: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun gorselVeSoruGoster() {
        if (currentIndex >= quizKelimeListesi.size) {
            Toast.makeText(this, "Quiz tamamlandı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val kelime = quizKelimeListesi[currentIndex]
        Picasso.get().load(kelime.gorselUrl).into(binding.kelimeImage)
        binding.kullaniciCevap.setText("")
        hak = 3
    }

    private fun dogrulaCevap() {
        val cevap = binding.kullaniciCevap.text.toString().trim().lowercase()
        val dogruCevap = quizKelimeListesi[currentIndex].kelimeIng.lowercase()

        if (cevap == dogruCevap) {
            Toast.makeText(this, "Tebrikler! Doğru cevap.", Toast.LENGTH_SHORT).show()
            currentIndex++
            gorselVeSoruGoster()
        } else {
            hak--
            if (hak > 0) {
                Toast.makeText(this, "Yanlış cevap. Kalan hakkınız: $hak", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Doğru cevap: $dogruCevap", Toast.LENGTH_LONG).show()
                currentIndex++
                gorselVeSoruGoster()
            }
        }
    }
}

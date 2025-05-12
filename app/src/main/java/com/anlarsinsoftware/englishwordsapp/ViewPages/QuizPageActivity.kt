package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityQuizPageBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class QuizPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizPageBinding

    private lateinit var quizKelimeListesi: List<Kelime>
    private var currentIndex = 0
    private var hak = 3
    private var dogruSayisi = 0
    private var yanlisSayisi = 0

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
        val radius=50
        val margin=0
        Picasso.get().load(kelime.gorselUrl)
            .transform(RoundedCornersTransformation(radius, margin))
            .into(binding.kelimeImage)

        binding.quizTurkce.setText(kelime.kelimeTur)
        binding.kullaniciCevap.setText("")

        hak = 3
        binding.hakText.setText(hak.toString())
    }

    private fun dogrulaCevap() {
        val cevap = binding.kullaniciCevap.text.toString().trim().lowercase()
        val dogruCevap = quizKelimeListesi[currentIndex].kelimeIng.trim().lowercase()

        if (cevap == dogruCevap) {
            dogruSayisi++
            binding.dogruSayisiTv.setText(dogruSayisi.toString())
            Toast.makeText(this, "Tebrikler! Doğru cevap.", Toast.LENGTH_SHORT).show()
            currentIndex++
            gorselVeSoruGoster()
        } else {
            hak--
            if (hak > 0) {
                Toast.makeText(this, "Yanlış cevap", Toast.LENGTH_SHORT).show()
            }
            else{
                yanlisSayisi++
                binding.yanlisSayisiTv.setText(yanlisSayisi.toString())
                Toast.makeText(this, "Bilemediniz Doğru cevap: $dogruCevap", Toast.LENGTH_LONG).show()
                currentIndex++
                gorselVeSoruGoster()
            }
        }
        binding.hakText.setText(hak.toString())
        when(hak){
            1 -> binding.hakText.setTextColor(Color.RED)
            2-> binding.hakText.setTextColor(Color.YELLOW)
            3-> binding.hakText.setTextColor(Color.GREEN)
        }
    }
}

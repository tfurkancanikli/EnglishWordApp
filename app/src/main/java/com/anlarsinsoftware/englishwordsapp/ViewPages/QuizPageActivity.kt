package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityQuizPageBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

class QuizPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizPageBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var quizKelimeListesi: List<Kelime>
    private var currentIndex = 0
    private var hak = 3
    private var dogruSayisi = 0
    private var yanlisSayisi = 0

    private val zamanAraliklari = listOf(1, 7, 30, 90, 180, 365)

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityQuizPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getKullaniciyaOzelKelimeler()


        binding.dogrulaButton.setOnClickListener {
            dogrulaCevap()
        }
    }

    private fun getKullaniciyaOzelKelimeler() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        val ozelKelimeler = mutableListOf<String>()

        db.collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val asama = doc.getLong("asama") ?: 1
                    val sonTarih = doc.getTimestamp("sonDogruTarih")?.toDate() ?: continue
                    val kelimeId = doc.id

                    val fark = (Date().time - sonTarih.time) / (1000 * 60 * 60 * 24)  // Gün farkı

                    val gerekenGun = when (asama.toInt()) {
                        1 -> 1
                        2 -> 7
                        3 -> 30
                        4 -> 90
                        5 -> 180
                        6 -> 365
                        else -> 999
                    }

                    if (fark >= gerekenGun) {
                        ozelKelimeler.add(kelimeId)
                    }
                }

                getQuizWords(ozelKelimeler)
            }
    }


    private fun gorselVeSoruGoster() {
        if (currentIndex >= quizKelimeListesi.size) {
            Toast.makeText(this, "Quiz tamamlandı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val kelime = quizKelimeListesi[currentIndex]
        val radius = 50
        val margin = 0

        Picasso.get().load(kelime.gorselUrl)
            .transform(RoundedCornersTransformation(radius, margin))
            .into(binding.kelimeImage)

        binding.quizTurkce.setText(kelime.kelimeTur)
        binding.kullaniciCevap.setText("")

        hak = 3
        binding.hakText.setText(hak.toString())
        binding.hakText.setTextColor(Color.GREEN)
    }

    private fun dogrulaCevap() {
        val cevap = binding.kullaniciCevap.text.toString().trim().lowercase()
        val kelime = quizKelimeListesi[currentIndex]
        val dogruCevap = kelime.kelimeIng.trim().lowercase()
        val dogruMu = cevap == dogruCevap

        if (dogruMu) {
            dogruSayisi++
            binding.dogruSayisiTv.text = dogruSayisi.toString()
            Toast.makeText(this, "Tebrikler! Doğru cevap.", Toast.LENGTH_SHORT).show()

            val yeniDogruSayisi = kelime.dogruSayisi + 1
            val docRef = Firebase.firestore.collection("kelimeler").document(kelime.docId)
            docRef.update(
                mapOf(
                    "dogruCevapSayisi" to yeniDogruSayisi,
                    "sonDogruCevapZamani" to Timestamp.now()
                )
            )
        } else {
            hak--
            if (hak > 0) {
                Toast.makeText(this, "Yanlış cevap", Toast.LENGTH_SHORT).show()
            } else {
                yanlisSayisi++
                binding.yanlisSayisiTv.text = yanlisSayisi.toString()
                Toast.makeText(this, "Bilemediniz. Doğru cevap: $dogruCevap", Toast.LENGTH_LONG).show()
            }
        }

        binding.hakText.text = hak.toString()
        when (hak) {
            1 -> binding.hakText.setTextColor(Color.RED)
            2 -> binding.hakText.setTextColor(Color.YELLOW)
            3 -> binding.hakText.setTextColor(Color.GREEN)
        }

        updateKelimeDurumu(kelime.kelimeId, dogruMu,kelime)

        if (dogruMu || hak == 0) {
            currentIndex++
            if (currentIndex < quizKelimeListesi.size) {
                hak = 3
                gorselVeSoruGoster()
            } else {
                Toast.makeText(this, "Quiz tamamlandı!", Toast.LENGTH_LONG).show()
                bagla(HomePageActivity::class.java,true)
            }
        }
    }

    private fun DocumentSnapshot.toKelime(id: String): Kelime {
        val kelime = Kelime(
            kelimeId = id,
            kullaniciAdi = getString("kullaniciAdi") ?: "",
            kelimeIng = getString("ingilizceKelime") ?: "",
            kelimeTur = getString("turkceKarsiligi") ?: "",
            birinciCumle = getString("birinciCumle") ?: "",
            ikinciCumle = getString("ikinciCumle") ?: "",
            gorselUrl = getString("gorselUrl") ?: ""
        )

        kelime.docId = id // 🔑 Firestore belgesinin ID'si
        kelime.dogruSayisi = getLong("dogruCevapSayisi")?.toInt() ?: 0
        kelime.sonDogruMs = getTimestamp("sonDogruCevapZamani")?.toDate()?.time ?: 0L

        return kelime
    }
    private fun updateKelimeDurumu(kelimeId: String, dogruBildi: Boolean,kelime: Kelime) {
        val kelimeIng= kelime.kelimeIng
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val kelimeRef = Firebase.firestore
            .collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .document(kelimeId)

        kelimeRef.get().addOnSuccessListener { doc ->
            val oncekiSayac = doc.getLong("dogruSayac") ?: 0
            val oncekiAsama = doc.getLong("asama") ?: 1

            val name = auth.currentUser!!.displayName


            if (dogruBildi) {
                val yeniSayac = oncekiSayac + 1

                val yeniAsama = if (yeniSayac >= 6) 6 else oncekiAsama + 1
                kelimeRef.set(
                    mapOf(
                        "dogruSayac" to yeniSayac,
                        "asama" to yeniAsama,
                        "sonDogruTarih" to Timestamp.now(),
                        "kullaniciAdi" to name,
                        "Kelime" to kelimeIng
                    )
                )
            } else {
                kelimeRef.set(
                    mapOf(
                        "dogruSayac" to 0,
                        "asama" to 1,
                        "sonDogruTarih" to Timestamp.now(),
                        "kullaniciAdi" to name,
                        "Kelime" to kelimeIng
                    )
                )
            }
        }
    }

    private fun getQuizWords(kelimeIdListesi: List<String>) {
        val db = Firebase.firestore
        val quizKelimeListesi = mutableListOf<Kelime>()

        db.collection("kelimeler").get().addOnSuccessListener { snapshot ->
            val tumKelimeler = snapshot.documents

            val kelimeListesi = mutableListOf<Kelime>()



            // Öncelikli kelimeler
            kelimeIdListesi.forEach { id ->
                val doc = tumKelimeler.find { it.id == id }
                doc?.let {
                    quizKelimeListesi.add(doc.toKelime(it.id))
                }
            }

            // Geri kalanını rastgele tamamla
            val rastgeleKelimeler = tumKelimeler
                .filterNot { kelimeIdListesi.contains(it.id) }
                .shuffled()
                .take(15 - quizKelimeListesi.size)

            rastgeleKelimeler.forEach {
                quizKelimeListesi.add(it.toKelime(it.id))
            }

            this.quizKelimeListesi = quizKelimeListesi
            gorselVeSoruGoster()
        }
    }



}

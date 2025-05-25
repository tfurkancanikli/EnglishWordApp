package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat.postDelayed
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityQuizPageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.util.Date
import java.util.concurrent.TimeUnit

class QuizPageActivity : BaseCompact() {
    private lateinit var binding: ActivityQuizPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var quizKelimeListesi: List<Kelime>
    private var currentIndex = 0
    private var dogruSayisi = 0
    private var yanlisSayisi = 0
    private val zamanAraliklari = listOf(1, 7, 30, 90, 180, 365)

    // Renkler
    private val colorCorrect = Color.parseColor("#4CAF50") // Yeşil
    private val colorWrong = Color.parseColor("#F44336")   // Kırmızı
    private val colorPrimary = Color.parseColor("#3F51B5") // mavi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        setupUI()
        getKullaniciyaOzelKelimeler()
    }

    private fun setupUI() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }
            settingsButton.setOnClickListener { showSettingsBottomSheet() }


            loadingOverlay.visibility = View.VISIBLE
            progressIndicator.progress = 0
            resultFeedback.visibility = View.GONE

            dogrulaButton.setOnClickListener { dogrulaCevap() }


            kullaniciCevap.setOnEditorActionListener { _, _, _ ->
                dogrulaCevap()
                true
            }
        }
    }

    private fun showSettingsBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_quiz_settings, null)
        val bottomSheet = BottomSheetDialog(this).apply {
            setContentView(bottomSheetView)
            behavior.peekHeight = 1000
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        with(bottomSheetView) {
            findViewById<Slider>(R.id.sliderTotalWords).apply {
                valueFrom = 10f
                valueTo = 100f
                stepSize = 5f
                value = 10f
            }

            findViewById<Slider>(R.id.sliderNewWords).apply {
                valueFrom = 0f
                valueTo = 50f
                stepSize = 1f
                value = 0f
            }

            findViewById<MaterialButton>(R.id.saveSettingsButton).setOnClickListener {
                val totalWords = findViewById<Slider>(R.id.sliderTotalWords).value.toInt()
                val newWords = findViewById<Slider>(R.id.sliderNewWords).value.toInt()

                restartQuizWithNewSettings(totalWords, newWords)
                bottomSheet.dismiss()

                Snackbar.make(binding.root,
                    "Quiz ayarları güncellendi: $totalWords kelime (${newWords} yeni)",
                    Snackbar.LENGTH_LONG)
                    .setBackgroundTint(colorPrimary)
                    .show()
            }
        }

        bottomSheet.show()
    }

    private fun restartQuizWithNewSettings(totalWords: Int, newWords: Int) {
        currentIndex = 0
        dogruSayisi = 0
        yanlisSayisi = 0
        binding.dogruSayisiTv.text = "0"
        binding.yanlisSayisiTv.text = "0"
        binding.progressIndicator.progress = 0
        getKullaniciyaOzelKelimeler(totalWords, newWords)
    }
    private fun getKullaniciyaOzelKelimeler(totalWords: Int = 10, newWords: Int = 0) {
        val uid = auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .get()
            .addOnSuccessListener { snapshot ->
                val now = Date().time
                val ozelKelimeler = snapshot.documents.filter { doc ->
                    val asama = (doc.getLong("asama") ?: 1).toInt()
                    if (asama >= 6) return@filter false

                    val sonTarih = doc.getTimestamp("sonDogruTarih")?.toDate() ?: Date(0)
                    val gerekenGun = zamanAraliklari.getOrNull(asama - 1) ?: 999
                    val gerekenMs = TimeUnit.DAYS.toMillis(gerekenGun.toLong())
                    val farkMs = now - sonTarih.time

                    farkMs >= gerekenMs
                }.map { it.id }

                getQuizWords(ozelKelimeler, totalWords, newWords)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun gorselVeSoruGoster() {
        if (currentIndex >= quizKelimeListesi.size) {
            showQuizCompletionDialog()
            return
        }

        val kelime = quizKelimeListesi[currentIndex]
        binding.apply {
            loadingOverlay.visibility = View.VISIBLE
            kelimeImage.visibility = View.INVISIBLE
            quizTurkce.visibility = View.INVISIBLE
            resultFeedback.visibility = View.GONE

            quizTurkce.text = kelime.kelimeTur
            kullaniciCevap.setText("")
            hakText.text = "${currentIndex + 1} / ${quizKelimeListesi.size}"
            progressIndicator.progress = ((currentIndex + 1) * 100 / quizKelimeListesi.size)

            kelime.gorselUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Picasso.get().load(url)
                    .resize(1024, 1024)
                    .transform(RoundedCornersTransformation(16, 0))
                    .into(kelimeImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            loadingOverlay.visibility = View.GONE
                            kelimeImage.visibility = View.VISIBLE
                            quizTurkce.visibility = View.VISIBLE
                            kullaniciCevap.requestFocus()
                        }

                        override fun onError(e: Exception?) {
                            loadingOverlay.visibility = View.GONE
                            kelimeImage.setImageResource(R.drawable.gallery_icon)
                            kelimeImage.visibility = View.VISIBLE
                            quizTurkce.visibility = View.VISIBLE
                        }
                    })
            } ?: run {
                loadingOverlay.visibility = View.GONE
                kelimeImage.setImageResource(R.drawable.gallery_icon)
                kelimeImage.visibility = View.VISIBLE
                quizTurkce.visibility = View.VISIBLE
            }
        }
    }

    private fun dogrulaCevap() {
        val cevap = binding.kullaniciCevap.text.toString().trim().lowercase()
        if (cevap.isEmpty()) {
            binding.inputLayout.error = "Lütfen bir cevap girin"
            return
        }
        binding.inputLayout.error = null

        val kelime = quizKelimeListesi[currentIndex]
        val dogruCevap = kelime.kelimeIng.trim().lowercase()
        val dogruMu = cevap == dogruCevap

        if (dogruMu) {
            dogruSayisi++
            binding.dogruSayisiTv.text = dogruSayisi.toString()
            showFeedback(true, dogruCevap)
        } else {
            yanlisSayisi++
            binding.yanlisSayisiTv.text = yanlisSayisi.toString()
            showFeedback(false, dogruCevap)
        }

        updateKelimeDurumu(kelime.kelimeId, dogruMu, kelime)
        currentIndex++

        if (currentIndex < quizKelimeListesi.size) {
            binding.kullaniciCevap.postDelayed({ gorselVeSoruGoster() }, 1000)
        } else {
            showQuizCompletionDialog()
        }
    }

    private fun showFeedback(isCorrect: Boolean, correctAnswer: String) {
        binding.apply {
            resultFeedback.apply {
                text = if (isCorrect) {
                    "Doğru! 🎉"
                } else {
                    "Yanlış! Doğru cevap: $correctAnswer"
                }
                setTextColor(Color.BLACK)
                background = ContextCompat.getDrawable(
                    this@QuizPageActivity,
                    if (isCorrect) R.drawable.bg_rounded_green else R.drawable.bg_rounded_red
                )
                visibility = View.VISIBLE
            }


            resultFeedback.postDelayed({ resultFeedback.visibility = View.GONE }, 1000)
        }
    }

    private fun showQuizCompletionDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.quiz_end_dialog, null)

        view.apply {
            findViewById<TextView>(R.id.dogruSayisi_tv).text = dogruSayisi.toString()
            findViewById<TextView>(R.id.yanlisSayisi_tv).text = yanlisSayisi.toString()
            findViewById<TextView>(R.id.cozulenSoruSayisi_tv).text = quizKelimeListesi.size.toString()
            findViewById<TextView>(R.id.aciklama_tv).text =
                "${auth.currentUser?.displayName ?: "Kullanıcı"}, quiz tamamlandı!"

            findViewById<MaterialButton>(R.id.quizDialogButton).setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                show()
            }
    }

    private fun DocumentSnapshot.toKelime(id: String): Kelime {
        return Kelime(
            kelimeId = id,
            kullaniciAdi = getString("kullaniciAdi") ?: "",
            kelimeIng = getString("ingilizceKelime") ?: "",
            kelimeTur = getString("turkceKarsiligi") ?: "",
            birinciCumle = getString("birinciCumle") ?: "",
            ikinciCumle = getString("ikinciCumle") ?: "",
            gorselUrl = getString("gorselUrl") ?: ""
        ).apply {
            docId = id
            dogruSayisi = getLong("dogruCevapSayisi")?.toInt() ?: 0
            sonDogruMs = getTimestamp("sonDogruCevapZamani")?.toDate()?.time ?: 0L
        }
    }

    private fun updateKelimeDurumu(kelimeId: String, dogruBildi: Boolean, kelime: Kelime) {
        val uid = auth.currentUser?.uid ?: return
        val kelimeRef = Firebase.firestore
            .collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .document(kelimeId)

        kelimeRef.get().addOnSuccessListener { doc ->
            val now = Date().time
            val oncekiAsama = (doc.getLong("asama") ?: 1).toInt()
            val sonDogruTarih = doc.getTimestamp("sonDogruTarih")?.toDate() ?: Date(0)
            val farkMs = now - sonDogruTarih.time
            val gerekenGun = zamanAraliklari.getOrNull(oncekiAsama - 1) ?: 999
            val gerekenMs = TimeUnit.DAYS.toMillis(gerekenGun.toLong())

            val updates = mutableMapOf<String, Any>(
                "sonDogruTarih" to Timestamp.now(),
                "kullaniciAdi" to (auth.currentUser?.displayName ?: "") as String,
                "Kelime" to kelime.kelimeIng
            )

            if (dogruBildi) {
                updates["dogruSayac"] = (doc.getLong("dogruSayac") ?: 0) + 1

                if (farkMs >= gerekenMs && oncekiAsama < 6) {
                    updates["asama"] = oncekiAsama + 1

                    if (oncekiAsama + 1 >= 6) {
                        Firebase.firestore.collection("ogrenilmisKelimeler")
                            .document(uid)
                            .collection("kelimeler")
                            .document(kelimeId)
                            .set(mapOf(
                                "kelimeId" to kelimeId,
                                "ingilizceKelime" to kelime.kelimeIng,
                                "turkceKarsiligi" to kelime.kelimeTur,
                                "gorselUrl" to kelime.gorselUrl,
                                "tarih" to Timestamp.now()
                            ))
                    }
                }
            } else {
                updates.apply {
                    put("asama", 1)
                    put("yanlisSayac", (doc.getLong("yanlisSayac") ?: 0) + 1)
                }
            }

            kelimeRef.set(updates)
        }
    }

    private fun getQuizWords(
        suresiDolanKelimeler: List<String>,
        totalWordsCount: Int = 10,
        newWordsCount: Int = 0
    ) {
        val totalWords = totalWordsCount.coerceIn(10, 100)
        val newWords = newWordsCount.coerceIn(0, 50).coerceAtMost(totalWords)

        val db = Firebase.firestore
        val uid = auth.currentUser?.uid ?: return

        db.collection("kelimeler").get().addOnSuccessListener { snapshot ->
            val tumKelimeler = snapshot.documents
            val quizKelimeListesi = mutableListOf<Kelime>()


            val secilenSuresiDolanlar = suresiDolanKelimeler
                .shuffled()
                .take(totalWords)
                .mapNotNull { id -> tumKelimeler.find { it.id == id }?.toKelime(id) }

            quizKelimeListesi.addAll(secilenSuresiDolanlar)


            db.collection("kullaniciKelimeleri")
                .document(uid)
                .collection("kelimeler")
                .get()
                .addOnSuccessListener { kullaniciSnapshot ->
                    val kullaniciKelimeIdSet = kullaniciSnapshot.documents.map { it.id }.toSet()

                    if (newWords > 0) {
                        val yeniKelimeler = tumKelimeler
                            .filter { it.id !in kullaniciKelimeIdSet }
                            .shuffled()
                            .take(newWords)
                            .mapNotNull { it.toKelime(it.id) }

                        quizKelimeListesi.addAll(yeniKelimeler)
                    }


                    val kalanKadarKelime = totalWords - quizKelimeListesi.size
                    if (kalanKadarKelime > 0) {
                        val ekKelimeListesi = tumKelimeler
                            .filter { it.id in kullaniciKelimeIdSet }
                            .filter { it.id !in suresiDolanKelimeler }
                            .shuffled()
                            .take(kalanKadarKelime)
                            .mapNotNull { it.toKelime(it.id) }

                        quizKelimeListesi.addAll(ekKelimeListesi)
                    }

                    this.quizKelimeListesi = quizKelimeListesi.shuffled().take(totalWords)
                    gorselVeSoruGoster()
                }
        }
    }

    override fun onBackPressed() {
        if (currentIndex < quizKelimeListesi.size) {
            AlertDialog.Builder(this)
                .setTitle("Quiz Bitmedi")
                .setMessage("Quiz henüz tamamlanmadı. Çıkmak istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ -> finish() }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun backImageClick(view: View) {
        onBackPressed()
    }
}
package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityQuizPageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

class QuizPageActivity : BaseCompact() {
    private lateinit var binding: ActivityQuizPageBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var quizKelimeListesi: List<Kelime>
    private var currentIndex = 0
    private var dogruSayisi = 0
    private var yanlisSayisi = 0
    private val zamanAraliklari = listOf(1, 7, 30, 90, 180, 365)
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityQuizPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.settingsButton.setOnClickListener {
            showSettingsBottomSheet()
        }
        binding.hakText.text = "1 / 10"


        getKullaniciyaOzelKelimeler()
        binding.dogrulaButton.setOnClickListener {
            dogrulaCevap()
        }
    }

    private fun showSettingsBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_quiz_settings, null)
        val bottomSheet = BottomSheetDialog(this).apply {
            setContentView(bottomSheetView)
            behavior.peekHeight = 1000
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        bottomSheet.show()

        val sliderTotal = bottomSheetView.findViewById<Slider>(R.id.sliderTotalWords).apply {
            valueFrom = 10f
            valueTo = 100f
            stepSize = 5f
            value = 10f
        }

        val sliderNew = bottomSheetView.findViewById<Slider>(R.id.sliderNewWords).apply {
            valueFrom = 0f
            valueTo = 50f
            stepSize = 1f
            value = 0f
        }

        bottomSheetView.findViewById<MaterialButton>(R.id.saveSettingsButton).setOnClickListener {
            val totalWords = sliderTotal.value.toInt().coerceAtMost(100) // Max 100
            val newWords = sliderNew.value.toInt().coerceAtMost(50) // Max 50


            val adjustedNewWords = newWords.coerceAtMost(totalWords)

            restartQuizWithNewSettings(totalWords, adjustedNewWords)
            bottomSheet.dismiss()

            Toast.makeText(
                this,
                "Quiz ayarları güncellendi:\nToplam $totalWords kelime (${adjustedNewWords} yeni)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun restartQuizWithNewSettings(totalWords: Int, newWords: Int) {

        val validatedTotalWords = totalWords.coerceIn(10, 100)
        val validatedNewWords = newWords.coerceIn(0, 50).coerceAtMost(validatedTotalWords)


        currentIndex = 0
        dogruSayisi = 0
        yanlisSayisi = 0
        binding.dogruSayisiTv.text = "0"
        binding.yanlisSayisiTv.text = "0"


        getKullaniciyaOzelKelimeler(validatedTotalWords, validatedNewWords)
    }

    private fun getKullaniciyaOzelKelimeler(totalWords: Int = 10, newWords: Int = 0) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        val ozelKelimeler = mutableListOf<String>()

        db.collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .get()
            .addOnSuccessListener { snapshot ->
                val now = Date().time

                for (doc in snapshot.documents) {
                    val asama = (doc.getLong("asama") ?: 1).toInt()
                    val sonTarih = doc.getTimestamp("sonDogruTarih")?.toDate() ?: continue
                    val kelimeId = doc.id

                    if (asama >= 6) continue

                    val gerekenGun = zamanAraliklari.getOrNull(asama - 1) ?: 999
                    val gerekenMs = TimeUnit.DAYS.toMillis(gerekenGun.toLong())
                    val farkMs = now - sonTarih.time

                    if (farkMs >= gerekenMs) {
                        ozelKelimeler.add(kelimeId)
                    }
                }
                getQuizWords(ozelKelimeler, totalWords, newWords)
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

        val gorselUrl = kelime.gorselUrl
        if (!gorselUrl.isNullOrEmpty()) {
            Picasso.get().load(kelime.gorselUrl)
                .resize(1024,1024)
                .transform(RoundedCornersTransformation(radius, margin))
                .into(binding.kelimeImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        // Fotoğraf başarıyla yüklendi
                        binding.progressBar2.visibility = View.GONE
                        binding.quizTurkce.visibility=View.VISIBLE
                        binding.hakText.visibility=View.VISIBLE
                        binding.cardViewFlase.visibility=View.VISIBLE
                        binding.cardViewTrue.visibility=View.VISIBLE
                        binding.quizTurkce.visibility=View.VISIBLE
                        binding.kelimeImage.visibility = View.VISIBLE
                    }

                    override fun onError(e: Exception?) {
                        // Yükleme hatası
                        binding.progressBar2.visibility = View.GONE
                        Toast.makeText(this@QuizPageActivity, "Yükleme başarısız", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {

            binding.kelimeImage.setImageResource(R.drawable.gallery_icon)
        }

        binding.quizTurkce.setText(kelime.kelimeTur)
        binding.kullaniciCevap.setText("")

        binding.hakText.text = "${currentIndex + 1} / ${quizKelimeListesi.size}"
        binding.hakText.setTextColor(Color.BLACK)
    }


    @SuppressLint("MissingInflatedId")
    private fun dogrulaCevap() {
        val cevap = binding.kullaniciCevap.text.toString().trim().lowercase()
        val kelime = quizKelimeListesi[currentIndex]
        val dogruCevap = kelime.kelimeIng.trim().lowercase()
        val dogruMu = cevap == dogruCevap

        if (dogruMu) {
            dogruSayisi++
            binding.dogruSayisiTv.text = dogruSayisi.toString()
            Toast.makeText(this, "Tebrikler! Doğru cevap.", Toast.LENGTH_SHORT).show()
        } else {
            yanlisSayisi++
            binding.yanlisSayisiTv.text = yanlisSayisi.toString()
            Toast.makeText(this, "Bilemediniz. Doğru cevap: $dogruCevap", Toast.LENGTH_LONG).show()
        }

        updateKelimeDurumu(kelime.kelimeId, dogruMu, kelime)

        currentIndex++
        if (currentIndex < quizKelimeListesi.size) {
            gorselVeSoruGoster()
        } else {
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.quiz_end_dialog, null)
            val dogruSayisiTV = view.findViewById<TextView>(R.id.dogruSayisi_tv)
            val yanlisSayisiTV = view.findViewById<TextView>(R.id.yanlisSayisi_tv)
            val soruSayisi = view.findViewById<TextView>(R.id.cozulenSoruSayisi_tv)
            val aciklama = view.findViewById<TextView>(R.id.aciklama_tv)
            val button = view.findViewById<Button>(R.id.quizDialogButton)
            val soruS=quizKelimeListesi.size
            val kullanici =auth.currentUser!!.displayName

            dogruSayisiTV.text="$dogruSayisi"
            yanlisSayisiTV.text="$yanlisSayisi"
            soruSayisi.text="$soruS"
            aciklama.text="$kullanici bir sonraki quiz'de görüşmek üzere👋"
            button.setOnClickListener{
                bagla(HomePageActivity::class.java,true)
            }

            val builder = AlertDialog.Builder(this)
            builder.setView(view)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            dialog.show()


            Toast.makeText(this, "Quiz tamamlandı!", Toast.LENGTH_LONG).show()

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

        kelime.docId = id
        kelime.dogruSayisi = getLong("dogruCevapSayisi")?.toInt() ?: 0
        kelime.sonDogruMs = getTimestamp("sonDogruCevapZamani")?.toDate()?.time ?: 0L

        return kelime
    }
    private fun updateKelimeDurumu(kelimeId: String, dogruBildi: Boolean, kelime: Kelime) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val kelimeRef = Firebase.firestore
            .collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .document(kelimeId)

        kelimeRef.get().addOnSuccessListener { doc ->
            val oncekiAsama = (doc.getLong("asama") ?: 1).toInt()
            val sonDogruTarih = doc.getTimestamp("sonDogruTarih")?.toDate() ?: Date(0)
            val farkMs = Date().time - sonDogruTarih.time
            val gerekenGun = zamanAraliklari.getOrNull(oncekiAsama - 1) ?: 999
            val gerekenMs = TimeUnit.DAYS.toMillis(gerekenGun.toLong())

            val name = auth.currentUser?.displayName ?: ""
            val kelimeIng = kelime.kelimeIng

            if (dogruBildi) {
                if (farkMs >= gerekenMs) {
                    val yeniAsama = if (oncekiAsama < 6) oncekiAsama + 1 else 6
                    kelimeRef.set(
                        mapOf(
                            "asama" to yeniAsama,
                            "dogruSayac" to (doc.getLong("dogruSayac") ?: 0) + 1,
                            "yanlisSayac" to (doc.getLong("yanlisSayac") ?: 0),
                            "sonDogruTarih" to Timestamp.now(),
                            "kullaniciAdi" to name,
                            "Kelime" to kelimeIng
                        )
                    )
                    if (yeniAsama >= 6 || oncekiAsama >= 6) {
                        val ogrenilenKelimeRef = Firebase.firestore
                            .collection("ogrenilmisKelimeler")
                            .document(uid)
                            .collection("kelimeler")
                            .document(kelimeId)

                        ogrenilenKelimeRef.set(
                            mapOf(
                                "kelimeId" to kelimeId,
                                "ingilizceKelime" to kelime.kelimeIng,
                                "turkceKarsiligi" to kelime.kelimeTur,
                                "gorselUrl" to kelime.gorselUrl,
                                "tarih" to Timestamp.now()
                            )
                        )
                    }
                } else {
                    kelimeRef.set(
                        mapOf(
                            "asama" to oncekiAsama,
                            "dogruSayac" to (doc.getLong("dogruSayac") ?: 0) +1,
                            "yanlisSayac" to (doc.getLong("yanlisSayac") ?: 0),
                            "sonDogruTarih" to Timestamp.now(),
                            "kullaniciAdi" to name,
                            "Kelime" to kelimeIng
                        )
                    )
                }
            } else {
                kelimeRef.set(
                    mapOf(
                        "asama" to 1,
                        "dogruSayac" to (doc.getLong("dogruSayac") ?: 0),
                        "yanlisSayac" to (doc.getLong("yanlisSayac") ?: 0) + 1,
                        "sonDogruTarih" to Timestamp.now(),
                        "kullaniciAdi" to name,
                        "Kelime" to kelimeIng
                    )
                )
            }
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("kelimeler").get().addOnSuccessListener { snapshot ->
            val tumKelimeler = snapshot.documents
            val quizKelimeListesi = mutableListOf<Kelime>()


            val secilenSuresiDolanlar = suresiDolanKelimeler
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
                .setPositiveButton("Evet") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun backImageClick(view: View){

        if (currentIndex < quizKelimeListesi.size) {
            AlertDialog.Builder(this)
                .setTitle("Quiz Bitmedi")
                .setMessage("Quiz henüz tamamlanmadı. Çıkmak istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                   goToHomeClick(view)
                }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            goToHomeClick(view)
        }
    }
}
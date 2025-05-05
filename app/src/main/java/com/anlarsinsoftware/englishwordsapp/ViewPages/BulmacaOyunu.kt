package com.anlarsinsoftware.englishwordsapp.ViewPages
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BulmacaOyunu : BaseCompact() {

    private val db = Firebase.firestore
    private var kelimeListesi: MutableList<String> = mutableListOf()
    private var gizliKelime = ""
    private var gecerliSatir = 0

    private lateinit var yeniOyunButonu: Button

    private lateinit var izgara: GridLayout
    private lateinit var hucreMatrisi: Array<Array<EditText>>
    private lateinit var gonderButonu: Button
    private lateinit var geriBildirim: TextView
    private lateinit var yuklemeProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulmaca_oyunu)

        izgara = findViewById(R.id.letterGrid)
        gonderButonu = findViewById(R.id.guessBtn)
        geriBildirim = findViewById(R.id.feedback)
        yuklemeProgress = findViewById(R.id.progress)
        yeniOyunButonu = findViewById(R.id.yeniOyunBtn)
        buildGrid()

        kelimeleriYukle()



        gonderButonu.setOnClickListener {
            tahminYap()
        }

        yeniOyunButonu.setOnClickListener {
            yeniOyunBaslat()

        }



    }

    private fun kelimeleriYukle() {
        yuklemeProgress.visibility = View.VISIBLE
        geriBildirim.text = "Kelimeler yükleniyor"
        gonderButonu.isEnabled = false

        db.collection("kelimeler")
            .get()
            .addOnSuccessListener { querySnapshot ->
                kelimeListesi.clear()

                try {

                    for (document in querySnapshot.documents) {
                        val kelimeIng = document.getString("ingilizceKelime")?.trim()?.uppercase()
                        if (kelimeIng != null && kelimeIng.length == 5) {
                            kelimeListesi.add(kelimeIng)
                        }
                    }

                    if (kelimeListesi.isNotEmpty()) {
                        gizliKelime = kelimeListesi.random()
                        Log.d("Wordle", "Gizli kelime: $gizliKelime")
                        geriBildirim.text = "Hazır! tahminini yap"
                        gonderButonu.isEnabled = true
                    } else {
                        geriBildirim.text = "Veritabanında uygun kelime bulunamadı!"
                    }
                } catch (e: Exception) {
                    Log.e("Wordle", "Kelime yükleme hatası", e)
                    geriBildirim.text = "Kelime yükleme hatası: ${e.localizedMessage}"
                } finally {
                    yuklemeProgress.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Wordle", "Veritabanı hatası", exception)
                yuklemeProgress.visibility = View.GONE
                geriBildirim.text = "Veritabanı hatası: ${exception.localizedMessage}"


                Toast.makeText(
                    this@BulmacaOyunu,
                    " Lütfen internet bağlantınızı kontrol edin.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun yeniOyunBaslat() {
        // Mevcut oyun durumunu sıfırla
        gecerliSatir = 0
        geriBildirim.text = ""
        gonderButonu.isEnabled = true

        // Tüm hücreleri temizle ve resetle
        for (row in 0 until 6) {
            for (col in 0 until 5) {
                hucreMatrisi[row][col].apply {
                    setText("")
                    setBackgroundResource(R.color.white)
                    isEnabled = row == 0 // Sadece ilk satır aktif
                    setTextColor(Color.BLACK) // Yazı rengi siyah
                }
            }
        }


        hucreMatrisi[0][0].requestFocus()


        kelimeleriYukle()
    }


    private fun buildGrid() {
        hucreMatrisi = Array(6) { row ->
            Array(5) { col ->
                EditText(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 60.dpToPx()
                        height = 60.dpToPx()
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                        setMargins(4, 4, 4, 4)
                    }
                    gravity = Gravity.CENTER
                    textSize = 24f
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    setBackgroundResource(R.color.white)
                    isSingleLine = true

                    addTextChangedListener(object : android.text.TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: android.text.Editable?) {
                            if (s?.length == 1 && col < 4) {
                                hucreMatrisi[row][col + 1].requestFocus()
                            }
                        }
                    })

                    setOnKeyListener { _, keyCode, event ->
                        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                            when (keyCode) {
                                android.view.KeyEvent.KEYCODE_DEL -> {
                                    if (text.isEmpty() && col > 0) {
                                        hucreMatrisi[row][col - 1].apply {
                                            requestFocus()
                                            setSelection(length())
                                        }
                                    }
                                }
                                android.view.KeyEvent.KEYCODE_ENTER -> {
                                    if (row == gecerliSatir) {
                                        tahminYap()
                                        return@setOnKeyListener true
                                    }
                                }
                            }
                        }
                        false
                    }
                }.also { izgara.addView(it) }
            }
        }
        hucreMatrisi[0][0].requestFocus()
    }

    private fun tahminYap() {
        if (gecerliSatir >= 6) return

        val guess = getGuessFromRow(gecerliSatir)

        if (guess.length != 5) {
            geriBildirim.text = "5 harfli bir kelime girin!"
            return
        }

        if (!kelimeListesi.contains(guess)) {
            geriBildirim.text = "Geçersiz kelime!"
            return
        }

        setFeedbackForRow(gecerliSatir, guess, gizliKelime)

        if (guess == gizliKelime) {
            geriBildirim.text = "Tebrikler! Kazandınız!"
            gonderButonu.isEnabled = false
            hucreMatrisi[gecerliSatir].forEach { it.isEnabled = false }
        } else {
            gecerliSatir++
            if (gecerliSatir == 6) {
                geriBildirim.text = "Kaybettiniz! Doğru kelime: $gizliKelime"
                gonderButonu.isEnabled = false
            } else {
                hucreMatrisi[gecerliSatir][0].requestFocus()
            }
        }
    }

    private fun getGuessFromRow(row: Int): String {
        return hucreMatrisi[row].joinToString("") { it.text.toString().uppercase() }
    }

    private fun setFeedbackForRow(row: Int, guess: String, secret: String) {
        val secretChars = secret.toCharArray()
        val guessChars = guess.toCharArray()
        val feedback = IntArray(5) { 0 }


        for (i in 0..4) {
            if (guessChars[i] == secretChars[i]) {
                feedback[i] = 2
                secretChars[i] = ' '
            }
        }


        for (i in 0..4) {
            if (feedback[i] != 0) continue

            val index = secretChars.indexOf(guessChars[i])
            if (index != -1) {
                feedback[i] = 1
                secretChars[index] = ' '
            }
        }


        for (i in 0..4) {
            val cell = hucreMatrisi[row][i]
            cell.isEnabled = false

            when (feedback[i]) {
                2 -> cell.setBackgroundColor(Color.GREEN)
                1 -> cell.setBackgroundColor(Color.YELLOW)
                else -> cell.setBackgroundColor(Color.DKGRAY)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}
package com.anlarsinsoftware.englishwordsapp.ViewPages
import com.google.firebase.database.*
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.*
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R

class BulmacaOyunu : BaseCompact() {

    lateinit var database: DatabaseReference

    var kelimeListesi: List<String> = listOf()
    var gizliKelime = ""
    var gecerliSatir = 0

    lateinit var izgara: GridLayout
    lateinit var hucreMatrisi: Array<Array<EditText>>
    lateinit var gonderButonu: Button
    lateinit var geriBildirim: TextView
    lateinit var ilerlemeGosterimi: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulmaca_oyunu)
        izgara = findViewById(R.id.letterGrid)
        gonderButonu = findViewById(R.id.guessBtn)
        geriBildirim = findViewById(R.id.feedback)
        ilerlemeGosterimi = findViewById(R.id.progress)

        val bulmacaGeri = findViewById<Button>(R.id.geriButon)
        bulmacaGeri.setOnClickListener {
         bagla(HomePageActivity::class.java,false)



        }



        buildGrid()
        gonderButonu.setOnClickListener {
            if (gecerliSatir >= 6) return@setOnClickListener

            val guess = getGuessFromRow(gecerliSatir)

            if (guess.length != 5 || !kelimeListesi.contains(guess.lowercase())) {
                geriBildirim.text = "Geçersiz kelime!"
                return@setOnClickListener
            }

            setFeedbackForRow(gecerliSatir, guess, gizliKelime)

            if (guess == gizliKelime.uppercase()) {
                geriBildirim.text = "Kazandın!"
                gonderButonu.isEnabled = false
            } else {
                gecerliSatir++
                if (gecerliSatir == 6) {
                    geriBildirim.text = "Kaybettin! Doğru kelime: $gizliKelime"
                }
            }
        }
    }

    private fun buildGrid() {
        hucreMatrisi = Array(6) { row ->
            Array(5) { col ->
                EditText(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 120
                        height = 120
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                        setMargins(4, 4, 4, 4)
                    }
                    gravity = Gravity.CENTER
                    textSize = 24f
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    setBackgroundColor(Color.LTGRAY)
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
                                        gonderButonu.performClick()

                                        // Tahmin başarılı değilse ve 6. satır değilse, sonraki satırın ilk kutusuna odaklan
                                        if (gecerliSatir < 6) {
                                            hucreMatrisi[gecerliSatir][0].requestFocus()
                                        }
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





    private fun getGuessFromRow(row: Int): String {
        return hucreMatrisi[row].joinToString("") { it.text.toString().uppercase() }
    }

    private fun setFeedbackForRow(row: Int, guess: String, secret: String) {
        val guessChars = guess.toCharArray()
        val secretChars = secret.uppercase().toCharArray()

        for (i in 0..4) {
            val cell = hucreMatrisi[row][i]
            val letter = guessChars[i]

            if (letter == secretChars[i]) {
                cell.setBackgroundColor(Color.GREEN)
            } else if (secretChars.contains(letter)) {
                cell.setBackgroundColor(Color.YELLOW)
            } else {
                cell.setBackgroundColor(Color.DKGRAY)
            }
        }
    }




}












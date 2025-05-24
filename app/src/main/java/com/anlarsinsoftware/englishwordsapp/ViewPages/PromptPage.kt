package com.anlarsinsoftware.englishwordsapp.ViewPages

import KelimeSecimAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources.Theme
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Placeholder
import androidx.fragment.app.DialogFragment
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Util.OPEN_ROUTER_API_KEY
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.Util.replicateAPI_KEY
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityPromptPageBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PromptPage : BaseCompact() {

    private lateinit var binding: ActivityPromptPageBinding
    private lateinit var adapter: KelimeSecimAdapter
    private lateinit var selectedWords: List<Kelime>
    private lateinit var secilenIngilizceKelimeler: List<String>
    private lateinit var promptKelimeleri: String
    private lateinit var promptImageCreate :String
    private lateinit var olusanHikaye:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromptPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchDogruKelimeler { kelimeler ->
            setupRecyclerView(kelimeler)
        }

        val btnCevir = findViewById<ImageView>(R.id.translate)
        val txtIngilizce = findViewById<TextView>(R.id.storyText)
        val txtTurkce = findViewById<TextView>(R.id.storyText)

        btnCevir.setOnClickListener {
            val metin = txtIngilizce.text.toString()
            ceviriYap(metin,
                onResult = { ceviri ->
                    txtTurkce.text = ceviri
                },
                onError = { hata ->
                    Toast.makeText(this, hata, Toast.LENGTH_SHORT).show()
                }
            )
            btnCevir.visibility=View.GONE
            binding.translate2.visibility=View.VISIBLE
        }



        binding.secButton.setOnClickListener {
            fetchDogruKelimeler { kelimeler ->
                val bottomSheet = KelimeSecBottomSheet(kelimeler) { secilenKelimeler ->
                    secilenIngilizceKelimeler = secilenKelimeler.map { it.kelimeIng }
                    promptKelimeleri = secilenIngilizceKelimeler.joinToString(", ")

                }
                bottomSheet.show(supportFragmentManager, "KelimeSecBottomSheet")
            }
            binding.button2.visibility=View.VISIBLE
        }

        binding.button2.setOnClickListener {
            if (::promptKelimeleri.isInitialized) {
                binding.progressBar.visibility = View.VISIBLE
                createStoryOpenRouter(promptKelimeleri,
                    onResult = { story ->
                        binding.storyScroll.visibility=View.VISIBLE
                        binding.storyText.visibility = View.VISIBLE
                        binding.translate.visibility=View.VISIBLE
                        binding.storyText.text = story
                         olusanHikaye=story
                        promptImageCreate = "Create a meaningful image inspired by these words $story"
                        generateImageFromStory(promptImageCreate)
                        binding.progressBar.visibility = View.GONE
                    },
                    onError = {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                )
            } else {
                Toast.makeText(this, "Lütfen önce 5 kelime seçin.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.translate2.setOnClickListener{
            findViewById<TextView>(R.id.storyText).text=olusanHikaye
            btnCevir.visibility=View.VISIBLE
            binding.translate2.visibility=View.INVISIBLE
        }

        textBasmaOlayi()


    }
    fun textBasmaOlayi(){
        val selectedWordsText = findViewById<TextView>(R.id.storyText)
        selectedWordsText.setOnClickListener {
            Toast.makeText(this, "Kopyalamak için basılı tutun", Toast.LENGTH_SHORT).show()
        }
        selectedWordsText.setOnLongClickListener {
            val textToCopy = selectedWordsText.text.toString()
            if (textToCopy.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Metin panoya kopyalandı", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    fun ceviriYap(metin: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.TURKISH)
            .build()

        val translator = Translation.getClient(options)

        // Model indir (ilk kullanımda gerekli)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(metin)
                    .addOnSuccessListener { ceviri ->
                        onResult(ceviri)
                    }
                    .addOnFailureListener { e ->
                        onError("Çeviri hatası: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                onError("Model indirilemedi: ${e.localizedMessage}")
            }
    }


    private fun fetchDogruKelimeler(onResult: (List<Kelime>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("kullaniciKelimeleri").document(uid)
            .collection("kelimeler")
            .whereGreaterThan("dogruSayac", 0)
            .get()
            .addOnSuccessListener { snapshot ->
                val kelimeIds = snapshot.documents.map { it.id }

                db.collection("kelimeler").get().addOnSuccessListener { allWordsSnapshot ->
                    val kelimeler = allWordsSnapshot.documents.filter {
                        kelimeIds.contains(it.id)
                    }.map { doc ->
                        Kelime(
                            kelimeId = doc.id,
                            kullaniciAdi = doc.getString("kullaniciAdi") ?: "",
                            kelimeIng = doc.getString("ingilizceKelime") ?: "",
                            kelimeTur = doc.getString("turkceKarsiligi") ?: "",
                            birinciCumle = doc.getString("birinciCumle") ?: "",
                            ikinciCumle = doc.getString("ikinciCumle") ?: "",
                            gorselUrl = doc.getString("gorselUrl")
                        )
                    }
                    onResult(kelimeler)
                }
            }
    }


    private fun setupRecyclerView(kelimeler: List<Kelime>) {
        adapter = KelimeSecimAdapter(kelimeler) { selected ->
            selectedWords = selected
            binding.secButton.isEnabled = selected.size == 5

        }
    }

    private fun createStoryOpenRouter(
        words: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = "Bearer ${OPEN_ROUTER_API_KEY}"
        val url = "https://openrouter.ai/api/v1/chat/completions"
        val prompt = "Write a very short paragraph for children using these English words : $words"



        val jsonBody = """
        {
          "model": "mistralai/mistral-7b-instruct",
          "messages": [
            {"role": "user", "content": "$prompt"}
          ]
        }
    """.trimIndent()

        val client = OkHttpClient()
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)
            .addHeader("HTTP-Referer", "https://englishwordsapp.example")
            .addHeader("X-Title", "EnglishWordsApp")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { onError(e.message ?: "İstek başarısız.") }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val jsonStr = response.body?.string()
                Log.e("OpenRouterResponse", jsonStr ?: "Boş cevap")

                if (jsonStr.isNullOrBlank()) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PromptPage,
                            "Hikaye alınamadı. API boş döndü.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return
                }

                try {
                    val json = JSONObject(jsonStr)
                    if (!json.has("choices")) {
                        onError("Yanıtta 'choices' alanı yok.")
                        return
                    }

                    val choices = json.getJSONArray("choices")
                    if (choices.length() == 0) {
                        onError("Yanıtta hiç seçenek yok.")
                        return
                    }

                    val firstChoice = choices.getJSONObject(0)
                    if (!firstChoice.has("message")) {
                        onError("Yanıtta 'message' alanı yok.")
                        return
                    }

                    val message = firstChoice.getJSONObject("message")
                    val content = message.getString("content")

                    runOnUiThread {
                        onResult(content.trim())
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        onError("Cevap çözümlenemedi: ${e.message}")
                    }
                }

            }
        })
    }


        private fun generateImageFromStory(prompt:String) {
            val imageUrl = "https://image.pollinations.ai/prompt/$prompt"
                runOnUiThread {
                    Picasso.get()
                        .load(imageUrl)
                        .transform(RoundedCornersTransformation(50, 0))
                        .placeholder(R.drawable.gallery_icon)
                        .into(binding.imageView11)
                    binding.imageView11.visibility = View.VISIBLE
                    binding.penai.visibility=View.VISIBLE
                }
        }
}

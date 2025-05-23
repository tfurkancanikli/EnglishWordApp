package com.anlarsinsoftware.englishwordsapp.ViewPages

import KelimeSecimAdapter
import android.content.res.Resources.Theme
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.Util.OPEN_ROUTER_API_KEY
import com.anlarsinsoftware.englishwordsapp.Util.replicateAPI_KEY
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityPromptPageBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PromptPage : AppCompatActivity() {

    private lateinit var binding: ActivityPromptPageBinding
    private lateinit var adapter: KelimeSecimAdapter
    private lateinit var selectedWords: List<Kelime>
    private lateinit var secilenIngilizceKelimeler: List<String>
    private lateinit var promptKelimeleri: String
    private lateinit var promptImageCreate :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromptPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchDogruKelimeler { kelimeler ->
            setupRecyclerView(kelimeler)
        }

        // Kelime seçme butonu
        binding.secButton.setOnClickListener {
            fetchDogruKelimeler { kelimeler ->
                val bottomSheet = KelimeSecBottomSheet(kelimeler) { secilenKelimeler ->
                    secilenIngilizceKelimeler = secilenKelimeler.map { it.kelimeIng }
                    promptKelimeleri = secilenIngilizceKelimeler.joinToString(", ")
                }
                bottomSheet.show(supportFragmentManager, "KelimeSecBottomSheet")

            }
        }

        // Hikaye oluştur ve görsel üret
        binding.button2.setOnClickListener {
            if (::promptKelimeleri.isInitialized) {
                binding.progressBar.visibility = View.VISIBLE
                createStoryOpenRouter(promptKelimeleri,
                    onResult = { story ->
                        binding.storyText.visibility = View.VISIBLE
                        binding.storyText.text = story
                        promptImageCreate = "Create a meaningful image inspired by these words $story"
                        generateImageFromStory("dog")
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
    }


    // Doğru sayacı 0'dan büyük kelimeleri çek
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

    // RecyclerView adapter kur
    private fun setupRecyclerView(kelimeler: List<Kelime>) {
        adapter = KelimeSecimAdapter(kelimeler) { selected ->
            selectedWords = selected
            binding.secButton.isEnabled = selected.size == 5
        }
    }

    // OpenRouter API ile hikaye üret
    // OpenRouter API ile hikaye üret
    private fun createStoryOpenRouter(
        words: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = "Bearer ${OPEN_ROUTER_API_KEY}"
        val url = "https://openrouter.ai/api/v1/chat/completions"
        val prompt = " Write a short children's story using these English words : $words"



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

    private fun generateImageFromStory(story: String) {
        val client = OkHttpClient()

        val promptText = if (story.length > 300) story.take(300) else story

        val jsonBody = JSONObject().apply {
            put("version", "ac732df83cea7fff18b8472768c88ad041fa750ff7682a21affe81863cbe77e4") // güncel versiyon ID
            put("input", JSONObject().apply {
                put("prompt", promptImageCreate)
                put("width", 512)
                put("height", 512)
                put("num_outputs", 1)
                put("guidance_scale", 7.5)
                put("num_inference_steps", 50)
                put("negative_prompt","soft, blurry, ugly")
                put("prompt_strength",0.8)
                put("high_noise_frac",0.8)
                put("scheduler","DPMSolverMultistep")
                put("refine","expert_ensemble_refiner")
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.replicate.com/v1/predictions")
            .addHeader("Authorization", "Token $replicateAPI_KEY") // Token buraya dikkat
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PromptPage, "İstek başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string() ?: return
                Log.e("REPLICATE_RESPONSE", responseStr)
                val predictionId = JSONObject(responseStr).optString("id", null)

                if (predictionId != null) {
                    checkPredictionStatus(predictionId)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PromptPage, "ID alınamadı", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun checkPredictionStatus(predictionId: String) {
        val client = OkHttpClient()
        val handler = Handler(mainLooper)

        lateinit var checkRunnable: Runnable
        checkRunnable = object : Runnable {
            override fun run() {
                val request = Request.Builder()
                    .url("https://api.replicate.com/v1/predictions/$predictionId")
                    .addHeader("Authorization", "Token ${replicateAPI_KEY}") // Token buraya
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@PromptPage, "Kontrol başarısız", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string()
                        val json = JSONObject(body)
                        val status = json.getString("status")

                        if (status == "succeeded") {
                            val imageUrl = json.getJSONArray("output").getString(0)
                            runOnUiThread {
                                Glide.with(this@PromptPage)
                                    .load(imageUrl)
                                    .into(binding.imageView11)
                                binding.imageView11.visibility = View.VISIBLE
                            }
                        } else if (status == "processing") {
                            handler.postDelayed(checkRunnable, 2000) // HATALI DEĞİL
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@PromptPage, "İşlem başarısız: $status", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }

        handler.postDelayed(checkRunnable, 2000)
    }




}

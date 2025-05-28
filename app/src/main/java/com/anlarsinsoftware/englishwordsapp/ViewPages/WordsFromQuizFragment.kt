package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anlarsinsoftware.englishwordsapp.Adapter.WordsFromQuizAdapter
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.FragmentWordsFromQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class WordsFromQuizFragment : Fragment() {

    private var _binding: FragmentWordsFromQuizBinding? = null
    private val binding get() = _binding!!

    private val cozulenKelimeler = mutableListOf<Kelime>()
    private lateinit var adapter: WordsFromQuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordsFromQuizBinding.inflate(inflater, container, false)

        adapter = WordsFromQuizAdapter(cozulenKelimeler)
        binding.recyclerViewQuizWords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewQuizWords.adapter = adapter

        getOgrenilmekteOlanKelimeler()

        return binding.root
    }

    private fun getOgrenilmekteOlanKelimeler() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        Firebase.firestore.collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .whereLessThan("asama", 6)
            .get()
            .addOnSuccessListener { snapshot ->
                cozulenKelimeler.clear()
                var counter = snapshot.size()
                if (counter == 0) {
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                for (doc in snapshot) {
                    val kelimeId = doc.id
                    val asama = doc.getLong("asama")?.toInt() ?: 0
                    val tarih = doc.getTimestamp("sonDogruTarih")?.toDate()

                    Firebase.firestore.collection("kelimeler")
                        .document(kelimeId)
                        .get()
                        .addOnSuccessListener { kelimeDoc ->
                            val kelime = kelimeDoc.toObject(Kelime::class.java)
                            if (kelime != null) {
                                if (asama >= 6) {
                                    val view=layoutInflater.inflate(R.layout.item_quiz_words,null)
                                    val text=view.findViewById<TextView>(R.id.kelimeAsama)
                                    text.text="Öğrenildi."
                                }else  {
                                    kelime.asama = asama
                                }

                                kelime.sonDogruTarih = tarih
                                cozulenKelimeler.add(kelime)
                            }
                            counter--
                            if (counter == 0) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

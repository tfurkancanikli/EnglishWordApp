package com.anlarsinsoftware.englishwordsapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.SozlukRecyclerRowBinding
import java.util.*

class SozlukAdapter(
    kelimeListesi: List<Kelime>,
    private val onItemClickListener: (Kelime) -> Unit
) : RecyclerView.Adapter<SozlukAdapter.KelimeHolder>() {

    private val fullList = mutableListOf<Kelime>().apply { addAll(kelimeListesi) }
    private val filteredList = mutableListOf<Kelime>().apply { addAll(kelimeListesi) }

    class KelimeHolder(val binding: SozlukRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelimeHolder {
        val binding = SozlukRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KelimeHolder(binding)
    }

    override fun onBindViewHolder(holder: KelimeHolder, position: Int) {
        val kelime = filteredList[position]
        holder.binding.tvKelimeIng.text = kelime.kelimeIng
        holder.binding.tvKelimeTur.text = kelime.kelimeTur

        val ing = kelime.kelimeIng.trim()
        val ilkHarf = if (ing.isNotEmpty()) ing[0] else null
        holder.binding.tvBasHarf.text = ilkHarf?.uppercase(Locale.getDefault()) ?: ""

        holder.itemView.setOnClickListener {
            onItemClickListener(kelime)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(fullList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            val filtered = fullList.filter {
                it.kelimeIng?.lowercase()?.contains(lowerCaseQuery) == true ||
                        it.kelimeTur?.lowercase()?.contains(lowerCaseQuery) == true
            }
            filteredList.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    fun updateFullList(newList: List<Kelime>) {
        fullList.clear()
        fullList.addAll(newList)
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }
}

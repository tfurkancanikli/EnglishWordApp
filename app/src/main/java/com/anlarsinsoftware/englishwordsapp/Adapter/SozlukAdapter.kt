package com.anlarsinsoftware.englishwordsapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.SozlukRecyclerRowBinding

class SozlukAdapter(
    private val kelimeListesi: ArrayList<Kelime>,
    private val onItemClickListener: (Kelime) -> Unit
) : RecyclerView.Adapter<SozlukAdapter.KelimeHolder>() {

    class KelimeHolder(val binding: SozlukRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelimeHolder {
        val binding = SozlukRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KelimeHolder(binding)
    }

    override fun onBindViewHolder(holder: KelimeHolder, position: Int) {
        val kelime = kelimeListesi[position]
        holder.binding.kelimeRow.text = kelime.kelimeIng


        holder.itemView.setOnClickListener {
            onItemClickListener(kelime)
        }
    }

    override fun getItemCount(): Int {
        return kelimeListesi.size
    }
}

package com.anlarsinsoftware.englishwordsapp.ViewPages

import KelimeSecimAdapter
import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.databinding.BottomsheetKelimeSecBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager

class KelimeSecBottomSheet(
    private val kelimeler: List<Kelime>,
    private val onKelimeSelected: (List<Kelime>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetKelimeSecBinding
    private lateinit var adapter: KelimeSecimAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetKelimeSecBinding.inflate(inflater, container, false)

        adapter = KelimeSecimAdapter(kelimeler) { selectedList ->
            binding.confirmButton.isEnabled = selectedList.size == 5
            binding.confirmButton.setOnClickListener {
                onKelimeSelected(selectedList)
                dismiss()
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@KelimeSecBottomSheet.adapter
        }

        return binding.root
    }
}

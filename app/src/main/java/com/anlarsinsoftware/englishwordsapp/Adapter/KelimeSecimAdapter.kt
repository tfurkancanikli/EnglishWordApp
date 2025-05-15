import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R

class KelimeSecimAdapter(
    private val kelimeler: List<Kelime>,
    private val onSelectionChanged: (List<Kelime>) -> Unit
) : RecyclerView.Adapter<KelimeSecimAdapter.KelimeViewHolder>() {

    private val selectedItems = mutableSetOf<Kelime>()

    inner class KelimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kelimeText: TextView = itemView.findViewById(R.id.kelimeText)
        val container: View = itemView.findViewById(R.id.kelimeContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kelime_secim, parent, false)
        return KelimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: KelimeViewHolder, position: Int) {
        val kelime = kelimeler[position]
        holder.kelimeText.text = kelime.kelimeIng

        holder.container.setBackgroundColor(
            if (selectedItems.contains(kelime)) Color.LTGRAY else Color.WHITE
        )

        holder.container.setOnClickListener {
            if (selectedItems.contains(kelime)) {
                selectedItems.remove(kelime)
            } else if (selectedItems.size < 5) {
                selectedItems.add(kelime)
            }
            notifyItemChanged(position)
            onSelectionChanged(selectedItems.toList())
        }
    }

    override fun getItemCount(): Int = kelimeler.size
}

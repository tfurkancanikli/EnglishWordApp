import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anlarsinsoftware.englishwordsapp.ViewPages.LearnedWordsFragment
import com.anlarsinsoftware.englishwordsapp.ViewPages.WordsFromQuizFragment

class ProfilePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WordsFromQuizFragment()
            1 -> LearnedWordsFragment()
            else -> WordsFromQuizFragment()
        }
    }
}

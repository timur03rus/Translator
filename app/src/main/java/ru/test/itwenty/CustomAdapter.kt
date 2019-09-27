package ru.test.itwenty

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import java.util.*

class CustomAdapter(context: Context, @LayoutRes resource: Int,
                    private val originalItems: ArrayList<Word>) : ArrayAdapter<Word>(context, resource, originalItems) {
    private var filteredItems: ArrayList<Word>? = null
    private val inflater: LayoutInflater

    init {
        filteredItems = ArrayList()
        filteredItems!!.addAll(this.originalItems)
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return filteredItems!!.size
    }

    override fun getItem(position: Int): Word? {
        return filteredItems!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                val filteredList = ArrayList<Word>()

                if (constraint == "" || constraint.toString().trim { it <= ' ' }.isEmpty()) {
                    results.values = originalItems
                } else {
                    val textToFilter = constraint.toString().toLowerCase()
                    for (word in originalItems) {
                        if (word.word?.length!! >= textToFilter.length && word.word!!.toLowerCase().contains(textToFilter)) {
                            filteredList.add(word)
                        }
                    }
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    filteredItems = results.values as ArrayList<Word>
                    notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = filteredItems!![position]
        var v: View?

        if (convertView == null) {
            v = inflater.inflate(R.layout.list_item, parent, false)
        } else {
            v = convertView
        }

        val button = v!!.findViewById<ImageButton>(R.id.addToFavourites2)
        val text = v.findViewById<TextView>(R.id.text)
        val translation = v.findViewById<TextView>(R.id.translation)
        val language = v.findViewById<TextView>(R.id.languages)

        text.text = item.word
        translation.text = item.translation
        language.text = item.sourceLanguage + "-" + item.targetLanguage

        val dbhelper = DataBaseHelper(v.context, "Favourites.db")
        if (dbhelper.isInDataBase(item)) {
            button.setImageResource(R.drawable.selected_favourites_icon)
        }
        dbhelper.close()

        button.setOnClickListener { v1 ->
            val text1 = item.word
            val translation1 = item.translation
            val languages = intArrayOf(item.sourcePosition, item.targetPosition)
            val button1 = v1.findViewById<ImageButton>(R.id.addToFavourites2)
            val word = Word(text1, translation1, languages[0], languages[1])

            val dbhelper1 = DataBaseHelper(context, "Favourites.db")
            if (dbhelper1.isInDataBase(word)) {
                dbhelper1.setDeleted(word)
                button1.setImageResource(R.drawable.default_favourites_icon)
            } else {
                dbhelper1.insertWord(word)
                button1.setImageResource(R.drawable.selected_favourites_icon)
            }
            dbhelper1.close()

        }

        return v
    }
}

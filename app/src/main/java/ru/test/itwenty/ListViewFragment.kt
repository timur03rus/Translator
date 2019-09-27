package ru.test.itwenty

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ListViewFragment : Fragment() {

    private var actionBar: ActionBar? = null
    private var adapter: CustomAdapter? = null
    private var nameOfDB: String? = null
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nameOfDB = arguments!!.getString("nameOfDB")
        actionBar = (activity as AppCompatActivity).supportActionBar
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.listview_fragment, container, false)
        changeToListView()
        setHintToSearch()
        return rootView
    }

    fun setHintToSearch() {
        val search = rootView!!.findViewById<EditText>(R.id.search)
        if (nameOfDB == "History.db") {
            search.setHint(R.string.history_search_hint)
        } else {
            search.setHint(R.string.favourite_search_hint)
        }
    }

    fun setCustomActionBar() {
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar!!.setCustomView(R.layout.custom_action_bar)

        if (nameOfDB == "History.db") {
            actionBar!!.setTitle(R.string.text_history)
        } else {
            actionBar!!.setTitle(R.string.text_favourites)
        }
    }

    fun changeToListView() {
        val dataBaseHelper = DataBaseHelper(context!!, nameOfDB!!)
        val arrayList = dataBaseHelper.allWords
        adapter = CustomAdapter(context!!, R.layout.list_item, arrayList)

        if (arrayList.isEmpty()) {
            val noWordsText = rootView!!.findViewById<TextView>(R.id.no_words_in_listview)
            val search = rootView!!.findViewById<EditText>(R.id.search)
            noWordsText.visibility = View.VISIBLE
            search.visibility = View.INVISIBLE
            actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            if (nameOfDB == "History.db") {
                noWordsText.setText(R.string.no_words_in_history)
                actionBar!!.setTitle(R.string.text_history)
            } else {
                noWordsText.setText(R.string.no_words_in_favourites)
                actionBar!!.setTitle(R.string.text_favourites)
            }
        } else {
            setCustomActionBar()

            val listView = rootView!!.findViewById<ListView>(R.id.listView)
            listView.adapter = adapter

            listView.setOnItemClickListener { parent, view, position, id ->
                val text = view.findViewById<TextView>(R.id.text)
                val translation = view.findViewById<TextView>(R.id.translation)
                val textView = view.findViewById<TextView>(R.id.languages)
                val langs = textView.text.toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                var dbhelper = DataBaseHelper(view.context, nameOfDB!!)
                val languages = dbhelper.getLanguages(text.text.toString(), langs[0],
                        langs[1])
                dbhelper.close()

                dbhelper = DataBaseHelper(view.context, "Favourites.db")
                val sharedPref = activity!!.getPreferences(Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putInt("selection1", languages[0])
                editor.putInt("selection2", languages[1])
                editor.putString("textToTranslate", text.text.toString())
                editor.putString("translatedText", translation.text.toString())
                if (dbhelper.isInDataBase(Word(text.text.toString(),
                                translation.text.toString(), languages[0], languages[1]))) {
                    editor.putBoolean("isFavourite", true)
                } else {
                    editor.putBoolean("isFavourite", false)
                }
                editor.apply()
                dbhelper.close()

                (activity as MainActivity).changeToMainView()
            }

            val search = rootView!!.findViewById<EditText>(R.id.search)
            search.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    adapter!!.filter.filter(s.toString())
                }
            })
        }
    }

    override fun onDestroy() {
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
        actionBar!!.setTitle(R.string.app_name)
        super.onDestroy()
    }
    fun newInstance(nameOfDB: String): ListViewFragment {
        val listViewFragment = ListViewFragment()
        val args = Bundle()
        args.putString("nameOfDB", nameOfDB)
        listViewFragment.arguments = args
        return listViewFragment
    }
}

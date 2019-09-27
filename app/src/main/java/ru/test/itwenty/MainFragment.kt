package ru.test.itwenty

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.widget.textChanges
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class MainFragment : Fragment() {

    private var rootView: View? = null
    private var spinner1: Spinner? = null
    private var spinner2: Spinner? = null
    private var textToTranslate: EditText? = null
    private var addToFavourites: ImageButton? = null
    private var changeLanguages: ImageButton? = null
    private var translatedText: TextView? = null
    private var isFavourite: Boolean = false // if current word is favourite.
    private var noTranslate: Boolean = false // do not translate at 1-st text changing. Need when initialize
    // with some text.
    /**
     * Initialize widget elements and create view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return created view of fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.main_fragment, container, false)
        spinner1 = rootView!!.findViewById(R.id.languages1)
        spinner2 = rootView!!.findViewById(R.id.languages2)
        textToTranslate = rootView!!.findViewById(R.id.textToTranslate)
        textToTranslate!!.movementMethod = ScrollingMovementMethod()
        textToTranslate!!.isVerticalScrollBarEnabled = true
        changeLanguages = rootView!!.findViewById(R.id.changeLanguages)
        addToFavourites = rootView!!.findViewById(R.id.addToFavourites1)
        translatedText = rootView!!.findViewById(R.id.translatedText)
        translatedText!!.movementMethod = ScrollingMovementMethod()
        translatedText!!.isVerticalScrollBarEnabled = true
        setArgs()
        return rootView
    }

    /**
     * Add listeners and set data.
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setSpinners()
        textChangedListener()
        addButtonListener()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        val sharedPref = activity!!.getSharedPreferences("default", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("selection1", spinner1!!.selectedItemPosition)
        editor.putInt("selection2", spinner2!!.selectedItemPosition)
        editor.putString("textToTranslate", textToTranslate!!.text.toString())
        editor.putString("translatedText", translatedText!!.text.toString())
        editor.putBoolean("isFavourite", isFavourite)
        editor.apply()
        super.onDestroyView()
    }

    fun addToHistory() {
        val text = textToTranslate!!.text.toString().trim { it <= ' ' }
        if (text != "") {
            val dataBaseHelper = DataBaseHelper(rootView!!.context, "History.db")
            dataBaseHelper.insertWord(Word(textToTranslate!!.text.toString().trim { it <= ' ' },
                    translatedText!!.text.toString(), spinner1!!.selectedItemPosition,
                    spinner2!!.selectedItemPosition))
            dataBaseHelper.close()
        }
    }

    fun checkIfInFavourites() {
        val text = textToTranslate!!.text.toString()
        if (text != "") {
            addToFavourites!!.visibility = View.VISIBLE

            val dataBaseHelper = DataBaseHelper(rootView!!.context, "Favourites.db")
            if (dataBaseHelper.isInDataBase(Word(text, translatedText!!.text.toString(),
                            spinner1!!.selectedItemPosition, spinner2!!.selectedItemPosition))) {
                addToFavourites!!.setImageResource(R.drawable.selected_favourites_icon)
                isFavourite = true
            } else {
                addToFavourites!!.setImageResource(R.drawable.default_favourites_icon)
                isFavourite = false
            }
            dataBaseHelper.close()
        } else {
            isFavourite = false
            addToFavourites!!.visibility = View.INVISIBLE
            translatedText!!.text = ""
        }
    }

    fun setArgs() {
        val sharedPref = activity!!.getSharedPreferences("default", Context.MODE_PRIVATE)
        val text = sharedPref.getString("textToTranslate", "")
        val translation = sharedPref.getString("translatedText", "")
        val selection1 = sharedPref.getInt("selection1", 0)
        val selection2 = sharedPref.getInt("selection2", 1)
        isFavourite = sharedPref.getBoolean("isFavourite", false)
        if (text != "") {
            noTranslate = true
            textToTranslate!!.setText(text)
            spinner1!!.setSelection(selection1)
            spinner2!!.setSelection(selection2)
            translatedText!!.text = translation
            addToFavourites!!.visibility = View.VISIBLE
            if (isFavourite) {
                addToFavourites!!.setImageResource(R.drawable.selected_favourites_icon)
            } else {
                addToFavourites!!.setImageResource(R.drawable.default_favourites_icon)
            }
        }
    }

    fun addButtonListener() {

        addToFavourites!!.setOnClickListener { v ->
            val dataBaseHelper = DataBaseHelper(v.context,
                    "Favourites.db")
            val text = textToTranslate!!.text.toString().trim { it <= ' ' }
            val translation = translatedText!!.text.toString()
            val source = spinner1!!.selectedItemPosition
            val target = spinner2!!.selectedItemPosition
            val item = Word(text, translation, source, target)
            if (dataBaseHelper.isInDataBase(item)) {
                dataBaseHelper.deleteWord(item)
                addToFavourites!!.setImageResource(R.drawable.default_favourites_icon)
                isFavourite = false
            } else {
                isFavourite = true
                dataBaseHelper.insertWord(item)
                addToFavourites!!.setImageResource(R.drawable.selected_favourites_icon)
            }
            dataBaseHelper.close()
        }

        changeLanguages!!.setOnClickListener { v ->
            val sourceLng = spinner1!!.selectedItemPosition
            val targetLng = spinner2!!.selectedItemPosition

            spinner1!!.setSelection(targetLng)
            spinner2!!.setSelection(sourceLng)

            translate(textToTranslate!!.text.toString().trim { it <= ' ' })
        }

    }

    fun setSpinners() {
        // Spinner Drop down elements
        val categories = ArrayList<String>()

        if (Locale.getDefault().language == "en") {
            Collections.addAll(categories, *Languages.langsEN)
        } else {
            Collections.addAll(categories, *Languages.langsRU)
        }

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(context!!,
                android.R.layout.simple_spinner_item, categories)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attaching data adapter to spinner
        spinner1!!.adapter = dataAdapter
        spinner2!!.adapter = dataAdapter
        spinner2!!.setSelection(1)
    }

    @SuppressLint("CheckResult")
    fun textChangedListener() {

        // Translate the text after 500 milliseconds when user ends to typing
        textToTranslate?.textChanges()?.filter { charSequence -> charSequence.isNotEmpty() }?.debounce(500, TimeUnit.MILLISECONDS)?.subscribe { charSequence -> translate(charSequence.toString().trim { it <= ' ' }) }

        textToTranslate?.textChanges()?.filter { charSequence -> charSequence.isEmpty() }?.subscribe { activity!!.runOnUiThread { checkIfInFavourites() } }
    }

    private fun translate(text: String) {
        if (noTranslate) {
            noTranslate = false
            return
        }

        val APIKey = "trnsl.1.1.20170314T200256Z.c558a20c3d6824ff.7" + "860377e797dffcf9ce4170e3c21266cbc696f08"
        val language1 = spinner1!!.selectedItem.toString()
        val language2 = spinner2!!.selectedItem.toString()

        val query = Retrofit.Builder().baseUrl("https://translate.yandex.net/").addConverterFactory(GsonConverterFactory.create()).build()
        val apiHelper = query.create(APIHelper::class.java)
        val call = apiHelper.getTranslation(APIKey, text,
                langCode(language1) + "-" + langCode(language2))

        call.enqueue(object : Callback<TranslatedText> {
            override fun onResponse(call: Call<TranslatedText>, response: Response<TranslatedText>) {
                if (response.isSuccessful) {
                    activity!!.runOnUiThread {
                        translatedText!!.text = response.body().text!![0]
                        checkIfInFavourites()
                        addToHistory()
                    }
                }
            }

            override fun onFailure(call: Call<TranslatedText>, t: Throwable) {}
        })
    }

    fun langCode(selectedLang: String): String? {
        var code: String? = null

        if (Locale.getDefault().language == "en") {
            for (i in Languages.langsEN.indices) {
                if (selectedLang == Languages.langsEN[i]) {
                    code = Languages.getLangCodeEN(i)
                }
            }
        } else {
            for (i in Languages.langsRU.indices) {
                if (selectedLang == Languages.langsRU[i]) {
                    code = Languages.getLangCodeRU(i)
                }
            }
        }
        return code
    }
}

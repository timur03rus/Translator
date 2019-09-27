package ru.test.itwenty

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        if (savedInstanceState == null) {
            changeToMainView()
        }
        bottomNavigationListener()
    }

    private fun bottomNavigationListener() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_translate -> changeToMainView()
                R.id.action_favourites -> changeToListView("Favourites.db")
                R.id.action_history -> changeToListView("History.db")
            }
            true
        }
    }

    fun changeToListView(nameOfDB: String) {
        // Change current fragment in activity
        val ft = supportFragmentManager.beginTransaction()
        val listViewFragment = ListViewFragment().newInstance(nameOfDB)
        ft.replace(R.id.fragment, listViewFragment)
        ft.commit()
    }

    fun changeToMainView() {
        // Change current fragment in activity
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, MainFragment())
        ft.commit()
    }

    fun TrashOnClick(v: View) {
        val title = supportActionBar!!.title!!.toString()
        if (title == getString(R.string.text_history)) {
            showConfirmationDialog(R.string.delete_confirmation_of_history_words, "History.db",
                    v.context)
        } else {
            showConfirmationDialog(R.string.delete_confirmation_of_favourite_words, "Favourites.db",
                    v.context)
        }
    }

    fun showConfirmationDialog(answerID: Int, nameOfDB: String, context: Context) {
        var title = R.string.text_history
        if (nameOfDB == "Favourites.db") {
            title = R.string.text_favourites
        }
        // if user agrees to delete words, then delete
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(answerID)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    deleteWordsFromDB(context, nameOfDB)
                    changeToListView(nameOfDB)
                }
                .setNegativeButton(android.R.string.no, null).show()
    }

    private fun deleteWordsFromDB(context: Context, nameOfDB: String) {
        val dataBaseHelper = DataBaseHelper(context, nameOfDB)
        dataBaseHelper.deleteAllWords()
        dataBaseHelper.close()
    }
}

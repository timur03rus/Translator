package ru.test.itwenty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Word> {

    private ArrayList<Word> originalItems;
    private ArrayList<Word> filteredItems;
    private LayoutInflater inflater;

    public CustomAdapter(@NonNull Context context, @LayoutRes int resource,
                         @NonNull ArrayList<Word> objects) {
        super(context, resource, objects);
        this.originalItems = objects;
        filteredItems = new ArrayList<>();
        filteredItems.addAll(this.originalItems);
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredItems.size();
    }

    public Word getItem(int position) {
        return filteredItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                final ArrayList<Word> filteredList = new ArrayList<Word>();

                if (constraint.equals("") || constraint.toString().trim().length() == 0) {
                    results.values = originalItems;
                } else {
                    String textToFilter = constraint.toString().toLowerCase();
                    for (Word word : originalItems) {
                        if (word.getWord().length() >= textToFilter.length() &&
                                word.getWord().toLowerCase().contains(textToFilter)) {
                            filteredList.add(word);
                        }
                    }
                    results.values = filteredList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    filteredItems = (ArrayList<Word>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
        return filter;
    }

    @NotNull
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        final Word item = filteredItems.get(position);
        View v = null;

        if (convertView == null) {
            v = inflater.inflate(R.layout.list_item, parent, false);
        } else {
            v = convertView;
        }

        ImageButton button = v.findViewById(R.id.addToFavourites2);
        TextView text = v.findViewById(R.id.text);
        TextView translation = v.findViewById(R.id.translation);
        final TextView language = v.findViewById(R.id.languages);

        text.setText(item.getWord());
        translation.setText(item.getTranslation());
        language.setText(item.getSourceLanguage() + "-" + item.getTargetLanguage());

        DataBaseHelper dbhelper = new DataBaseHelper(v.getContext(), "Favourites.db");
        if (dbhelper.isInDataBase(item)) {
            button.setImageResource(R.drawable.selected_favourites_icon);
        }
        dbhelper.close();

        button.setOnClickListener(
                v1 -> {
                    String text1 = item.getWord();
                    String translation1 = item.getTranslation();
                    int[] languages = {item.getSourcePosition(), item.getTargetPosition()};
                    ImageButton button1 = v1.findViewById(R.id.addToFavourites2);
                    Word word = new Word(text1, translation1, languages[0], languages[1]);

                    DataBaseHelper dbhelper1 = new DataBaseHelper(getContext(), "Favourites.db");
                    if (dbhelper1.isInDataBase(word)) {
                        dbhelper1.setDeleted(word);
                        button1.setImageResource(R.drawable.default_favourites_icon);
                    } else {
                        dbhelper1.insertWord(word);
                        button1.setImageResource(R.drawable.selected_favourites_icon);
                    }
                    dbhelper1.close();

                });

        return v;
    }
}

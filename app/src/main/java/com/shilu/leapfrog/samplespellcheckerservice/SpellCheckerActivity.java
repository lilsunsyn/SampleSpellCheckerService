package com.shilu.leapfrog.samplespellcheckerservice;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class SpellCheckerActivity extends Activity implements SpellCheckerSession.SpellCheckerSessionListener {

//    EditText edt_input;

    SpellCheckerSession mScs;
    TextServicesManager tsm;

    private static final List<String> listSuggestion = new ArrayList<>();
    ArrayAdapter<String> adapter;

//    ListView listView;

    String[] words;

    private static final String TAG = SpellCheckerActivity.class.getSimpleName();
    private static final int NOT_A_LENGTH = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setElements();
    }


    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        listSuggestion.clear();
//
//        for (int i = 0; i < results.length; ++i) {
//            // Returned listSuggestion are contained in SuggestionsInfo
//            final int len = results[i].getSuggestionsCount();
//
//            for (int j = 0; j < len; ++j) {
//                listSuggestion.add(results[i].getSuggestionAt(j));
//            }
//
//        }
//
//        adapter.notifyDataSetChanged();

        Log.d(TAG, "onGetSuggestions");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.length; ++i) {
            dumpSuggestionsInfoInternal(sb, results[i], 0, NOT_A_LENGTH);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("suggestion "+sb.toString());
                listSuggestion.add(sb.toString());
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        listSuggestion.clear();
        if(!isSentenceSpellCheckSupported()) {
            Log.e(TAG, "Sentence spell check is not supported on this platform, "
                    + "but accidentially called.");
            return;
        }
        Log.d(TAG, "onGetSentenceSuggestions");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.length; ++i) {
            final SentenceSuggestionsInfo ssi = results[i];
            for (int j = 0; j < ssi.getSuggestionsCount(); ++j) {
                dumpSuggestionsInfoInternal(
                        sb, ssi.getSuggestionsInfoAt(j), ssi.getOffsetAt(j), ssi.getLengthAt(j));
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("suggestion "+sb.toString());
                listSuggestion.add(sb.toString());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void dumpSuggestionsInfoInternal(
            final StringBuilder sb, final SuggestionsInfo si, final int length, final int offset) {
        // Returned suggestions are contained in SuggestionsInfo
        final int len = si.getSuggestionsCount();
        sb.append('\n');
        for (int j = 0; j < len; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(si.getSuggestionAt(j));
        }
//        sb.append(" (" + len + ")");
//        if (length != NOT_A_LENGTH) {
//            sb.append(" length = " + length + ", offset = " + offset);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tsm = (TextServicesManager) getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        mScs = tsm.newSpellCheckerSession(null, null, this, true);
        mScs.getSpellChecker().getServiceInfo().flags = SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS;
    }
    private boolean isSentenceSpellCheckSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    /**
     * Initialize editText, listView, adapters.
     * Add textChangerListeners.
     *
     * @author Manas Shrestha
     */
    public void setElements() {
//        edt_input = (EditText) findViewById(R.id.edt_input);
//
//        listView = (ListView) findViewById(R.id.simpleListView);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listSuggestion);

        /*
        listView.setAdapter(adapter);

        edt_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mScs != null) {
                    // Instantiate TextInfo for each query
                    // TextInfo can be passed a sequence number and a cookie number to identify the result
                    if (isSentenceSpellCheckSupported()) {
                        // Note that getSentenceSuggestions works on JB or later.
                        Log.d(TAG, "Sentence spellchecking supported.");
                        if (s.length() != 0) {
                            String sentence = s.toString();
                            words = sentence.split("\\s+");
                            Log.e("words", words[words.length - 1]);
                            mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(words[words.length - 1])}, 3);

                        }

                    } else {
                        // Note that getSuggestions() is a deprecated API.
                        // It is recommended for an application running on Jelly Bean or later
                        // to call getSentenceSuggestions() only.
                        mScs.getSuggestions(new TextInfo("tgis"), 3);
                        mScs.getSuggestions(new TextInfo("hllo"), 3);
                        mScs.getSuggestions(new TextInfo("helloworld"), 3);
                    }
                } else {
                    Log.e(TAG, "Couldn't obtain the spell checker service.");
                }
//                if (mScs != null) {
//                    if (s.length() != 0) {
//                        String sentence = s.toString();
//                        words = sentence.split("\\s+");
//                        android.util.Log.e("words", words[words.length - 1]);
//
////                        mScs.getSuggestions(new TextInfo(words[words.length - 1]), 10);
//                        mScs.getSentenceSuggestions(new TextInfo[]{new TextInfo(words[words.length - 1])}, 10);
//                    }
//                } else {
//                    gotoSpellCheckerSetting();
//                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("from list", listSuggestion.get(position));

                words[words.length - 1] = listSuggestion.get(position);

                String newSentence = "";

                for (int i = 0; i < words.length; i++) {

                    newSentence = newSentence + words[i];

                    if (i != words.length - 1) {
                        newSentence = newSentence + " ";
                    }
                }

                edt_input.setText(newSentence);
                edt_input.setSelection(edt_input.getText().length());
            }
        });*/

    }

    /**
     * Show toast.
     * Goto spellchecker settings.
     *
     * @author Manas Shrestha
     */
    public void gotoSpellCheckerSetting() {

        // Show the message to user
        Toast.makeText(SpellCheckerActivity.this, "Please turn on the spell checker from setting", Toast.LENGTH_LONG).show();
        // open the settings page for user to turn spell checker ON
        ComponentName componentToLaunch = new ComponentName("com.android.settings",
                "com.android.settings.Settings$SpellCheckersSettingsActivity");
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentToLaunch);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Error
        }
    }
}

package com.colstech.readster.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.colstech.readster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefinitionDialogFragment extends DialogFragment {

    private static final String ARG_WORD = "word";

    public static DefinitionDialogFragment newInstance(String word) {
        DefinitionDialogFragment fragment = new DefinitionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_definition_dialog, null);

        TextView wordText = view.findViewById(R.id.word_text);
        TextView definitionText = view.findViewById(R.id.definition_text);
        ProgressBar loadingProgress = view.findViewById(R.id.loading_progress);

        String word = getArguments().getString(ARG_WORD);
        wordText.setText(word);

        fetchDefinition(word, definitionText, loadingProgress);

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void fetchDefinition(String word, TextView definitionView, ProgressBar progressBar) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    result = stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String finalResult = result;
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (finalResult != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(finalResult);
                        JSONObject entry = jsonArray.getJSONObject(0);
                        JSONArray meanings = entry.getJSONArray("meanings");
                        StringBuilder definitions = new StringBuilder();

                        for (int i = 0; i < meanings.length(); i++) {
                            JSONObject meaning = meanings.getJSONObject(i);
                            String partOfSpeech = meaning.getString("partOfSpeech");
                            definitions.append(partOfSpeech).append(":\n");

                            JSONArray defs = meaning.getJSONArray("definitions");
                            for (int j = 0; j < defs.length(); j++) {
                                JSONObject def = defs.getJSONObject(j);
                                definitions.append("- ").append(def.getString("definition")).append("\n");
                            }
                            definitions.append("\n");
                        }
                        definitionView.setText(definitions.toString());
                    } catch (JSONException e) {
                        definitionView.setText("Definition not found.");
                    }
                } else {
                    definitionView.setText("Error fetching definition.");
                }
            });
        });
    }
}

package com.colstech.readster.ui.main;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.colstech.readster.Assets;
import com.colstech.readster.Config;
import com.colstech.readster.MainActivity;
import com.colstech.readster.databinding.FragmentMainBinding;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "image_uri";

    private FragmentMainBinding binding;

    private MainViewModel viewModel;

    private Uri imageUri;

    public static MainFragment newInstance(Uri imageUri) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
        }

        Assets.extractAssets(requireContext());

        if (!viewModel.isInitialized()) {
            String dataPath = Assets.getTessDataPath(requireContext());
            viewModel.initTesseract(dataPath, Config.TESS_LANG, Config.TESS_ENGINE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            binding.image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            viewModel.recognizeImage(bitmap);
        }

        binding.captureAgain.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).takePicture();
        });
        binding.text.setMovementMethod(LinkMovementMethod.getInstance());

        viewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            binding.captureAgain.setEnabled(!processing);
        });
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            binding.status.setText(progress);
        });
        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            setClickableText(result);
        });
    }

    private void setClickableText(String text) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String word = text.substring(start, end);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    DefinitionDialogFragment.newInstance(word).show(getParentFragmentManager(), "definition");
                }
            };
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.text.setText(spannableString);
    }
}

package com.colstech.readster.ui.main;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.colstech.readster.Assets;
import com.colstech.readster.Config;
import com.colstech.readster.databinding.FragmentMainBinding;

import java.io.IOException;

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

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            binding.image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.start.setOnClickListener(v -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                viewModel.recognizeImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        binding.stop.setOnClickListener(v -> {
            viewModel.stop();
        });
        binding.text.setMovementMethod(new ScrollingMovementMethod());

        viewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            binding.start.setEnabled(!processing);
            binding.stop.setEnabled(processing);
        });
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            binding.status.setText(progress);
        });
        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            binding.text.setText(result);
        });
    }
}
package com.example.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {

    // Hàm tạo nhanh Fragment với dữ liệu
    public static OnboardingFragment newInstance(String title, String desc, int imgRes) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("desc", desc);
        args.putInt("img", imgRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        ImageView img = view.findViewById(R.id.img_onboarding);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString("title"));
            tvDesc.setText(getArguments().getString("desc"));
            img.setImageResource(getArguments().getInt("img"));
        }
    }
}
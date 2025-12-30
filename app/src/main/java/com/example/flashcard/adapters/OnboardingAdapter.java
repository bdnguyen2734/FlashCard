package com.example.flashcard.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.flashcard.R;
import com.example.flashcard.ui.onboarding.OnboardingFragment;

public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:

                return OnboardingFragment.newInstance(
                        "Học nhanh với Flashcards",
                        "Ghi nhớ từ vựng hiệu quả gấp 4 lần chỉ với 5 phút mỗi ngày.",
                        R.drawable.fragment1
                );
            case 1:

                return OnboardingFragment.newInstance(
                        "Kho từ đa dạng",
                        "Gần 1000 từ vựng theo nhiều chủ đề phong phú.",
                        R.drawable.fragment2
                );
            default:
                return OnboardingFragment.newInstance(
                        "Theo dõi tiến độ",
                        "Xem thống kê kết quả học tập và cải thiện mỗi ngày.",
                        R.drawable.fragment3
                );
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
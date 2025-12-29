package com.example.flashcard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. KIỂM TRA TRẠNG THÁI NGƯỜI DÙNG ---
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        boolean isOnboardingDone = prefs.getBoolean("is_onboarding_done", false);

        if (isLoggedIn) {
            // Nếu đã đăng nhập -> Vào thẳng màn hình học
            navigateToMain();
            return; // Dừng code ở dưới
        } else if (isOnboardingDone) {
            // Nếu chưa đăng nhập nhưng đã xem intro -> Vào màn hình đăng nhập
            navigateToLogin();
            return; // Dừng code ở dưới
        }

        // --- 2. NẾU LÀ LẦN ĐẦU TIÊN MỞ APP ---
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        btnNext = findViewById(R.id.btn_onboarding_action);

        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 2) { // Trang cuối
                    btnNext.setText("Bắt đầu ngay");
                } else {
                    btnNext.setText("Tiếp tục");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < 2) {
                viewPager.setCurrentItem(current + 1);
            } else {
                // KHI BẤM "BẮT ĐẦU NGAY":
                // Lưu lại là đã xem xong Onboarding
                SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
                editor.putBoolean("is_onboarding_done", true);
                editor.apply();

                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
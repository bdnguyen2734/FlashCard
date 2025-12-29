package com.example.flashcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // -------------------- UI - Home --------------------
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private ImageButton btnAutoImages, btnReset;

    // -------------------- UI - Profile --------------------
    private ScrollView layoutProfile;
    private View layoutHome;
    private TextView tvDisplayName, tvEmailProfile, tvLearnedCount, tvTotalCount;
    private TextInputEditText edtNameProfile;
    private ImageView imgEditName;
    private Button btnSaveProfile, btnLogout;

    // -------------------- Bottom nav --------------------
    private BottomNavigationView bottomNav;

    // -------------------- Data --------------------
    private List<Category> categories;

    // Pixabay
    private static final String PIXABAY_API_KEY = "53606559-5f8d8a317e7f8da942e7b81ae";

    // Nhóm chủ đề
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Phương án dự phòng cho khóa SharedPrefs (trong trường hợp DataManager thiếu các hàm hỗ trợ tên người dùng)
    private static final String PREFS = "FLASHCARD_PREFS";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        initViews();
        setupRecyclerView();
        setupEvents();
        setupBottomNavigation();

        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
        if (layoutProfile.getVisibility() == View.VISIBLE) loadProfileData();
    }

    // -------------------- Init Views --------------------
    private void initViews() {
        // Home
        recyclerView = findViewById(R.id.category_recycler_view);
        btnAutoImages = findViewById(R.id.btn_auto_images);
        btnReset = findViewById(R.id.btn_reset_data);

        // Layout containers
        layoutHome = findViewById(R.id.layout_home_container);
        layoutProfile = findViewById(R.id.layout_profile_container);

        // Bottom nav
        bottomNav = findViewById(R.id.bottom_navigation);

        // Profile views
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvEmailProfile = findViewById(R.id.tv_email_profile);
        tvLearnedCount = findViewById(R.id.tv_learned_count);
        tvTotalCount = findViewById(R.id.tv_total_count);
        edtNameProfile = findViewById(R.id.edt_name_profile);
        imgEditName = findViewById(R.id.img_edit_name);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);

        // Ensure initial states
        layoutHome.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
        edtNameProfile.setEnabled(false);
        btnSaveProfile.setVisibility(View.GONE);

        loadProfileData();
    }

    // -------------------- RecyclerView --------------------
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categories = DataManager.loadData(this);
        adapter = new CategoryAdapter(this, categories);
        recyclerView.setAdapter(adapter);
    }

    private void refreshData() {
        categories = DataManager.loadData(this);
        if (adapter != null) adapter.setData(categories);
    }

    // -------------------- Bottom navigation --------------------
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                layoutHome.setVisibility(View.VISIBLE);
                layoutProfile.setVisibility(View.GONE);
                refreshData();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                layoutHome.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.VISIBLE);
                loadProfileData();
                return true;
            }
            return false;
        });
    }

    // -------------------- Events --------------------
    private void setupEvents() {
        // Reset
        btnReset.setOnClickListener(v -> showResetDialog());

        // Auto fetch images
        btnAutoImages.setOnClickListener(v -> startAutoFetchImages());

        // Edit name: enable edit + show save
        imgEditName.setOnClickListener(v -> {
            edtNameProfile.setEnabled(true);
            edtNameProfile.requestFocus();
            edtNameProfile.setSelection(edtNameProfile.getText() != null ? edtNameProfile.getText().length() : 0);
            btnSaveProfile.setVisibility(View.VISIBLE);
            imgEditName.setVisibility(View.GONE);
        });

        // Save name
        btnSaveProfile.setOnClickListener(v -> {
            String newName = edtNameProfile.getText() == null ? "" : edtNameProfile.getText().toString().trim();
            if (newName.isEmpty()) {
                edtNameProfile.setError("Tên không được để trống");
                return;
            }
            try {
                DataManager.updateDisplayName(this, newName);
            } catch (Exception e) {
                getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putString(KEY_USERNAME, newName).apply();
            }

            // Update UI
            tvDisplayName.setText(newName);
            edtNameProfile.setEnabled(false);
            btnSaveProfile.setVisibility(View.GONE);
            imgEditName.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Đã lưu tên mới", Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            try {
                DataManager.logout(this);
            } catch (Exception e) {
                getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
            }
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    // -------------------- Load Profile --------------------
    private void loadProfileData() {
        // 1) Email: try DataManager.getCurrentEmail
        String email = "";
        try {
            email = DataManager.getCurrentEmail(this);
        } catch (Exception ignored) {}

        if (email == null) email = "";

        // 2) Display name: try DataManager.getUserDisplayName or SharedPrefs fallback
        String displayName = null;
        try {
            displayName = DataManager.getUserDisplayName(this);
        } catch (Exception ignored) {}

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USERNAME, "").trim();
        }
        if (displayName == null || displayName.isEmpty()) {
            if (!email.isEmpty() && email.contains("@")) {
                displayName = email.substring(0, email.indexOf("@"));
            } else {
                displayName = "Người dùng";
            }
        }

        tvDisplayName.setText(displayName);
        edtNameProfile.setText(displayName);
        tvEmailProfile.setText(email.isEmpty() ? "—" : email);

        try {
            int[] stats = DataManager.getLearningStats(this); // [learned, total]
            if (stats != null && stats.length >= 2) {
                tvLearnedCount.setText(String.valueOf(stats[0]));
                tvTotalCount.setText(String.valueOf(stats[1]));
            } else {
                tvLearnedCount.setText("0");
                tvTotalCount.setText("0");
            }
        } catch (Exception e) {
            tvLearnedCount.setText("0");
            tvTotalCount.setText("0");
        }

        // ensure edit controls initial state
        edtNameProfile.setEnabled(false);
        btnSaveProfile.setVisibility(View.GONE);
        imgEditName.setVisibility(View.VISIBLE);
    }

    // -------------------- Reset Dialog --------------------
    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục dữ liệu gốc")
                .setMessage("Tất cả tiến độ học và ảnh sẽ bị xóa. Bạn có chắc?")
                .setPositiveButton("Khôi phục", (d, w) -> {
                    DataManager.resetData(this);
                    refreshData();
                    Toast.makeText(this, "Đã khôi phục dữ liệu!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // -------------------- Pixabay auto-fetch --------------------
    private void startAutoFetchImages() {
        List<Category> all = DataManager.loadData(this);
        int missing = countMissingImages(all);
        if (missing == 0) {
            Toast.makeText(this, "Tất cả từ vựng đã có ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = createProgressDialog(missing);
        dialog.show();
        executor.execute(() -> processAutoFetch(all, dialog));
    }

    private int countMissingImages(List<Category> cats) {
        int count = 0;
        for (Category c : cats)
            for (Word w : c.getWords())
                if (w.getImageUrl() == null || w.getImageUrl().trim().isEmpty())
                    count++;
        return count;
    }

    private ProgressDialog createProgressDialog(int max) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Đang tải ảnh minh họa...");
        pd.setMessage("Đang kết nối Pixabay...");
        pd.setCancelable(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(max);
        return pd;
    }

    private void processAutoFetch(List<Category> all, ProgressDialog dialog) {
        int done = 0;
        int success = 0;
        boolean changed = false;

        for (Category cat : all) {
            for (Word w : cat.getWords()) {
                if (w.getImageUrl() != null && !w.getImageUrl().trim().isEmpty()) continue;

                done++;
                updateDialog(dialog, done, w.getEnglishWord());

                String url = searchPixabayImage(w.getEnglishWord());
                if (url != null) {
                    w.setImageUrl(url);
                    success++;
                    changed = true;
                }

                try { Thread.sleep(200); } catch (Exception ignored) {}
            }
        }

        if (changed) DataManager.saveData(MainActivity.this);
        finishFetching(dialog, success);
    }

    private void updateDialog(ProgressDialog dialog, int progress, String word) {
        runOnUiThread(() -> {
            dialog.setProgress(progress);
            dialog.setMessage("Đang tìm: " + word);
        });
    }

    private void finishFetching(ProgressDialog dialog, int success) {
        runOnUiThread(() -> {
            dialog.dismiss();
            if (success > 0) {
                refreshData();
                Toast.makeText(this, "Đã cập nhật " + success + " ảnh!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Không tìm thấy ảnh phù hợp.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String searchPixabayImage(String query) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String api = "https://pixabay.com/api/?key=" + PIXABAY_API_KEY +
                    "&q=" + encoded + "&image_type=photo&per_page=3";

            HttpURLConnection conn = (HttpURLConnection) new URL(api).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);

            JSONArray hits = new JSONObject(json.toString()).getJSONArray("hits");
            if (hits.length() > 0) return hits.getJSONObject(0).getString("webformatURL");

        } catch (Exception e) {
            Log.e("Pixabay", "Error: " + e.getMessage());
        }
        return null;
    }
}

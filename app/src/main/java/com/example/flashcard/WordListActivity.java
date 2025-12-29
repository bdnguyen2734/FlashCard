package com.example.flashcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Import ImageButton
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private List<Word> wordList;
    private TextView tvTitle;
    private Button btnStart;
    private FloatingActionButton btnAdd;
    private ImageButton btnResetCategory; // Khai báo nút reset
    private String currentCategoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        // Ánh xạ View
        tvTitle = findViewById(R.id.tv_category_title);
        recyclerView = findViewById(R.id.rv_word_list);
        btnStart = findViewById(R.id.btn_start_learning);
        btnAdd = findViewById(R.id.fab_add_word);
        btnResetCategory = findViewById(R.id.btn_reset_category);

        currentCategoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (currentCategoryName != null) {
            tvTitle.setText(currentCategoryName);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadWords();

        // Sự kiện nút Bắt đầu
        btnStart.setOnClickListener(v -> startLearningSession());

        // Sự kiện nút Thêm từ
        btnAdd.setOnClickListener(v -> showAddWordDialog());

        // 👇 SỰ KIỆN NÚT RESET (HỌC LẠI TỪ ĐẦU) 👇
        btnResetCategory.setOnClickListener(v -> showResetProgressDialog());
    }

    private void showResetProgressDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Học lại bài này?")
                .setMessage("Bạn có muốn xóa hết các dấu tích xanh (✓) để học lại từ đầu không?\n(Từ vựng và hình ảnh vẫn giữ nguyên)")
                .setPositiveButton("Học lại", (dialog, which) -> {
                    // 1. Reset trong Database/File
                    DataManager.resetCategoryProgress(this, currentCategoryName);

                    // 2. Cập nhật giao diện ngay lập tức
                    if (wordList != null) {
                        for (Word w : wordList) {
                            w.setLearned(false);
                        }
                        adapter.notifyDataSetChanged(); // Mất dấu tích xanh
                    }
                    Toast.makeText(this, "Đã sẵn sàng học lại!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadWords() {
        List<Category> allCategories = DataManager.loadData(this);
        wordList = new ArrayList<>();

        for (Category cat : allCategories) {
            if (cat.getName().equals(currentCategoryName)) {
                wordList = cat.getWords();
                break;
            }
        }
        adapter = new WordAdapter(this, wordList);
        recyclerView.setAdapter(adapter);
    }

    private void startLearningSession() {
        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "Danh sách trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Word> unlearnedWords = new ArrayList<>();
        for (Word w : wordList) {
            if (!w.isLearned()) {
                unlearnedWords.add(w);
            }
        }

        if (unlearnedWords.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Hoàn thành!")
                    .setMessage("Bạn đã học hết từ vựng trong bài này.\nBấm nút mũi tên xoay ở góc trên bên phải để học lại.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Collections.shuffle(unlearnedWords);
        int count = Math.min(5, unlearnedWords.size());
        ArrayList<Word> sessionWords = new ArrayList<>(unlearnedWords.subList(0, count));

        Intent intent = new Intent(WordListActivity.this, FlashcardActivity.class);
        intent.putExtra("SESSION_WORDS", sessionWords);
        intent.putExtra("CATEGORY_NAME", currentCategoryName);
        startActivity(intent);
    }

    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_word, null);
        builder.setView(dialogView);

        EditText edtEnglish = dialogView.findViewById(R.id.edt_english);
        EditText edtVietnamese = dialogView.findViewById(R.id.edt_vietnamese);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String eng = edtEnglish.getText().toString().trim();
            String vn = edtVietnamese.getText().toString().trim();
            if (!eng.isEmpty() && !vn.isEmpty()) {
                addNewWord(eng, vn);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void addNewWord(String english, String vietnamese) {
        Word newWord = new Word();
        newWord.setEnglishWord(english);
        newWord.setVietnameseMeaning(vietnamese);
        newWord.setDefinition("Người dùng tự thêm");
        newWord.setLearned(false);

        wordList.add(newWord);
        adapter.notifyItemInserted(wordList.size() - 1);
        recyclerView.scrollToPosition(wordList.size() - 1);
        DataManager.saveData(this);
        Toast.makeText(this, "Đã thêm: " + english, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadWords();
        }
    }
}
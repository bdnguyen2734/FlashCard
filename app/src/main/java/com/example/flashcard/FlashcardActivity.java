package com.example.flashcard;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide; // Đảm bảo đã import Glide
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private List<Word> wordList;
    private int currentIndex = 0;

    // View Components
    private TextView categoryTitle, textEnglish, textPhonetic, textVietnamese, textProgress;
    private ImageView imgIllustration; // Ảnh minh họa
    private Button buttonPrev, buttonNext;
    private ImageButton btnSpeak;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        tts = new TextToSpeech(this, this);

        // Ánh xạ View
        categoryTitle = findViewById(R.id.text_category_title);
        textEnglish = findViewById(R.id.text_word_english);
        textPhonetic = findViewById(R.id.text_word_phonetic);
        textVietnamese = findViewById(R.id.text_word_vietnamese);
        imgIllustration = findViewById(R.id.img_word_illustration); // Ánh xạ ảnh
        textProgress = findViewById(R.id.text_progress);
        buttonPrev = findViewById(R.id.button_prev);
        buttonNext = findViewById(R.id.button_next);
        btnSpeak = findViewById(R.id.btn_speak);

        // Nhận dữ liệu
        wordList = (ArrayList<Word>) getIntent().getSerializableExtra("SESSION_WORDS");
        String catName = getIntent().getStringExtra("CATEGORY_NAME");

        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "Lỗi dữ liệu!", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        categoryTitle.setText(catName + " (Học từ)");
        buttonPrev.setOnClickListener(v -> showPreviousWord());
        buttonNext.setOnClickListener(v -> showNextWord());
        btnSpeak.setOnClickListener(v -> speakCurrentWord());

        displayCurrentWord();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    private void speakCurrentWord() {
        if (wordList != null && !wordList.isEmpty()) {
            tts.speak(wordList.get(currentIndex).getEnglishWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void displayCurrentWord() {
        Word currentWord = wordList.get(currentIndex);

        // 1. Hiển thị Text
        textEnglish.setText(currentWord.getEnglishWord());
        textPhonetic.setText(currentWord.getPhonetic());
        textVietnamese.setText(currentWord.getVietnameseMeaning());
        textProgress.setText((currentIndex + 1) + "/" + wordList.size());

        // 2. Hiển thị Ảnh bằng Glide
        String url = currentWord.getImageUrl();
        if (url != null && !url.isEmpty()) {
            // Nếu có link ảnh thì hiện ImageView và tải ảnh
            imgIllustration.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                    .error(R.drawable.ic_launcher_background)       // Ảnh lỗi
                    .into(imgIllustration);
        } else {
            imgIllustration.setVisibility(View.GONE);
        }
    }


    private void showPreviousWord() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentWord();
        }
    }

    private void showNextWord() {
        if (currentIndex < wordList.size() - 1) {
            currentIndex++;
            displayCurrentWord();
        } else {
            Toast.makeText(this, "Xong phần học! Chuyển sang Nối từ...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FlashcardActivity.this, QuizMatchingActivity.class);
            intent.putExtra("SESSION_WORDS", (ArrayList<Word>) wordList);
            intent.putExtra("CATEGORY_NAME", getIntent().getStringExtra("CATEGORY_NAME"));
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
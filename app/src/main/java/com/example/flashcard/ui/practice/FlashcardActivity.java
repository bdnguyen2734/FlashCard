package com.example.flashcard.ui.practice;

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

import com.bumptech.glide.Glide;
import com.example.flashcard.R;
import com.example.flashcard.models.Word;
import com.example.flashcard.ui.quiz.QuizMatchingActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private List<Word> wordList;
    private int currentIndex = 0;

    // View Components
    private TextView categoryTitle, textEnglish, textPhonetic, textVietnamese, textProgress;
    private ImageView imgIllustration;
    private Button buttonPrev, buttonNext;
    private ImageButton btnSpeak;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        tts = new TextToSpeech(this, this);

        // 1. Ánh xạ View (Đã thêm textPhonetic)
        categoryTitle = findViewById(R.id.text_category_title);
        textEnglish = findViewById(R.id.text_word_english);
        textPhonetic = findViewById(R.id.text_word_phonetic);
        textVietnamese = findViewById(R.id.text_word_vietnamese);
        imgIllustration = findViewById(R.id.img_word_illustration);
        textProgress = findViewById(R.id.text_progress);
        buttonPrev = findViewById(R.id.button_prev);
        buttonNext = findViewById(R.id.button_next);
        btnSpeak = findViewById(R.id.btn_speak);

        // 2. Nhận dữ liệu từ Intent
        wordList = (ArrayList<Word>) getIntent().getSerializableExtra("SESSION_WORDS");
        String catName = getIntent().getStringExtra("CATEGORY_NAME");

        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "Lỗi dữ liệu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        categoryTitle.setText(catName + " (Học từ)");

        // 3. Gán sự kiện click
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

        // --- A. Hiển thị Text Tiếng Anh ---
        textEnglish.setText(currentWord.getEnglishWord());
        String phonetic = currentWord.getPhonetic();
        if (phonetic != null && !phonetic.trim().isEmpty()) {
            textPhonetic.setText(phonetic);
            textPhonetic.setVisibility(View.VISIBLE); // Hiện
        } else {
            textPhonetic.setVisibility(View.GONE);    // Ẩn
        }


        textVietnamese.setText(currentWord.getVietnameseMeaning());
        textProgress.setText((currentIndex + 1) + "/" + wordList.size());

        // Hiển thị Ảnh bằng Glide
        String url = currentWord.getImageUrl();
        if (url != null && !url.isEmpty()) {
            imgIllustration.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fitCenter() // Giúp ảnh hiển thị vừa vặn
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
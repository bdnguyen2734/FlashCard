package com.example.flashcard.ui.quiz;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View; // Import View để dùng VISIBLE/GONE
import android.widget.Button;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide; // Import Glide để tải ảnh
import com.example.flashcard.models.Category;
import com.example.flashcard.R;
import com.example.flashcard.models.Word;
import com.example.flashcard.ui.library.WordListActivity;
import com.example.flashcard.data.DataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizGuessActivity extends AppCompatActivity {

    private ArrayList<Word> sessionWords;
    private int currentIndex = 0;
    private int score = 0;

    private TextView tvQuestion;
    private ImageView imgQuestion;
    private Button[] btnAnswers = new Button[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_guess);

        // Ánh xạ View
        tvQuestion = findViewById(R.id.text_question_word);
        imgQuestion = findViewById(R.id.img_question); // Ánh xạ ImageView từ layout

        btnAnswers[0] = findViewById(R.id.btn_ans_1);
        btnAnswers[1] = findViewById(R.id.btn_ans_2);
        btnAnswers[2] = findViewById(R.id.btn_ans_3);
        btnAnswers[3] = findViewById(R.id.btn_ans_4);
        btnAnswers[4] = findViewById(R.id.btn_ans_5);

        sessionWords = (ArrayList<Word>) getIntent().getSerializableExtra("SESSION_WORDS");
        if (sessionWords == null) { finish(); return; }

        Collections.shuffle(sessionWords);
        loadQuestion();
    }

    private void loadQuestion() {
        if (currentIndex >= sessionWords.size()) {
            finishSession();
            return;
        }

        Word correctWord = sessionWords.get(currentIndex);

        // 1. Hiển thị nghĩa Tiếng Việt
        tvQuestion.setText(correctWord.getVietnameseMeaning());

        // 2. Hiển thị Ảnh minh họa
        String url = correctWord.getImageUrl();
        if (url != null && !url.isEmpty()) {
            imgQuestion.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ (tùy chọn)
                    .into(imgQuestion);
        } else {
            // Nếu không có ảnh thì ẩn đi để layout gọn hơn
            imgQuestion.setVisibility(View.GONE);
        }

        // 3. Tạo danh sách đáp án
        List<Word> options = new ArrayList<>();
        options.add(correctWord);

        List<Word> distractions = new ArrayList<>(sessionWords);
        distractions.remove(correctWord);
        options.addAll(distractions);

        // Đảm bảo đủ 5 đáp án (phòng trường hợp sessionWords < 5)
        while (options.size() < 5 && !distractions.isEmpty()) {
            options.add(distractions.get(0));
        }

        Collections.shuffle(options);

        // 4. Gán dữ liệu vào các nút
        for (int i = 0; i < 5; i++) {
            Button btn = btnAnswers[i];
            // Kiểm tra nếu i vượt quá số lượng option (trường hợp danh sách < 5 từ)
            if (i < options.size()) {
                Word option = options.get(i);
                btn.setVisibility(View.VISIBLE);
                btn.setText(option.getEnglishWord());
                btn.setBackgroundResource(R.drawable.bg_quiz_answer);

                btn.setEnabled(true);
                btn.setOnClickListener(v -> checkAnswer(btn, option, correctWord));
            } else {
                btn.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void checkAnswer(Button selectedBtn, Word selectedOption, Word correctWord) {
        for (Button btn : btnAnswers) btn.setEnabled(false);

        if (selectedOption.getEnglishWord().equals(correctWord.getEnglishWord())) {
            // ĐÚNG -> Đổi sang nền xanh
            selectedBtn.setBackgroundResource(R.drawable.bg_card_correct);
            score++;
        } else {
            // SAI -> Đổi sang nền đỏ
            selectedBtn.setBackgroundResource(R.drawable.bg_card_wrong);

            // Hiện đáp án đúng
            for (Button btn : btnAnswers) {
                if (btn.getText().toString().equals(correctWord.getEnglishWord())) {
                    btn.setBackgroundResource(R.drawable.bg_card_correct);
                }
            }
        }

        // Chuyển câu sau 1 giây
        new Handler().postDelayed(() -> {
            currentIndex++;
            loadQuestion();
        }, 1000);
    }

    private void finishSession() {
        List<Category> allCategories = DataManager.loadData(this);
        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        for (Category cat : allCategories) {
            if (cat.getName().equals(categoryName)) {
                for (Word w : cat.getWords()) {
                    for (Word sessionWord : sessionWords) {
                        if (w.getEnglishWord().equals(sessionWord.getEnglishWord())) {
                            w.setLearned(true); // Đánh dấu đã học
                        }
                    }
                }
                break;
            }
        }
        DataManager.saveData(this); // Lưu lại tiến độ

        Toast.makeText(this, "Hoàn thành! Bạn đúng " + score + "/" + sessionWords.size() + " câu.", Toast.LENGTH_LONG).show();

        // Quay về danh sách từ vựng
        Intent intent = new Intent(this, WordListActivity.class);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
package com.example.flashcard.ui.quiz;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.flashcard.models.MatchingCard;
import com.example.flashcard.R;
import com.example.flashcard.models.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizMatchingActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView progressTextView;
    private ArrayList<Word> sessionWords;

    private View firstCardView = null;
    private int pairsFound = 0;
    private int totalPairs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_matching);

        gridLayout = findViewById(R.id.matching_grid_layout);
        progressTextView = findViewById(R.id.tv_progress);

        sessionWords = (ArrayList<Word>) getIntent().getSerializableExtra("SESSION_WORDS");
        if (sessionWords == null || sessionWords.isEmpty()) {
            finish(); return;
        }

        totalPairs = sessionWords.size();
        setupGameSplitColumns();
    }

    private void setupGameSplitColumns() {
        List<MatchingCard> englishCards = new ArrayList<>();
        List<MatchingCard> vietnameseCards = new ArrayList<>();

        for (int i = 0; i < sessionWords.size(); i++) {
            Word word = sessionWords.get(i);
            String pairId = "pair_" + i;

            // Cột trái: Tiếng Anh
            MatchingCard engCard = new MatchingCard(word.getEnglishWord(), pairId, null);
            englishCards.add(engCard);

            // Cột phải: Tiếng Việt (Kèm Link Ảnh)
            MatchingCard vnCard = new MatchingCard(word.getVietnameseMeaning(), pairId, word.getEnglishWord());
            vnCard.imageUrl = word.getImageUrl();
            vietnameseCards.add(vnCard);
        }

        Collections.shuffle(englishCards);
        Collections.shuffle(vietnameseCards);

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(2);

        for (int i = 0; i < sessionWords.size(); i++) {
            addCardToGrid(englishCards.get(i), false);
            addCardToGrid(vietnameseCards.get(i), true);
        }
        updateProgress();
    }

    private void addCardToGrid(MatchingCard card, boolean showImage) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_matching_card, gridLayout, false);

        TextView tvText = cardView.findViewById(R.id.tv_card_text);
        ImageView imgIcon = cardView.findViewById(R.id.img_card_icon);

        tvText.setText(card.text);
        cardView.setTag(card);

        if (showImage && card.imageUrl != null && !card.imageUrl.isEmpty()) {
            imgIcon.setVisibility(View.VISIBLE);
            Glide.with(this).load(card.imageUrl).into(imgIcon);
        } else {
            imgIcon.setVisibility(View.GONE);
        }

        // CẤU HÌNH LAYOUT GRID
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0; // Để weight=1 tự chia đều chiều ngang

        // 👇👇 QUAN TRỌNG: TÍNH CHIỀU CAO THEO DP (KHÔNG DÙNG PIXEL CỐ ĐỊNH) 👇👇
        int heightInDp = 150;
        int heightInPx = (int) (heightInDp * getResources().getDisplayMetrics().density);
        params.height = heightInPx;

        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Chia đều cột (weight=1)
        params.setMargins(12, 12, 12, 12); // Khoảng cách giữa các ô

        cardView.setLayoutParams(params);
        cardView.setOnClickListener(this::onCardClicked);

        gridLayout.addView(cardView);
    }

    private void onCardClicked(View view) {
        if (!view.isEnabled() || view == firstCardView) return;

        view.setBackgroundResource(R.drawable.bg_card_selected);

        if (firstCardView == null) {
            firstCardView = view;
        } else {
            setAllCardsClickable(false);
            MatchingCard firstCardData = (MatchingCard) firstCardView.getTag();
            MatchingCard currentCardData = (MatchingCard) view.getTag();

            if (firstCardData.pairId.equals(currentCardData.pairId)) {
                // ĐÚNG
                pairsFound++;
                updateProgress();
                firstCardView.setBackgroundResource(R.drawable.bg_card_correct);
                view.setBackgroundResource(R.drawable.bg_card_correct);
                firstCardView.setEnabled(false);
                view.setEnabled(false);
                firstCardView = null;
                setAllCardsClickable(true);

                if (pairsFound == totalPairs) {
                    new Handler().postDelayed(this::showGameResults, 500);
                }
            } else {
                // SAI
                firstCardView.setBackgroundResource(R.drawable.bg_card_wrong);
                view.setBackgroundResource(R.drawable.bg_card_wrong);

                View finalFirst = firstCardView;
                new Handler().postDelayed(() -> {
                    finalFirst.setBackgroundResource(R.drawable.bg_matching_card);
                    view.setBackgroundResource(R.drawable.bg_matching_card);
                    firstCardView = null;
                    setAllCardsClickable(true);
                }, 500);
            }
        }
    }

    private void setAllCardsClickable(boolean clickable) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child.isEnabled()) child.setClickable(clickable);
        }
    }

    private void updateProgress() {
        progressTextView.setText("Tìm cặp đúng: " + pairsFound + "/" + totalPairs);
    }

    private void showGameResults() {
        Intent intent = new Intent(QuizMatchingActivity.this, QuizGuessActivity.class);
        intent.putExtra("SESSION_WORDS", sessionWords);
        intent.putExtra("CATEGORY_NAME", getIntent().getStringExtra("CATEGORY_NAME"));
        startActivity(intent);
        finish();
    }
}
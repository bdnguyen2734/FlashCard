package com.example.flashcard.models;

// Lớp này giúp theo dõi từng thẻ trong trò chơi Ghép đôi
public class MatchingCard {
    public final String text; // Nội dung hiển thị (Tiếng Anh hoặc Tiếng Việt)
    public final String pairId; // ID duy nhất để xác định cặp (Ví dụ: "apple_pair")
    public final String originalWord; // Từ tiếng Anh gốc
    public String imageUrl;
    public MatchingCard(String text, String pairId, String originalWord) {
        this.text = text;
        this.pairId = pairId;
        this.originalWord = originalWord;
    }
}
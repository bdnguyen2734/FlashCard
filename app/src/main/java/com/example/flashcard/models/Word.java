package com.example.flashcard.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // <--- QUAN TRỌNG: Import thư viện này

// 👇 QUAN TRỌNG: Thêm "implements Serializable" để truyền được giữa các màn hình
public class Word implements Serializable {

    // Khớp với key "word" trong JSON
    @SerializedName("word")
    private String englishWord;

    // Khớp với key "vietnamese" trong JSON
    @SerializedName("vietnamese")
    private String vietnameseMeaning;

    @SerializedName("phonetic")
    private String phonetic;

    @SerializedName("definition")
    private String definition;

    @SerializedName("example")
    private String example;

    // Khớp với key "image_url" (Giữ lại để dùng sau này)
    @SerializedName("image_url")
    private String imageUrl;

    // 👇 Biến theo dõi trạng thái học (Mặc định là false - chưa học)
    // Biến này không có trong JSON nên không cần @SerializedName
    private boolean isLearned = false;

    // Constructor mặc định (Bắt buộc phải có để Gson hoạt động)
    public Word() {
    }

    // Constructor đầy đủ (Dùng khi bạn tạo từ mới bằng code thủ công)
    public Word(String englishWord, String vietnameseMeaning, String phonetic, String definition, String example) {
        this.englishWord = englishWord;
        this.vietnameseMeaning = vietnameseMeaning;
        this.phonetic = phonetic;
        this.definition = definition;
        this.example = example;
    }

    // --- Các hàm Getters và Setters ---

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getVietnameseMeaning() {
        return vietnameseMeaning;
    }

    public void setVietnameseMeaning(String vietnameseMeaning) {
        this.vietnameseMeaning = vietnameseMeaning;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter cho trạng thái đã học
    public boolean isLearned() {
        return isLearned;
    }

    // Setter cho trạng thái đã học
    public void setLearned(boolean learned) {
        isLearned = learned;
    }
}
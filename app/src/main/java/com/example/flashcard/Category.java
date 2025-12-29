package com.example.flashcard;

import java.util.List;

public class Category {
    private String name;       // Tên danh mục (Ví dụ: Animals)
    private List<Word> words;  // Danh sách từ vựng trong danh mục

    // Constructor
    public Category(String name, List<Word> words) {
        this.name = name;
        this.words = words;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public int getWordCount() {
        return words != null ? words.size() : 0;
    }

    public int getLearnedCount() {
        if (words == null) return 0;
        int count = 0;
        for (Word w : words) {
            if (w.isLearned()) {
                count++;
            }
        }
        return count;
    }
}
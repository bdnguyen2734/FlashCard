package com.example.flashcard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    // Hàm cập nhật dữ liệu mới (Dùng để refresh danh sách khi quay lại từ màn hình khác)
    public void setData(List<Category> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.nameTextView.setText(category.getName());

        // Lấy tổng số từ HIỆN TẠI (Đã trừ đi các từ bị xóa)
        int totalCount = category.getWords().size();
        holder.countTextView.setText(totalCount + " từ");

        // --- TÍNH TOÁN PHẦN TRĂM CHÍNH XÁC ---
        int learnedCount = 0;
        for (Word w : category.getWords()) {
            if (w.isLearned()) {
                learnedCount++;
            }
        }

        if (totalCount > 0) {
            // Ép kiểu sang double để tính toán số thập phân (Ví dụ: 5.0 / 40 = 0.125)
            double percentage = (double) learnedCount * 100 / totalCount;

            // Kiểm tra: Nếu là số tròn (ví dụ 50.0) thì hiện "50%", nếu lẻ (12.5) thì hiện "12.5%"
            if (percentage == (int) percentage) {
                holder.tvPercent.setText(String.format(Locale.US, "%d%%", (int) percentage));
            } else {
                // %.1f nghĩa là lấy 1 số sau dấu phẩy
                holder.tvPercent.setText(String.format(Locale.US, "%.1f%%", percentage));
            }
        } else {
            holder.tvPercent.setText("0%");
        }

        holder.btnPlay.setOnClickListener(v -> startQuickSession(category));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordListActivity.class);
            intent.putExtra("CATEGORY_NAME", category.getName());
            context.startActivity(intent);
        });
    }

    private void startQuickSession(Category category) {
        List<Word> unlearnedWords = new ArrayList<>();
        for (Word w : category.getWords()) {
            if (!w.isLearned()) unlearnedWords.add(w);
        }

        if (unlearnedWords.isEmpty()) {
            Toast.makeText(context, "Đã học hết danh mục này!", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.shuffle(unlearnedWords);
        int count = Math.min(5, unlearnedWords.size());
        ArrayList<Word> sessionWords = new ArrayList<>(unlearnedWords.subList(0, count));

        Intent intent = new Intent(context, FlashcardActivity.class);
        intent.putExtra("SESSION_WORDS", sessionWords);
        intent.putExtra("CATEGORY_NAME", category.getName());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView countTextView;
        TextView tvPercent;
        LinearLayout btnPlay;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_category_name);
            countTextView = itemView.findViewById(R.id.text_word_count);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}
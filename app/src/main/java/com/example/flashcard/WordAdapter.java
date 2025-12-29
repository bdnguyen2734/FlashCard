package com.example.flashcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private final Context context;
    private final List<Word> wordList;

    public WordAdapter(Context context, List<Word> wordList) {
        this.context = context;
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);

        // 1. Hiển thị nội dung
        holder.tvEnglish.setText(word.getEnglishWord());
        holder.tvVietnamese.setText(word.getVietnameseMeaning());

        // 2. Kiểm tra trạng thái "Đã học" để hiện dấu tích
        if (word.isLearned()) {
            holder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            holder.imgCheck.setVisibility(View.GONE);
        }

        // 3. Xử lý sự kiện nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                // Xóa khỏi danh sách hiển thị
                wordList.remove(currentPos);

                // Cập nhật giao diện
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, wordList.size());

                // LƯU DỮ LIỆU VÀO BỘ NHỚ MÁY
                DataManager.saveData(context);

                Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish, tvVietnamese;
        ImageButton btnDelete;
        ImageView imgCheck; // Khai báo ảnh dấu tích

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tv_english_word);
            tvVietnamese = itemView.findViewById(R.id.tv_vietnamese_meaning);
            btnDelete = itemView.findViewById(R.id.btn_delete_word);
            imgCheck = itemView.findViewById(R.id.img_learned_check); // Ánh xạ ID từ XML
        }
    }
}
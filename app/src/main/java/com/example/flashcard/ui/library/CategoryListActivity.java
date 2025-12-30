package com.example.flashcard.ui.library;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

import com.example.flashcard.models.Category;
import com.example.flashcard.adapters.CategoryAdapter;
import com.example.flashcard.R;
import com.example.flashcard.data.DataManager;

import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        // 1. Tải dữ liệu từ JSON
        categories = DataManager.loadData(this);

        // 2. Thiết lập RecyclerView
        recyclerView = findViewById(R.id.category_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Thiết lập Adapter
        adapter = new CategoryAdapter(this, categories);
        recyclerView.setAdapter(adapter);
    }
}
package com.example.multiplequiz;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiplequiz.Common.SpaceDecoration;
import com.example.multiplequiz.DBHelper.DBHelper;
import com.example.multiplequiz.adapter.CategoryAdapter;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recycler_category;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Multiple Quiz");
        toolbar.setTitleTextColor(R.color.colorAccent);
        setSupportActionBar(toolbar);

        recycler_category = findViewById(R.id.recycler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this, 2));

        //get screen height
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this,
                DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 16;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);
    }
}

package com.lee.thanos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<ItemBean> mList = new ArrayList<>();
    private RecyclerAdapter mRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.start);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerAdapter = new RecyclerAdapter(this);
        setDummyData(mList);
        mRecyclerAdapter.setData(mList);
        DisappearItemAnimation disappearAnimation = new DisappearItemAnimation();
        mRecyclerView.setItemAnimator(disappearAnimation);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomDisappearAHalf();
            }
        });
    }

    private void setDummyData(ArrayList<ItemBean> mList) {
        for (int i = 0; i < 10; i++) {
            mList.add(new ItemBean("This is Item " + i,
                    "This is Item " + i + ",jfhjshfjhadnbdheiqiuodjabe",
                    "", "item " + i, 0));
        }
    }

    private void randomDisappearAHalf() {

        if (mList != null && !mList.isEmpty()) {

            int size = mList.size();
            HashSet<Integer> set = new HashSet<>();
            while (set.size() < size / 2) {
                Random random = new Random();
                int anInt = random.nextInt(size);
                set.add(anInt);
            }

            for (Integer i : set) {
                mList.get(i).type = 1;
                mRecyclerAdapter.notifyItemChanged(i);
            }


        }
    }


}

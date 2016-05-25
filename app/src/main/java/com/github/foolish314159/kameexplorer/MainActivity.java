package com.github.foolish314159.kameexplorer;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ExplorerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Explorer mExplorer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ExplorerAdapter(new ArrayList<File>());
        mRecyclerView.setAdapter(mAdapter);

        mExplorer = new Explorer();
        if (mExplorer.hasReadPermission(this)) {
            displayFiles();
        } else {
            mExplorer.requestReadPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Explorer.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                displayFiles();
            }
        }
    }

    private void displayFiles() {
        mAdapter.addAll(mExplorer.listFilesSorted());
    }

}

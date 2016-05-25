package com.github.foolish314159.kameexplorer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ExplorerAdapter.ExplorerOnFileClickListener {

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
        mRecyclerView.addItemDecoration(new ExplorerItemSpacingDecoration(5));
        mAdapter = new ExplorerAdapter(this);
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
        mAdapter.clear();
        mAdapter.addAll(mExplorer.listFilesSorted());
        mRecyclerView.scrollToPosition(0);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFileClick(File file) {
        if (file.isDirectory()) {
            mExplorer.setCurrentPath(file.getAbsolutePath());
            displayFiles();
        } else {
            Toast.makeText(this, file.getPath() + " / " + ExplorerAdapter.getMimeType(file), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), ExplorerAdapter.getMimeType(file));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;

            if (isIntentSafe) {
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mExplorer.historyHasPrevious()) {
            mExplorer.historyBack();
            displayFiles();
        } else {
            super.onBackPressed();
        }
    }
}

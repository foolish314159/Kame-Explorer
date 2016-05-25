package com.github.foolish314159.kameexplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * Created by Tom on 25.05.2016.
 */
public class Explorer {

    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private static final String DEFAULT_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private String rootPath;
    private String currentPath;

    private Stack<String> pathHistory;

    public Explorer() {
        this(DEFAULT_ROOT_PATH, DEFAULT_ROOT_PATH);
    }

    public Explorer(String rootPath) {
        this(rootPath, rootPath);
    }

    public Explorer(String rootPath, String currentPath) {
        this.rootPath = rootPath;
        this.currentPath = currentPath;

        pathHistory = new Stack<>();
        pathHistory.push(currentPath);
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
        pathHistory.push(currentPath);
    }

    public boolean historyHasPrevious() {
        return pathHistory.size() > 1;
    }

    public void historyBack() {
        pathHistory.pop();
        currentPath = pathHistory.peek();
    }

    public List<File> listFiles() {
        File dir = new File(currentPath);
        return Arrays.asList(dir.listFiles());
    }

    public List<File> listFilesSorted() {
        return listFilesSorted(null);
    }

    public List<File> listFilesSorted(Comparator<File> comparator) {
        List<File> files = listFiles();

        if (comparator != null) {
            Collections.sort(files, comparator);
        } else {
            Collections.sort(files, new DefaultFileComparator());
        }

        return files;
    }

    public boolean hasReadPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasWritePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadPermission(Activity activity) {
        if (!hasReadPermission(activity)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "READ_EXTERNAL_STORAGE permission required", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private static class DefaultFileComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() == rhs.isDirectory()) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            } else {
                if (lhs.isDirectory()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

}

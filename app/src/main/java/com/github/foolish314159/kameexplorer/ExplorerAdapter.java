package com.github.foolish314159.kameexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Tom on 25.05.2016.
 */
public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ViewHolder> {

    public static interface ExplorerOnFileClickListener {
        void onFileClick(File file);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View itemView;
        public TextView textView;
        public File file;
        public ExplorerOnFileClickListener listener;
        public ViewHolder(View v, ExplorerOnFileClickListener listener) {
            super(v);
            itemView = v;
            textView = (TextView) v.findViewById(android.R.id.text1);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onFileClick(file);
            }
        }
    }

    private Context mContext;
    private List<File> mFiles;
    private LruCache<String, Drawable> mIconCache;

    public ExplorerAdapter(Context context) {
        mContext = context;
        mFiles = new ArrayList<>();

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mIconCache = new LruCache<String, Drawable>(maxMemory / 10) {
            @Override
            protected int sizeOf(String key, Drawable drawable) {
                return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
            }
        };
    }

    public void addAll(List<File> files) {
        mFiles.addAll(files);
    }

    public void clear() {
        mFiles.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        ViewHolder vh;
        if (mContext instanceof ExplorerOnFileClickListener) {
            vh = new ViewHolder(v, (ExplorerOnFileClickListener) mContext);
        } else {
            vh = new ViewHolder(v, null);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = mFiles.get(position);
        holder.file = file;
        holder.textView.setText(file.getName());

        Drawable icon = getIconDrawable(file);
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        holder.textView.setCompoundDrawablePadding(20);

        String mimeType = getMimeType(file);
        if (mimeType != null && mimeType.startsWith("image")) {
            executor.execute(new ThumbnailWorker(file, holder));
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static String getMimeType(File file) {
        String mimeType = null;
        String extension = null;
        int indexDot = file.getPath().lastIndexOf('.');
        if (indexDot > 0) {
            extension = file.getPath().substring(indexDot + 1);
        }
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    private Drawable getIconDrawable(File file) {
        if (file.isDirectory()) {
            return drawableFromResourceId(R.drawable.folder);
        }

        String mimeType = null;
        String extension = null;
        int indexDot = file.getPath().lastIndexOf('.');
        if (indexDot > 0) {
            extension = file.getPath().substring(indexDot + 1);
        }
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            // Custom extensions
            if (mimeType != null) {
                if (mimeType.equals("application/vnd.android.package-archive")) {
                    PackageInfo info = mContext.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0);
                    info.applicationInfo.sourceDir = file.getAbsolutePath();
                    info.applicationInfo.publicSourceDir = file.getAbsolutePath();
                    return info.applicationInfo.loadIcon(mContext.getPackageManager());
                }
            }
        }

        if (mimeType == null) {
            return drawableFromResourceId(R.drawable.file);
        }

        Drawable icon = mIconCache.get(extension);
        if (icon != null) {
            return icon;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.fromFile(file));
        intent.setType(mimeType);
        List<ResolveInfo> matches = mContext.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo match : matches) {
            icon = match.loadIcon(mContext.getPackageManager());
            mIconCache.put(extension, icon);
            return icon;
        }

        return drawableFromResourceId(R.drawable.file);
    }

    private Drawable drawableFromResourceId(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mContext.getDrawable(id);
        } else {
            return mContext.getResources().getDrawable(id);
        }
    }

    private Executor executor = Executors.newFixedThreadPool(5);

    private class ThumbnailWorker implements Runnable {

        private File file;
        private ViewHolder viewHolder;

        public ThumbnailWorker(File file, ViewHolder viewHolder) {
            this.file = file;
            this.viewHolder = viewHolder;
        }

        @Override
        public void run() {
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), 144, 144, 0);
            final BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), thumbnail);
            if (viewHolder.file.getAbsolutePath().equals(file.getAbsolutePath())) {
                if (mContext instanceof Activity) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                        }
                    });
                }
            }
        }
    }

}

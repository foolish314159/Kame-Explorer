package com.github.foolish314159.kameexplorer;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Tom on 25.05.2016.
 */
public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView textView;
        public ViewHolder(View v) {
            super(v);
            itemView = v;
            textView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    private List<File> mFiles;

    public ExplorerAdapter(List<File> files) {
        mFiles = files;
    }

    public void addAll(List<File> files) {
        mFiles.addAll(files);
        notifyDataSetChanged();
    }

    public void clear() {
        mFiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(mFiles.get(position).getName());
        holder.itemView.setBackgroundColor(mFiles.get(position).isDirectory() ? Color.TRANSPARENT : Color.GRAY);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

}

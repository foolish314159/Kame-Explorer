package com.github.foolish314159.kameexplorer;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Tom on 25.05.2016.
 */
public class ExplorerItemSpacingDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;

    public ExplorerItemSpacingDecoration(int space) {
        mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mSpace;
    }
}

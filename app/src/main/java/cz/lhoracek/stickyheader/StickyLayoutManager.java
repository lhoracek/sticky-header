package cz.lhoracek.stickyheader;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lhoracek on 8/31/15.
 */
public class StickyLayoutManager extends LinearLayoutManager {
    public StickyLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        View v = getChildAt(0);

    }
}

package cz.lhoracek.stickyheaders;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lhoracek on 8/31/15.
 */
public class StickyItemDecoration extends RecyclerView.ItemDecoration implements View.OnClickListener {
    private final static String TAG = StickyItemDecoration.class.getSimpleName();

    RecyclerView.Adapter mAdapter = null;
    private Map<Integer, Map<Integer, Integer>> headerPositionCache = new HashMap<>(); // map <itemPosition - map <viewType - headerPosition>>

    private final int h1ViewType, h2ViewType;

    public StickyItemDecoration(RecyclerView.Adapter mAdapter, int h1ViewType, int h2ViewType) {
        this.mAdapter = mAdapter;
        this.h1ViewType = h1ViewType;
        this.h2ViewType = h2ViewType;
    }

    public void clearCaches() {
        headerPositionCache.clear();
    }

    /**
     * Get header position for current item position
     * Header gets itself returned when looked for
     *
     * @param viewType     - header type
     * @param fromPosition - current item position
     * @return
     */
    private int getHeaderPosition(int viewType, int fromPosition) {
        if (mAdapter != null) {
            if (!headerPositionCache.containsKey(fromPosition)) {
                headerPositionCache.put(fromPosition, new HashMap<Integer, Integer>());
            }
            Map<Integer, Integer> positionHeaderCache = headerPositionCache.get(fromPosition);
            if (positionHeaderCache.containsKey(viewType)) {
                return positionHeaderCache.get(viewType);
            }
            for (int position = fromPosition; position >= 0; position--) {
                if (viewType == mAdapter.getItemViewType(position)) {
                    positionHeaderCache.put(viewType, position);
                    return position;
                }
            }
            positionHeaderCache.put(viewType, -1);
        }
        return -1;
    }


    private View getItemView(RecyclerView parent, int adapterPos) {
        RecyclerView.ViewHolder vh = mAdapter.onCreateViewHolder(parent, mAdapter.getItemViewType(adapterPos));
        mAdapter.onBindViewHolder(vh, adapterPos);
        measureView(parent, vh.itemView);
        return vh.itemView;
    }

    private void measureView(RecyclerView parent, View header) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

        header.measure(childWidth, childHeight);
        header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
    }

    /**
     * Get top offset of view including vertical transaltion
     *
     * @param child
     * @return
     */
    private int getAnimatedTop(View child) {
        return child.getTop() + (int) child.getTranslationY();
    }

    /**
     * Draw over recycler view
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final int count = parent.getChildCount();

        int topHeaderPosition = -1;
        int topHeaderOffset = 0;
        View topHeaderView = null;

        int subHeaderPosition = -1;
        int subHeaderOffset = 0;
        View subHeaderView = null;

        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            final View child = parent.getChildAt(layoutPos);
            boolean visible = getAnimatedTop(child) > -child.getHeight()/* && child.getTop() < parent.getHeight()*/;
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (visible && adapterPos != RecyclerView.NO_POSITION) {

                int childH2position = getHeaderPosition(h2ViewType, adapterPos);
                if (subHeaderPosition < 0) {// we do not have any sub header yet
                    if (childH2position >= 0) { // and we found content with header
                        if (getAnimatedTop(child) <= ((topHeaderPosition >= 0) ? topHeaderView.getHeight() : 0)) {
                            subHeaderPosition = childH2position;
                            subHeaderView = getItemView(parent, childH2position);
                        }
                    }
                } else { // we already have header position
                    if (childH2position >= 0 && childH2position != subHeaderPosition) {
                        int topHeaderBottom = topHeaderPosition >= 0 ? getItemView(parent, topHeaderPosition).getHeight() : 0;
                        if (getAnimatedTop(child) < topHeaderBottom) {
                            subHeaderPosition = childH2position;
                            subHeaderView = getItemView(parent, childH2position);
                        } else {
                            if (getAnimatedTop(child) < topHeaderBottom + subHeaderView.getHeight()) { // new header in forthcommitng content
                                subHeaderOffset = (topHeaderBottom + subHeaderView.getHeight()) - getAnimatedTop(child);
                            }
                        }
                    }
                }

                int childH1Position = getHeaderPosition(h1ViewType, adapterPos);
                if (topHeaderPosition < 0) { // dont have any header found
                    if (childH1Position >= 0) { // this child has header
                        //if (getAnimatedTop(child) <= 0) { // item is off the screen
                        topHeaderPosition = childH1Position; // get adapter position of its top header
                        topHeaderView = getItemView(parent, topHeaderPosition);
                        //   }
                    }
                } else {
                    if (childH1Position >= 0 && childH1Position != topHeaderPosition) {
                        if (getAnimatedTop(child) < topHeaderView.getHeight()) {
                            topHeaderOffset = topHeaderView.getHeight() - getAnimatedTop(child);
                        }
                        if (subHeaderPosition >= 0) { // we have subheader, but next top header is comming
                            if (getAnimatedTop(child) < topHeaderView.getHeight()) {
                                subHeaderPosition = -1;
                            } else if (getAnimatedTop(child) < topHeaderView.getHeight() + subHeaderView.getHeight()) {
                                subHeaderOffset += (topHeaderView.getHeight() + subHeaderView.getHeight()) - getAnimatedTop(child);
                            }
                        }
                    }
                }
                if (topHeaderPosition > subHeaderPosition) { // if top header is higher, do not draw subheader of previous items
                    subHeaderPosition = -1;
                }
            }
        }

        c.save();
        c.translate(0, -topHeaderOffset);
        if (subHeaderPosition >= 0) {
            c.translate(0, (topHeaderPosition >= 0 ? (topHeaderView.getHeight()) : 0) - subHeaderOffset);
            subHeaderView.draw(c);
        }
        if (topHeaderPosition >= 0) {
            c.translate(0, subHeaderPosition >= 0 ? (-topHeaderView.getHeight() + subHeaderOffset) : 0);
            topHeaderView.draw(c);
        }
        Log.d(TAG, "TO>" + topHeaderOffset + " SO>" + subHeaderOffset + " TP>" + topHeaderPosition + " SP>" + subHeaderPosition);
        c.restore();
    }


    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked");
    }
}

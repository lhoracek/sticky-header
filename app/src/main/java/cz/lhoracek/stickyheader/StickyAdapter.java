package cz.lhoracek.stickyheader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lhoracek on 8/31/15.
 */
public class StickyAdapter extends RecyclerView.Adapter<StickyHolder> {

    @Override
    public int getItemCount() {
        return 1000;
    }

    @Override
    public StickyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View text = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_text, parent, false);
        if (viewType == 1)
            text.setBackgroundColor(parent.getContext().getResources().getColor(R.color.green));
        if (viewType == 2)
            text.setBackgroundColor(parent.getContext().getResources().getColor(R.color.blue));
        return new StickyHolder(text);
    }

    @Override
    public void onBindViewHolder(StickyHolder holder, int position) {
        holder.getTextView().setText("Position : " + position);
    }

    private int getHeaderPosition(int fromPosition) {
        for (int position = fromPosition; position >= 0; position--) {
            if (1 == getItemViewType(position)) {
                return position;
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 10 == 0) return 1;
        if ((position+1) % 2 == 0) return 2;
        return 0;
    }

    public void onItemClicked(int adapterPos) {

    }
}

package cz.lhoracek.stickyheader;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by lhoracek on 8/31/15.
 */
public class StickyHolder extends RecyclerView.ViewHolder {
    public StickyHolder(View itemView) {
        super(itemView);
    }

    public TextView getTextView() {
        return (TextView) itemView;
    }
}

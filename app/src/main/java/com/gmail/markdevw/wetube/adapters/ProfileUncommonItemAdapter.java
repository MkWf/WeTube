package com.gmail.markdevw.wetube.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.TagItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/17/2015.
 */
public class ProfileUncommonItemAdapter extends RecyclerView.Adapter<ProfileUncommonItemAdapter.ItemAdapterViewHolder> {
    
    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tag_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = WeTubeApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getUncommonTags().get(index));
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getUncommonTags().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.tag_item_tag) TextView mTag;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void update(TagItem tagItem) {
            mTag.setText(tagItem.getTag());
        }
    }
}


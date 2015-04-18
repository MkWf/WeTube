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

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 4/17/2015.
 */
public class ProfileUncommonItemAdapter extends RecyclerView.Adapter<ProfileUncommonItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(ProfileCommonItemAdapter itemAdapter, TagItem tag);
    }

    private WeakReference<Delegate> delegate;

    public Delegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }
    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }


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

        TextView tag;
        TagItem tagItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            tag = (TextView) itemView.findViewById(R.id.tag_item_tag);

        }

        void update(TagItem tagItem) {
            this.tagItem = tagItem;

            tag.setText(tagItem.getTag());
        }
    }
}


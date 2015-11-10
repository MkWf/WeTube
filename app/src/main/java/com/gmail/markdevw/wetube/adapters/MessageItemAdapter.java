package com.gmail.markdevw.wetube.adapters;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mark on 4/7/2015.
 */
public class MessageItemAdapter extends RecyclerView.Adapter<MessageItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onMessageVideoItemClicked(MessageItemAdapter itemAdapter, String title, String thumbnail, String id);
    }

    private WeakReference<Delegate> mDelegate;

    public Delegate getDelegate() {
        if (mDelegate == null) {
            return null;
        }
        return mDelegate.get();
    }
    public void setDelegate(Delegate delegate) {
        this.mDelegate = new WeakReference<Delegate>(delegate);
    }


    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = WeTubeApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getMessages().get(index));
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getMessages().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mMessageIn;
        private TextView mMessageOut;
        private ImageView thumbnailIn;
        private ImageView thumbnailOut;

        private Resources mResources;
        private MessageItem mMessageItem;
        private String mMsgSplitter;
        private String mTitle;
        private String mThumbnail;
        private String mId;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            mResources = WeTubeApplication.getSharedInstance().getResources();
            mMsgSplitter = mResources.getString(R.string.message_splitter);

            mMessageIn = (TextView) itemView.findViewById(R.id.message_item_message_incoming);
            mMessageOut = (TextView) itemView.findViewById(R.id.message_item_message_outgoing);
            thumbnailIn = (ImageView) itemView.findViewById(R.id.message_item_thumbnail_incoming);
            thumbnailOut = (ImageView) itemView.findViewById(R.id.message_item_thumbnail_outgoing);

            itemView.setOnClickListener(this);
        }

        void update(MessageItem messageItem) {
            this.mMessageItem = messageItem;
            String message = messageItem.getMessage();

            if(message.startsWith(mMsgSplitter + mResources.getString(R.string.linked_video))){
                handleVideoMessages(messageItem, message);
            }else{
                handleNonVideoMessages(messageItem);
            }
        }

        public void handleNonVideoMessages(MessageItem messageItem) {
            if(messageItem.getType() == MessageItem.OUTGOING_MSG){
                thumbnailIn.setVisibility(View.GONE);
                thumbnailOut.setVisibility(View.GONE);
                mMessageIn.setVisibility(View.INVISIBLE);
                mMessageOut.setVisibility(View.VISIBLE);
                mMessageOut.setText(messageItem.getMessage());
            }else{
                thumbnailIn.setVisibility(View.GONE);
                thumbnailOut.setVisibility(View.GONE);
                mMessageOut.setVisibility(View.INVISIBLE);
                mMessageIn.setVisibility(View.VISIBLE);
                mMessageIn.setText(messageItem.getMessage());
            }
        }

        public void handleVideoMessages(MessageItem messageItem, String message) {
            ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(message.split(mMsgSplitter)));
            mTitle = msgSplit.get(2);
            mThumbnail = msgSplit.get(3);
            mId = msgSplit.get(4);

            if(messageItem.getType() == MessageItem.OUTGOING_MSG){
                thumbnailIn.setVisibility(View.GONE);
                mMessageIn.setVisibility(View.INVISIBLE);

                thumbnailOut.setVisibility(View.VISIBLE);
                mMessageOut.setVisibility(View.VISIBLE);
                mMessageOut.setText(mTitle);
                Picasso.with(WeTubeApplication.getSharedInstance())
                        .load(mThumbnail)
                        .into(thumbnailOut);
            }else{
                thumbnailOut.setVisibility(View.GONE);
                mMessageOut.setVisibility(View.INVISIBLE);

                thumbnailIn.setVisibility(View.VISIBLE);
                mMessageIn.setVisibility(View.VISIBLE);
                mMessageIn.setText(mTitle);
                Picasso.with(WeTubeApplication.getSharedInstance())
                        .load(mThumbnail)
                        .into(thumbnailIn);
            }
        }

        @Override
        public void onClick(View view) {
            String message = mMessageItem.getMessage();
            if(message.startsWith(mMsgSplitter + mResources.getString(R.string.linked_video))){
                getDelegate().onMessageVideoItemClicked(MessageItemAdapter.this, mTitle, mThumbnail, mId);
            }
        }
    }
}


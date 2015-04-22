package com.gmail.markdevw.wetube.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.MessageItem;

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 4/7/2015.
 */
public class MessageItemAdapter extends RecyclerView.Adapter<MessageItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(MessageItemAdapter itemAdapter, MessageItem messageItemItem);
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

        TextView messageIn;
        TextView messageOut;
        MessageItem messageItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            messageIn = (TextView) itemView.findViewById(R.id.message_item_message_incoming);
            messageOut = (TextView) itemView.findViewById(R.id.message_item_message_outgoing);

            itemView.setOnClickListener(this);
        }

        void update(MessageItem messageItem) {
           this.messageItem = messageItem;

           if(messageItem.getType() == MessageItem.OUTGOING_MSG){
               messageIn.setVisibility(View.INVISIBLE);
               messageOut.setVisibility(View.VISIBLE);
               messageOut.setText(messageItem.getMessage());
           }else{
               messageOut.setVisibility(View.INVISIBLE);
               messageIn.setVisibility(View.VISIBLE);
               messageIn.setText(messageItem.getMessage());
           }
        }

        @Override
        public void onClick(View view) {
            ///////////////////getDelegate().onItemClicked(MessageItemAdapter.this, userItem);
        }
    }
}


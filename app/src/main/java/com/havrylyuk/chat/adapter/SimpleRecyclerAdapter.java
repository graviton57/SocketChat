package com.havrylyuk.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.havrylyuk.chat.activity.MainActivity;
import com.havrylyuk.chat.R;
import com.havrylyuk.chat.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 *
 * Created by Igor Havrylyuk on 15.03.2017.
 */
public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ItemHolder> {

    private static final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static int[] COLORS = {
            R.color.white,
            R.color.blue_500,
            R.color.green_500,
            R.color.orange_500,
            R.color.pink_500
    };

    private List<Message> list = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    @Override
    public SimpleRecyclerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ItemHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(SimpleRecyclerAdapter.ItemHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addAll(List<Message> messages) {
        list.clear();
        list.addAll(messages);
        notifyDataSetChanged();
    }

    public void append(Message event) {
        list.add(event);
        notifyItemInserted(getItemCount());
    }

    public void delete(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemHolder item, int position);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
         TextView messageTime;
         TextView userName;
         TextView userMessage;
         View parentLayout;

        private SimpleRecyclerAdapter adapter;

        public ItemHolder(View itemView, SimpleRecyclerAdapter parent) {
            super(itemView);
            messageTime = (TextView) itemView.findViewById(R.id.massage_time);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userMessage = (TextView) itemView.findViewById(R.id.user_message);
            parentLayout =  itemView.findViewById(R.id.parentLayout);
            itemView.setOnClickListener(this);
            this.adapter = parent;
        }

        public void bind(Message item) {
            int index = 0;
            if (item.getUserName().equals(MainActivity.USER_NAME)) {
                index = 1;
            }
            parentLayout.setBackgroundResource(COLORS[index]);
            messageTime.setText(formatTime.format(item.getDate()));
            userName.setText(item.getUserName());
            userMessage.setText(item.getMessage());
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }
        }
    }
}
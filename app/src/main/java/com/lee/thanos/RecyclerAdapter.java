package com.lee.thanos;

import android.app.Activity;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_STUB = 1;

    private Activity mActivity;
    private ArrayList<ItemBean> mData;

    RecyclerAdapter(Activity activity) {
        mActivity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_STUB) {
            View stub = LayoutInflater.from(mActivity).inflate(R.layout.layout_item_stub, viewGroup, false);
            return new ItemStubViewHolder(stub);
        }
        View view = LayoutInflater.from(mActivity).inflate(R.layout.layout_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == 0) {
            ItemViewHolder itemViewHolder = ((ItemViewHolder) holder);
            itemViewHolder.title.setText(mData.get(position).title);
            itemViewHolder.button.setText(mData.get(position).buttonContent);
            itemViewHolder.description.setText(mData.get(position).description);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).type == 1) {
            return TYPE_STUB;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<ItemBean> list) {
        mData = list;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {


        public final TextView title;
        public final TextView button;
        public final TextView description;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView);
            button = itemView.findViewById(R.id.button);
            description = itemView.findViewById(R.id.textView2);
        }
    }

    class ItemStubViewHolder extends RecyclerView.ViewHolder {


        public final ImageView image1;
        public final ImageView image2;

        ItemStubViewHolder(@NonNull View itemView) {
            super(itemView);
            image1 = itemView.findViewById(R.id.image1);
            image2 = itemView.findViewById(R.id.image2);
        }
    }


}

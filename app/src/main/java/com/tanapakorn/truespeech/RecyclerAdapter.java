package com.tanapakorn.truespeech;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanapakorn.truespeech.viewholder.MyTextViewHolder;
import com.tanapakorn.truespeech.viewholder.YourTextViewHolder;

import java.util.List;
import java.util.zip.Inflater;

class RecyclerAdapter extends RecyclerView.Adapter<ChatViewHolder>  {

    private List<ChatContract.ITextViewModel> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    public RecyclerAdapter(Context pContext, List<ChatContract.ITextViewModel> pData) {
        this.mContext = pContext;
        this.mData = pData;

        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch(viewType){
            case ChatContract.VIEW_TYPE_MY_TEXT:
                view = mInflater.inflate(R.layout.item_my_chat_box, parent, false);
                return new MyTextViewHolder(view);
            case ChatContract.VIEW_TYPE_YOUR_TEXT:
                view = mInflater.inflate(R.layout.item_your_chat_box, parent, false);
                return new YourTextViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getViewType();
    }

    public void add(ChatContract.ITextViewModel pModel){
        mData.add(pModel);
    }
}

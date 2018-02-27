package com.tanapakorn.truespeech;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatViewHolder extends RecyclerView.ViewHolder{

    private TextView mTextView;

    public ChatViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.tvChatBox);
    }

    public void bind(ChatContract.ITextViewModel pData){
        mTextView.setText(pData.text());
    }
}

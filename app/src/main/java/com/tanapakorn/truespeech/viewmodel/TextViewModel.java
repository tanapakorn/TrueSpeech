package com.tanapakorn.truespeech.viewmodel;

import com.tanapakorn.truespeech.ChatContract;


public class TextViewModel implements ChatContract.ITextViewModel {
    private final int mViewType;
    private String mText;

    public TextViewModel(int pViewType) {
        this.mViewType = pViewType;
    }

    @Override
    public String text() {
        return mText;
    }

    public TextViewModel text(String pText) {
        mText = pText;
        return this;
    }

    @Override
    public int getViewType() {
        return mViewType;
    }
}

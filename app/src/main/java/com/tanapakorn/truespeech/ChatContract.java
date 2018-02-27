package com.tanapakorn.truespeech;


public class ChatContract {

    public static final int VIEW_TYPE_MY_TEXT = 0;
    public static final int VIEW_TYPE_YOUR_TEXT = VIEW_TYPE_MY_TEXT +1;

    public interface ITextViewModel{
        String text();
        int getViewType();
    }

}

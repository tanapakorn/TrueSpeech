package com.tanapakorn.truespeech;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tanapakorn.speechlibrary.SpeechManager;
import com.tanapakorn.truespeech.viewmodel.TextViewModel;

import java.util.ArrayList;
import java.util.List;


public class WebViewActivity extends AppCompatActivity{

    private String TAG = "MainActivity";

    private SpeechManager mManager;

    private WebView mWebView;
    private ImageButton mMicButton;

    private final SpeechManager.ServiceListener mSpeechServiceListener = new SpeechManager.ServiceListener() {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal) {
            if(!isFinal){
                return;
            }
            if (!TextUtils.isEmpty(text)) {
            }
        }
    };

    private final View.OnClickListener turnOffMicAction = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(mManager == null) return;

            mManager.stopVoiceRecorder();
            mMicButton.setImageResource(R.drawable.ic_mic_black_24dp);
            mMicButton.setOnClickListener(turnOnMicAction);
        }
    };

    private final View.OnClickListener turnOnMicAction = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(mManager == null) return;

            mManager.startVoiceRecorder();
            mMicButton.setImageResource(R.drawable.ic_mic_none_black_24dp);
            mMicButton.setOnClickListener(turnOffMicAction);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_webview);

        mWebView = (WebView)findViewById(R.id.webview);
        mMicButton = (ImageButton)findViewById(R.id.micButton);
        mMicButton.setOnClickListener(turnOnMicAction);

        mManager = new SpeechManager(this, R.raw.credential);
        mManager.echo(true);
        mManager.addSpeechServiceListener( mSpeechServiceListener);

        mWebView.loadUrl("file:///android_asset/html/chatBot.html");
        mWebView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.startService();
    }

    @Override
    protected void onStop() {
        mManager.stopService();
        super.onStop();
    }
}

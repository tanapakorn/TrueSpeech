package com.tanapakorn.truespeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tanapakorn.speechlibrary.SpeechManager;
import com.tanapakorn.truespeech.viewmodel.TextViewModel;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // View references
    private RecyclerView mRvChatHistory;
    private TextView mTextResult;

    private SpeechManager mManager;

    private ImageButton mMicBtn;
    private ImageButton mMicCloseBtn;

    private Button audioFileBtn;

    private EditText mEditText;
    private RecyclerAdapter rvAdapter;

    private final SpeechManager.ServiceListener mSpeechServiceListener = new SpeechManager.ServiceListener() {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal) {
            if(!isFinal){
                return;
            }
            if (mTextResult != null && !TextUtils.isEmpty(text)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rvAdapter != null){
                            rvAdapter.add(new TextViewModel(ChatContract.VIEW_TYPE_MY_TEXT).text(text));
                        }
                        mTextResult.setText(text);
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRvChatHistory = (RecyclerView)findViewById(R.id.rvChatHistory);
        audioFileBtn = (Button)findViewById(R.id.audio_file);
        mTextResult = (TextView)findViewById(R.id.result);
        mEditText = (EditText)findViewById(R.id.edit_text);
        mMicBtn = (ImageButton)findViewById(R.id.push_to_talk_button);
        mMicCloseBtn = (ImageButton)findViewById(R.id.push_to_talk_end_button);

        mMicBtn.setVisibility(View.VISIBLE);
        mMicCloseBtn.setVisibility(View.GONE);

        audioFileBtn.setOnClickListener(this);
        mMicBtn.setOnClickListener(this);
        mMicCloseBtn.setOnClickListener(this);

        rvAdapter = new RecyclerAdapter(this, generateMockup());
        mRvChatHistory.setLayoutManager( new LinearLayoutManager(this));
        mRvChatHistory.setAdapter(rvAdapter);

        mManager = new SpeechManager(this, R.raw.credential);
        mManager.addSpeechServiceListener( mSpeechServiceListener);
        mManager.addStatusListener(new SpeechManager.StatusListener() {
            @Override
            public void onStatusChanged(int pStatus) {
                switch(pStatus){
                    case SpeechManager.STATE_IDLE:
                        mEditText.setText(R.string.string_state_idle);
                        break;
                    case SpeechManager.STATE_RECORDING:
                        mEditText.setText(R.string.string_listening);
                        break;
                    default:
                        mEditText.setText("");
                        break;
                }
            }
        });
    }

    private List<ChatContract.ITextViewModel> generateMockup() {
        List<ChatContract.ITextViewModel> list = new ArrayList<>();
        list.add( new TextViewModel(ChatContract.VIEW_TYPE_MY_TEXT).text("what"));
        list.add( new TextViewModel(ChatContract.VIEW_TYPE_YOUR_TEXT).text("the"));

        return list;
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

    @Override
    public void onClick(View v) {
        if(mManager == null) {
            return;
        }
        switch(v.getId()){
            case R.id.audio_file:
                mManager.recognizeInputStream(getResources().openRawResource(R.raw.audio));
                break;
            case R.id.push_to_talk_button:
                mMicBtn.setVisibility(View.GONE);
                mMicCloseBtn.setVisibility(View.VISIBLE);
                mManager.startVoiceRecorder();
                break;
            case R.id.push_to_talk_end_button:
                mMicBtn.setVisibility(View.VISIBLE);
                mMicCloseBtn.setVisibility(View.GONE);
                mManager.stopVoiceRecorder();
                break;
        }
    }
}

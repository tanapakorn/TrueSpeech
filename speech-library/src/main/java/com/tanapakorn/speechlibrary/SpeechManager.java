package com.tanapakorn.speechlibrary;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

public class SpeechManager {

    public interface ServiceListener extends SpeechService.Listener{
        @Override
        void onSpeechRecognized(String text, boolean isFinal);
    }

    public interface StatusListener{
        void onStatusChanged(int pStatus);
    }

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    public static final int STATE_IDLE = 0;
    public static final int STATE_RECORDING = STATE_IDLE + 1;
    public static final int STATE_WAITING = STATE_RECORDING + 1;
    public static final int STATE_COMPLETE = STATE_WAITING + 1;

    private Context mContext;

    private int mCredentialResourceId;

    private TTSManager mTts;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;

    private List<SpeechService.Listener> mSpeechServiceListenerList;

    private List<StatusListener> mStatusListenerList;

    private int mState;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            for(SpeechService.Listener listener : mSpeechServiceListenerList){
                mSpeechService.addListener(listener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }
    };

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {
        @Override
        public void onVoiceStart() {
            if (mSpeechService != null && !mTts.isSpeaking()) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }
    };

    private TextToSpeech.OnInitListener mTtsListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status != TextToSpeech.SUCCESS){
                return;
            }
            Locale langTH = new Locale("th", "TH");
            switch(mTts.isLanguageAvailable(langTH)){
                case TextToSpeech.LANG_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                    mTts.setLanguage(langTH);
                    break;
                default:
                    mTts.setLanguage(Locale.US);
                    break;
            }
        }
    };

    private final SpeechService.Listener mSpeechServiceListener = new SpeechService.Listener() {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal) {
            if (isFinal) {
                mTts.speak(text, TextToSpeech.QUEUE_ADD, null);

                if(mVoiceRecorder != null) {
                    mVoiceRecorder.dismiss();
                }
            }
        }
    };

    public SpeechManager(@Nonnull Context pContext, @Nonnull int pResourceId) {
        this.mContext = pContext;
        this.mCredentialResourceId = pResourceId;

        mSpeechServiceListenerList = new ArrayList<>();
        mStatusListenerList = new ArrayList<>();

        mTts = new TTSManager(mContext, mTtsListener);

        mState = STATE_IDLE;
    }

    public boolean isPermissionGranted() {
        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
            //TODO permission dismiss
        } else {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
        return false;
    }

    public void startService() {
        if(isPermissionGranted()){
            // Prepare Cloud Speech API
            Intent intent = new Intent(mContext, SpeechService.class);
            intent.putExtra(SpeechService.GOOGLE_CREDENTIAL, mCredentialResourceId);

            mContext.bindService(
                    intent
                    , mServiceConnection
                    , Context.BIND_AUTO_CREATE);

            addSpeechServiceListener( mSpeechServiceListener);
        }
    }

    public void stopService() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        for(SpeechService.Listener listener : mSpeechServiceListenerList){
            mSpeechService.removeListener(listener);
        }
        mContext.unbindService(mServiceConnection);
        mSpeechServiceListenerList = new ArrayList<>();
        mSpeechService = null;
    }

    public void startVoiceRecorder(){
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();

        onStatusChanged(STATE_RECORDING);
    }

    public void stopVoiceRecorder(){
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
        onStatusChanged(STATE_IDLE);
    }

    public void recognizeInputStream(InputStream pInputStream) {
        if(mSpeechService != null && pInputStream != null){
            mSpeechService.recognizeInputStream(pInputStream);
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(mContext.getString(R.string.permission_message))
                .show(((AppCompatActivity)mContext).getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    public void addSpeechServiceListener(SpeechService.Listener pListener){
        if(mSpeechService != null){
            mSpeechService.addListener(pListener);
            return;
        }

        mSpeechServiceListenerList.add(pListener);
    }

    public void addStatusListener(StatusListener pListener) {
        mStatusListenerList.add(pListener);
    }

    private void onStatusChanged(int pStatus){
        for(StatusListener listener : mStatusListenerList){
            listener.onStatusChanged(pStatus);
        }
    }

    public TTSManager getTextToSpeech(){
        return mTts;
    }

    public void echo(boolean isEnable){
        mTts.setEnable(isEnable);
    }

    public boolean echo(){
        return mTts.isEnable();
    }

    private class TTSManager extends TextToSpeech{
        private Context mContext;
        private OnInitListener mOnInitListener;

        private boolean enable;

        public TTSManager(Context context, OnInitListener listener) {
            super(context, listener);
            this.mContext = context;
            this.mOnInitListener = listener;

            enable = true;
        }

        @Override
        public int speak(CharSequence text, int queueMode, Bundle params, String utteranceId) {
            if(!enable){
                return super.speak( null, queueMode, params, utteranceId);
            }
            return super.speak(text, queueMode, params, utteranceId);
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean isEnable() {
            return enable;
        }
    }
}

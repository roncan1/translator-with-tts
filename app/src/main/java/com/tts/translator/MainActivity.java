package com.tts.translator;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btn_language, btn_translation, btn_share, btn_copy, btn_tts;
    EditText ET_input;
    TextView TV_result, TV_language1, TV_language2;
    TextToSpeech tts;
    int language = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        translation();
        share();
        copy();
        changeLanguage();
        readText();

    }

    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            TV_result.setText(resultWord);
        }
    };

    void copy() {
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("translator", TV_result.getText().toString());
                clipboardManager.setPrimaryClip(clipData);

                //복사가 되었다면 토스트메시지 노출
                Toast.makeText(getApplicationContext(), "복사되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void share() {
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType(TV_result.getText().toString());

                String Test_Message = TV_result.getText().toString();

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                startActivity(Sharing);
            }
        });

    }

    public void changeLanguage() {
        btn_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (language == 0) {
                    TV_language1.setText("영어");
                    TV_language2.setText("한국어");
                    language = 1;
                } else {
                    TV_language1.setText("한국어");
                    TV_language2.setText("영어");
                    language = 0;
                }
            }
        });
    }

    void readText() {
        btn_tts.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override public void onClick(View v) {
                String text = TV_result.getText().toString();

                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    void init() {
        btn_language = (Button) findViewById(R.id.btn_language);
        btn_translation = (Button) findViewById(R.id.btn_translation);
        btn_copy = (Button) findViewById(R.id.btn_copy);
        btn_share = (Button) findViewById(R.id.btn_share);
        ET_input = (EditText) findViewById(R.id.ET_original);
        TV_result = (TextView) findViewById(R.id.TV_translation);
        TV_language1 = (TextView) findViewById(R.id.TV_language1);
        TV_language2 = (TextView) findViewById(R.id.TV_language2);
        btn_tts = (Button) findViewById(R.id.btn_tts);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
    }

    void translation() {

        btn_translation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        String word = ET_input.getText().toString();
                        // Papago는 3번에서 만든 자바 코드이다.
                        SetPapago papago = new SetPapago();
                        String resultWord;

                        if(language == 0){
                            resultWord= papago.getTranslation(word,"ko","en");
                        }else{
                            resultWord= papago.getTranslation(word,"en","ko");
                        }

                        Bundle papagoBundle = new Bundle();
                        papagoBundle.putString("resultWord",resultWord);


                        Message msg = papago_handler.obtainMessage();
                        msg.setData(papagoBundle);
                        papago_handler.sendMessage(msg);

                    }
                }.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }
}
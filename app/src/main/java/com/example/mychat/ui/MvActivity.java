package com.example.mychat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.framework.base.BaseUIActivity;
import com.example.framework.base.CustomVideoView;
import com.example.mychat.R;

/**
 * 进入首页前的mv界面
 */
public class MvActivity extends BaseUIActivity implements View.OnClickListener{
    private CustomVideoView videoView;
    private TextView btn_start;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mv);
        initView();
    }
    public void initView(){
        btn_start = (TextView) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        videoView = (CustomVideoView) findViewById (R.id.bg_mv);

        intent=new Intent();
        intent.setClass(this, LoginActivity.class);

        playVideo();
    }
    public void playVideo(){

        String uri = "android.resource://" + getPackageName() + "/" + R.raw.bg_mv;
        videoView.setVideoURI(Uri.parse(uri));

        MediaController mc = new MediaController(this);
        //设置控制器 控制的是那一个videoview
       // mc.setAnchorView(videoView);
        //设置videoview的控制器为mc
        mc.setVisibility(View.GONE);
        videoView.setMediaController(mc);
        videoView.requestFocus();
        videoView.start();
        //循环

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                startActivity(intent);
                finish();
            }
        });


}


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start:

                Log.d("111111111111111","直接进入");
                startActivity(intent);
                finish();

                break;
        }
    }



}

package com.example.mychat.ui;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.example.framework.bmob.BmobManager;
import com.example.framework.entity.Constants;
import com.example.framework.utils.SpUtils;
import com.example.mychat.MainActivity;
import com.example.mychat.R;

/**
 * 启动页
 */


public class IndexActivity extends AppCompatActivity {

    /**
     * 1.启动页全屏
     * 2.延迟进入主页
     * 3.
     * @param savedInstanceState
     */
    private static  final int SKIP_MAIN=1000;
    private static  final String SHARE_APP_TAG="SHARE_APP_TAG";//第一次启动标志
    private  static  final  String SHARE_APP_LOGIN="SHARE_APP_LOGIN";//是否登录过;

    private Handler mHandler =new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case SKIP_MAIN:
                    startMain();
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_index);
        mHandler.sendEmptyMessageDelayed(SKIP_MAIN,2*1000);
    }

    /**
     * 进入主页
     */
    private void startMain(){
        SpUtils.getInstance().initSp(this);
        boolean isFirst= SpUtils.getInstance().getBoolean(Constants.SP_IS_FIRST_APP,true);

        Intent intent=new Intent();

        intent.setClass(this, MainActivity.class);
       // startActivity(intent);
        //finish();

        if(isFirst){
            //第一次启动
            intent.setClass(this,MvActivity.class);
            SpUtils.getInstance().putBoolean(Constants.SP_IS_FIRST_APP,false);
        }else {
           String token= SpUtils.getInstance().getString(Constants.SP_TOKEN,"");
           if(TextUtils.isEmpty(token)){
               //判断bmob是否登录
               if (BmobManager.getInstance().isLogin()) {
                   //跳转到主页
                   intent.setClass(this, MainActivity.class);
               } else {
                   //跳转到登录页
                   intent.setClass(this, LoginActivity.class);
               }

           }else {
               //进入到主页
               intent.setClass(this, MainActivity.class);
           }
        }
        startActivity(intent);
        finish();
    }
}

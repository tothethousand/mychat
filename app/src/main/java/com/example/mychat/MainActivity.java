package com.example.mychat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.framework.base.BaseUIActivity;
import com.example.framework.bmob.BmobManager;
import com.example.framework.bmob.IMUser;
import com.example.framework.cloud.CloudManager;
import com.example.framework.entity.Constants;
import com.example.framework.event.EventManager;
import com.example.framework.event.MessageEvent;
import com.example.framework.gson.TokenBean;
import com.example.framework.manager.DialogManager;
import com.example.framework.manager.HttpManager;
import com.example.framework.manager.MediaPlayerManager;
import com.example.framework.utils.SpUtils;
import com.example.framework.utils.TimeUtils;
import com.example.framework.view.DialogView;
import com.example.mychat.fragment.ChatFragment;
import com.example.mychat.fragment.MeFragment;
import com.example.mychat.fragment.SquareFragment;
import com.example.mychat.fragment.StarFragment;
import com.example.mychat.service.CloudService;
import com.example.mychat.ui.FirstUploadActivity;
import com.example.mychat.ui.LoginActivity;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 主Acitivity
 */

public class MainActivity extends BaseUIActivity implements View.OnClickListener {

    //媒体类
    public MediaPlayer mMediaPlayer;
    //首页
    private ImageView iv_star;
    private TextView tv_star;
    private LinearLayout ll_star;
    private StarFragment mStarFragment = null;
    private FragmentTransaction mStarTransaction = null;

    //广场
    private ImageView iv_square;
    private TextView tv_square;
    private LinearLayout ll_square;
    private SquareFragment mSquareFragment = null;
    private FragmentTransaction mSquareTransaction = null;

    //聊天
    private ImageView iv_chat;
    private TextView tv_chat;
    private LinearLayout ll_chat;
    private ChatFragment mChatFragment = null;
    private FragmentTransaction mChatTransaction = null;

    //我的
    private ImageView iv_me;
    private TextView tv_me;
    private LinearLayout ll_me;
    private MeFragment mMeFragment = null;
    private FragmentTransaction mMeTransaction = null;

    private DialogView mUploadView;
    private Disposable disposable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        //获取权限
        requestPermiss();

        iv_star = (ImageView) findViewById(R.id.iv_star);
        tv_star = (TextView) findViewById(R.id.tv_star);
        ll_star = (LinearLayout) findViewById(R.id.ll_star);

        iv_square = (ImageView) findViewById(R.id.iv_square);
        tv_square = (TextView) findViewById(R.id.tv_square);
        ll_square = (LinearLayout) findViewById(R.id.ll_square);

        iv_chat = (ImageView) findViewById(R.id.iv_chat);
        tv_chat = (TextView) findViewById(R.id.tv_chat);
        ll_chat = (LinearLayout) findViewById(R.id.ll_chat);

        iv_me = (ImageView) findViewById(R.id.iv_me);
        tv_me = (TextView) findViewById(R.id.tv_me);
        ll_me = (LinearLayout) findViewById(R.id.ll_me);

        ll_star.setOnClickListener(this);
        ll_square.setOnClickListener(this);
        ll_chat.setOnClickListener(this);
        ll_me.setOnClickListener(this);

        //初始化各个组件
        initFragment();
        //默认选项
        checkMainTab(0);

        //检查TOKEN
        checkToken();

    }

    /**
     * 检查TOKEN
     */
    private void checkToken() {
        if (mUploadView != null) {
            DialogManager.getInstance().hide(mUploadView);
        }
        //获取TOKEN 需要三个参数 1.用户ID 2.头像地址 3.昵称


        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
       // createUploadDialog();

        if (!TextUtils.isEmpty(token)) {
            startCloudService();
        } else {
            //1.有三个参数
            String tokenPhoto = BmobManager.getInstance().getUser().getTokenPhoto();
            String tokenName = BmobManager.getInstance().getUser().getTokenNickName();

            if (!TextUtils.isEmpty(tokenPhoto) && !TextUtils.isEmpty(tokenName)) {
                //创建Token
                createToken();
            } else {
                //创建上传提示框
                createUploadDialog();
            }
        }
    }



    /**
     * 创建上传提示框
     */
    private void createUploadDialog() {
        mUploadView = DialogManager.getInstance().initView(this, R.layout.dialog_first_upload);
        //外部点击不能消息
        mUploadView.setCancelable(false);
        ImageView iv_go_upload = mUploadView.findViewById(R.id.iv_go_upload);
        iv_go_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("qwe","1");
                FirstUploadActivity.startActivity(MainActivity.this);
            }
        });
        DialogManager.getInstance().show(mUploadView);
    }

    /**
     * 创建TOKEN 连接融云
     */
    private void createToken() {
       // LogUtils.i("createToken");
        /**
         * 1.去融云后台获取Token
         * 2.连接融云
//         */
        final HashMap<String, String> map = new HashMap<>();
        map.put("userId", BmobManager.getInstance().getUser().getObjectId());
      //  map.put("userId","001");
        map.put("name", BmobManager.getInstance().getUser().getTokenNickName());
        map.put("portraitUri", BmobManager.getInstance().getUser().getTokenPhoto());
//        //通过OkHttp请求Token 异步去请求
       disposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                //执行请求过程
                String json = HttpManager.getInstance().postCloudToken(map);
               // LogUtils.i("json:" + json);
                emitter.onNext(json);
                emitter.onComplete();
            }
            //线程调度
        }).subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                     //   Log.d("qwe",s.toString());
                       parsingCloudToken(s);
                    }
                });

    }

    /**
     * 解析Token
     *
     * @param s
     */
    private void parsingCloudToken(String s) {
        try {
          //  LogUtils.i("parsingCloudToken:" + s);
            TokenBean tokenBean = new Gson().fromJson(s, TokenBean.class);
            if (tokenBean.getCode() == 200) {
                if (!TextUtils.isEmpty(tokenBean.getToken())) {
                    //保存Token
                    Log.d("token",tokenBean.getToken());
                    SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.getToken());
                    startCloudService();
                }
            } else if (tokenBean.getCode() == 2007) {
                Toast.makeText(this, "注册人数已达上限", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
           // LogUtils.i("parsingCloudToken:" + e.toString());
        }
    }

    /**
     * 启动云服务去连接融云服务
     */
    private void startCloudService() {
        //LogUtils.i("startCloudService");
        Log.d("ryfw","33");
        startService(new Intent(MainActivity.this, CloudService.class));

        //检查更新
      //  new UpdateHelper(this).updateApp(null);
    }

    private void initFragment() {
        //星球
        if (mStarFragment == null) {
            mStarFragment = new StarFragment();
            mStarTransaction = getSupportFragmentManager().beginTransaction();
            mStarTransaction.add(R.id.mMainLayout, mStarFragment);
            mStarTransaction.commit();
        }

        //广场
        if (mSquareFragment == null) {
            mSquareFragment = new SquareFragment();
            mSquareTransaction = getSupportFragmentManager().beginTransaction();
            mSquareTransaction.add(R.id.mMainLayout, mSquareFragment);
            mSquareTransaction.commit();
        }

        //聊天
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            mChatTransaction = getSupportFragmentManager().beginTransaction();
            mChatTransaction.add(R.id.mMainLayout, mChatFragment);
            mChatTransaction.commit();
        }

        //我的
        if (mMeFragment == null) {
            mMeFragment = new MeFragment();
            mMeTransaction = getSupportFragmentManager().beginTransaction();
            mMeTransaction.add(R.id.mMainLayout, mMeFragment);
            mMeTransaction.commit();
        }
    }

    /**
     * 切换主页选项卡
     *
     * @param index 0：星球
     *              1：广场
     *              2：聊天
     *              3：我的
     */
    private void checkMainTab(int index) {
        switch (index) {
            case 0:
                showFragment(mStarFragment);

                iv_star.setImageResource(R.drawable.img_star_p);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);

                break;
            case 1:
                showFragment(mSquareFragment);

                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square_p);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);

                break;
            case 2:
                showFragment(mChatFragment);

                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat_p);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_me.setTextColor(Color.BLACK);

                break;
            case 3:
                showFragment(mMeFragment);

                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me_p);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(getResources().getColor(R.color.colorAccent));

                break;
        }
    }
    /**
     * 防止重叠
     * 当应用的内存紧张的时候，系统会回收掉Fragment对象
     * 再一次进入的时候会重新创建Fragment
     * 非原来对象，我们无法控制，导致重叠
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        if (mStarFragment != null && fragment instanceof StarFragment) {
            mStarFragment = (StarFragment) fragment;
        }
        if (mSquareFragment != null && fragment instanceof SquareFragment) {
            mSquareFragment = (SquareFragment) fragment;
        }
        if (mChatFragment != null && fragment instanceof ChatFragment) {
            mChatFragment = (ChatFragment) fragment;
        }
        if (mMeFragment != null && fragment instanceof MeFragment) {
            mMeFragment = (MeFragment) fragment;
        }
    }

    /**
     * 隐藏所有的Fragment
     *
     * @param transaction
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if (mStarFragment != null) {
            transaction.hide(mStarFragment);
        }
        if (mSquareFragment != null) {
            transaction.hide(mSquareFragment);
        }
        if (mChatFragment != null) {
            transaction.hide(mChatFragment);
        }
        if (mMeFragment != null) {
            transaction.hide(mMeFragment);
        }
    }

    /**
     * 显示Fragment
     *
     * @param fragment
     */
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }
    //点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_star:
                checkMainTab(0);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;

        }
    }
    //获取权限的回调函数
    private void requestPermiss(){
        request(1000, new OnPermissionsResult() {
            @Override
            public void OnSuccess() {
               // Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFail(List<String> noPermissions) {
              //  Toast.makeText(MainActivity.this,"err",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode== Activity.RESULT_OK){

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //拿到token 消息回传 隐藏填写信息页面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_REFRE_TOKEN_STATUS:
                checkToken();
                break;
        }
    }


    //token拿到后，销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }
}



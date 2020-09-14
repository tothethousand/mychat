package com.example.mychat.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.framework.base.BaseUIActivity;
import com.example.framework.base.CustomVideoView;
import com.example.framework.bmob.BmobManager;
import com.example.framework.bmob.IMUser;
import com.example.framework.entity.Constants;
import com.example.framework.utils.SpUtils;
import com.example.mychat.MainActivity;
import com.example.mychat.R;
import com.example.mychat.adapter.UserBeanAdapter;
import com.example.mychat.bean.UserBean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * 登录页面
 */
public class LoginActivity extends BaseUIActivity implements View.OnClickListener {

    private CustomVideoView videoView;

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mAccountView;
    private EditText mPasswordView;
    private ImageView mClearAccountView;
    private ImageView mClearPasswordView;
    private CheckBox mEyeView;
    private CheckBox mDropDownView;
    private Button mLoginView;
    private TextView mForgetPsdView;
    private TextView mRegisterView;
    private LinearLayout mTermsLayout;
    private TextView mTermsView;
    private RelativeLayout mPasswordLayout;

    private List<View> mDropDownInvisibleViews;


    /**
     * 倒计时
     */
    private static final int H_TIME = 1001;
    //60s倒计时
    private static int TIME = 60;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case H_TIME:
                    TIME--;
                    mRegisterView.setText(TIME + "s");
                    if (TIME > 0) {
                        mHandler.sendEmptyMessageDelayed(H_TIME, 1000);
                    } else {
                        mRegisterView.setEnabled(true);
                        mRegisterView.setText(getString(R.string.login_register));
                    }
                    break;
            }
            return false;
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        playVideo();
        findViewId();
        initDropDownGroup();

        mPasswordView.setLetterSpacing(0.2f);

        mClearAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountView.setText("");
            }
        });

        mClearPasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordView.setText("");
            }
        });

        mEyeView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        mAccountView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //当账号栏正在输入状态时，密码栏的清除按钮和眼睛按钮都隐藏
                if(hasFocus){
                    mClearPasswordView.setVisibility(View.INVISIBLE);
                    mEyeView.setVisibility(View.INVISIBLE);
                }else {
                    mClearPasswordView.setVisibility(View.VISIBLE);
                    mEyeView.setVisibility(View.VISIBLE);
                }
//                Log.i(TAG,"onFocusChange()::" + hasFocus);
            }
        });
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //当密码栏为正在输入状态时，账号栏的清除按钮隐藏
                if(hasFocus)
                    mClearAccountView.setVisibility(View.INVISIBLE);
                else mClearAccountView.setVisibility(View.VISIBLE);
            }
        });

        mDropDownView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //下拉按钮点击的时候，密码栏、忘记密码、新用户注册、同意服务条款先全部隐藏
                    setDropDownVisible(View.INVISIBLE);
                    //下拉箭头变为上拉箭头
                    //弹出一个popupWindow
                    showDropDownWindow();
                }else {
                    setDropDownVisible(View.VISIBLE);
                }
            }
        });
        mDropDownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mForgetPsdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFindPsdDialog();
            }
        });

        mTermsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入服务条款界面
            }
        });

        mRegisterView.setOnClickListener(this);
        mLoginView.setOnClickListener(this);
    }


    private void findViewId() {
        mAccountView = findViewById(R.id.et_input_account);
        mPasswordView = findViewById(R.id.et_input_password);
        mClearAccountView = findViewById(R.id.iv_clear_account);
        mClearPasswordView = findViewById(R.id.iv_clear_password);
        mEyeView = findViewById(R.id.iv_login_open_eye);
        mDropDownView = findViewById(R.id.cb_login_drop_down);
        mLoginView = findViewById(R.id.btn_login);
        mForgetPsdView = findViewById(R.id.tv_forget_password);
        mRegisterView = findViewById(R.id.tv_register_account);
        mTermsLayout = findViewById(R.id.ll_terms_of_service_layout);
        mTermsView = findViewById(R.id.tv_terms_of_service);
        mPasswordLayout = findViewById(R.id.rl_password_layout);
    }

    private void initDropDownGroup() {
        mDropDownInvisibleViews = new ArrayList<>();
        mDropDownInvisibleViews.add(mPasswordView);
        mDropDownInvisibleViews.add(mForgetPsdView);
        mDropDownInvisibleViews.add(mRegisterView);
        mDropDownInvisibleViews.add(mPasswordLayout);
        mDropDownInvisibleViews.add(mLoginView);
        mDropDownInvisibleViews.add(mTermsLayout);
    }


    private void setDropDownVisible(int visible) {
        for (View view:mDropDownInvisibleViews){
            view.setVisibility(visible);
        }
    }

    private void showDropDownWindow() {
        final PopupWindow window = new PopupWindow(this);
        //下拉菜单里显示上次登录的账号，在这里先模拟获取有记录的用户列表
        List<UserBean> userBeanList = new ArrayList<>();
        //显示最近使用的手机号
        String account= SpUtils.getInstance().getString(Constants.SP_PHONE,"");
        userBeanList.add(new UserBean(account,""));

        //userBeanList.add(new UserBean("22669988","22669988"));
        //配置ListView的适配器
        final UserBeanAdapter adapter = new UserBeanAdapter(this);
        adapter.replaceData(userBeanList);
        //初始化ListView
        ListView userListView = (ListView) View.inflate(this,
                R.layout.window_account_drop_down,null);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //当下拉列表条目被点击时，显示刚才被隐藏视图,下拉箭头变上拉箭头
                //相当于mDropDownView取消选中
                mDropDownView.setChecked(false);
                //账号栏和密码栏文本更新
                UserBean checkedUser = adapter.getItem(position);
                mAccountView.setText(checkedUser.getAccount());
                mPasswordView.setText(checkedUser.getPassword());
                //关闭popupWindow
                window.dismiss();
            }
        });
        //添加一个看不见的FooterView，这样ListView就会自己在倒数第一个（FooterView）上边显示Divider，
        //进而在UI上实现最后一行也显示分割线的效果了
        userListView.addFooterView(new TextView(this));

        //配置popupWindow并显示
        window.setContentView(userListView);
        window.setAnimationStyle(0);
        window.setBackgroundDrawable(null);
        window.setWidth(mPasswordLayout.getWidth());
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setOutsideTouchable(true);
        window.showAsDropDown(mAccountView);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //当点击popupWindow之外区域导致window关闭时，显示刚才被隐藏视图，下拉箭头变上拉箭头
                //相当于mDropDownView取消选中
                mDropDownView.setChecked(false);
            }
        });

    }

    private void showFindPsdDialog() {
        //有空了做下
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDropDownInvisibleViews.clear();
    }

    public void playVideo(){
        videoView = (CustomVideoView) findViewById (R.id.bg_mv);
        String uri = "android.resource://" + getPackageName() + "/" + R.raw.bg2_mv;
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

                videoView.start();
            }
        });


    }

    @Override
    public void onClick(View v){

        switch (v.getId()){
            case R.id.tv_register_account:

                sendSMS();
                break;
            case R.id.btn_login:
                login();
                break;

        }
    }
    //发送验证码
    private void sendSMS() {
        String phone= mAccountView.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"输入为空",Toast.LENGTH_SHORT).show();
            return;
        }
        //请求短信验证
        BmobManager.getInstance().requestSMS(phone, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if(e==null){
                    mRegisterView.setEnabled(false);
                    mHandler.sendEmptyMessage(H_TIME);
                    Toast.makeText(LoginActivity.this,"短信验证码发送成功",Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(LoginActivity.this,"短信验证码发送失败",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    //登录
    private void login() {

        //1.判断手机号码和验证码不为空
        final String phone = mAccountView.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this,"账号不能为空",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String code = mPasswordView.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this,"密码不能为空",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mLoginView.setText("正在验证中，请稍后");
        mLoginView.setEnabled(false);


        BmobManager.getInstance().signOrLoginByMobilePhone(phone, code, new LogInListener<IMUser>() {
            @Override
            public void done(IMUser imUser, BmobException e) {

               // mLodingView.hide();
                if (e == null) {
                    //登陆成功
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    //把手机号码保存下来
                    SpUtils.getInstance().putString(Constants.SP_PHONE, phone);
                    finish();

                } else {
                    Log.d("err","执行到err");
                    Log.d("err",e.toString());
                        Toast.makeText(LoginActivity.this,"登录失败，请重新输入", Toast.LENGTH_SHORT).show();
                    mLoginView.setText("登录");
                    mLoginView.setEnabled(true);

                }
            }
        });
    }
}

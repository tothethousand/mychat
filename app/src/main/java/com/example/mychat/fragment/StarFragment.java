package com.example.mychat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.framework.base.BaseFragment;
import com.example.framework.bmob.BmobManager;
import com.example.framework.bmob.IMUser;
import com.example.framework.cloud.CloudManager;
import com.example.framework.event.EventManager;
import com.example.framework.event.MessageEvent;
import com.example.framework.helper.PairFriendHelper;
import com.example.framework.manager.DialogManager;
import com.example.framework.utils.CommonUtils;
import com.example.framework.view.DialogView;
import com.example.framework.view.LodingView;
import com.example.mychat.R;
//import com.example.mychat.fragment.xuanwoanim.MainActivity;
import com.example.mychat.fragment.xuanwoanim.RotateCircleView;
import com.example.mychat.fragment.xuanwoanim.ScreenUtil;
import com.example.mychat.ui.AddFriendActivity;
import com.example.mychat.ui.QrCodeActivity;
import com.example.mychat.ui.UserInfoActivity;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Profile: 首页
 */
public class StarFragment extends BaseFragment implements View.OnClickListener {


   //漩涡
    private ImageView mIvFlower1;
    private ImageView mIvFlower2;
    private RotateCircleView mRotateView;
    private Context mContext;
    private boolean isStopAnim = false;
    private List<String> imagePathList = new ArrayList<>();
    private Handler mHandler = new Handler();
    private RelativeLayout mRlAnim;

    //二维码结果
    private static final int REQUEST_CODE = 1235;

    private TextView tv_star_title;
    private ImageView iv_camera;
    private ImageView iv_add;

    private TagCloudView mCloudView;

    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

   // private CloudTagAdapter mCloudTagAdapter;
  //  private List<StarModel> mStarList = new ArrayList<>();

    private LodingView mLodingView;

    private DialogView mNullDialogView;
    private TextView tv_null_text;
    private TextView tv_null_cancel;

    //连接状态
    private TextView tv_connect_status;

    //全部好友
    private List<IMUser> mAllUserList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, null);
        initView(view);
        return view;
    }

    /**
     * 初始化View
     *
     * @param view
     */
    private void initView(View view) {
        mContext = view.getContext();
        //漩涡

        mRotateView = view.findViewById(R.id.rotate_view);
        mIvFlower2 = view.findViewById(R.id.iv_flower2);
        mIvFlower1 = view.findViewById(R.id.iv_flower1);
        mRlAnim = view.findViewById(R.id.rl_anim);
        //旋转
        startAnim();
        initData();
        mLodingView = new LodingView(getActivity());
//        mLodingView.setCancelable(false);

        //mNullDialogView = DialogManager.getInstance().initView(getActivity(), R.layout.layout_star_null_item, Gravity.BOTTOM);
        //tv_null_text = mNullDialogView.findViewById(R.id.tv_null_text);
        //tv_null_cancel = mNullDialogView.findViewById(R.id.tv_cancel);
//        tv_null_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DialogManager.getInstance().hide(mNullDialogView);
//            }
//        });

        iv_camera = view.findViewById(R.id.iv_camera);
        iv_add = view.findViewById(R.id.iv_add);
        tv_connect_status = view.findViewById(R.id.tv_connect_status);

        tv_star_title = view.findViewById(R.id.tv_star_title);

       // mCloudView = view.findViewById(R.id.mCloudView);

        //ll_random = view.findViewById(R.id.ll_random);
       // ll_soul = view.findViewById(R.id.ll_soul);
        //ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);

        iv_camera.setOnClickListener(this);
        iv_add.setOnClickListener(this);

//        ll_random.setOnClickListener(this);
//        ll_soul.setOnClickListener(this);
//        ll_fate.setOnClickListener(this);
        ll_love.setOnClickListener(this);

        //数据绑定
       // mCloudTagAdapter = new CloudTagAdapter(getActivity(), mStarList);
       // mCloudView.setAdapter(mCloudTagAdapter);

//        //监听点击事件
//        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
//            @Override
//            public void onItemClick(ViewGroup parent, View view, int position) {
//              //  startUserInfo(mStarList.get(position).getUserId());
//            }
//        });

        //绑定接口
        PairFriendHelper.getInstance().setOnPairResultListener(new PairFriendHelper.OnPairResultListener() {

            @Override
            public void OnPairListener(String userId) {
                startUserInfo(userId);
            }

            @Override
            public void OnPairFailListener() {
                mLodingView.hide();
                Toast.makeText(getActivity(), getString(R.string.text_pair_null), Toast.LENGTH_SHORT).show();
            }
        });

        loadStarUser();
    }
    /**
     * 跳转用户信息
     *
     * @param userId
     */
    private void startUserInfo(String userId) {
        mLodingView.hide();
        UserInfoActivity.startActivity(getActivity(), userId);
    }


    /**
     * 加载星球用户
     */
    private void loadStarUser() {
        //LogUtils.i("loadStarUser");
        /**
         * 我们从用户库中取抓取一定的好友进行匹配
         */
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
               // LogUtils.i("done");
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {

                        if (mAllUserList.size() > 0) {
                            mAllUserList.clear();
                        }

//                        if (mStarList.size() > 0) {
//                            mStarList.clear();
//                        }

                        mAllUserList = list;

                        //这里是所有的用户 只适合我们现在的小批量
                        int index = 50;
                        if (list.size() <= 50) {
                            index = list.size();
                        }
                        //直接填充
//                        for (int i = 0; i < index; i++) {
//                            IMUser imUser = list.get(i);
//                            saveStarUser(imUser.getObjectId(),
//                                    imUser.getNickName(),
//                                    imUser.getPhoto());
//                        }
                      //  LogUtils.i("done...");
                        //当请求数据已经加载出来的时候判断是否连接服务器
//                        if(CloudManager.getInstance().isConnect()){
//                            //已经连接，并且星球加载，则隐藏
//                            tv_connect_status.setVisibility(View.GONE);
//                        }
//                        mCloudTagAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_camera:
                //扫描
                Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.iv_add:
                //添加好友
                Log.d("qwe","添加好友");
                startActivity(new Intent(getActivity(), AddFriendActivity.class));
                break;
            case R.id.ll_love:
                //随机好友匹配匹配
                addViewStartAnim(0);
                break;
        }
    }
    /**
     * 匹配规则
     *
     * @param index
     */
    private void pairUser(int index) {

        if (CommonUtils.isEmpty(mAllUserList)) {
            //计算
            PairFriendHelper.getInstance().pairUser(index, mAllUserList);
        } else {
            mLodingView.hide();
            //onPairResultListener.OnPairFailListener();
        }
    }


    //漩涡

//    @Override
//    protected void onResume() {
//        super.onResume();
//        startAnim();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopIvAnim();
//    }

    private void initData() {
        for (int i = 0; i <7; i++) {
            imagePathList.add("");
        }

    }

    private void startAnim() {
        isStopAnim = false;
        mRotateView.startAnim();
        Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.anim_compose_guide_flower1);
        LinearInterpolator lir = new LinearInterpolator();
        animation1.setInterpolator(lir);
        Animation animation2 = AnimationUtils.loadAnimation(mContext, R.anim.anim_compose_guide_flower2);
        animation2.setInterpolator(lir);
        mIvFlower1.startAnimation(animation1);
        mIvFlower2.startAnimation(animation2);

        //addViewStartAnim(0);
    }

    private void stopIvAnim() {
        isStopAnim = true;
       // mRotateView.stopAnim();
       // mIvFlower1.clearAnimation();
       // mIvFlower2.clearAnimation();
    }

    //开启动画效果，同时准备匹配好友
    private void addViewStartAnim(int position) {
        if (isStopAnim) {
            isStopAnim=false;
            return;
        }
        if (position >= imagePathList.size()) {
           // addViewStartAnim(0);
            //调用好友匹配
            pairUser(0);
            return;
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_add_photo_item, null);
        int width = ScreenUtil.dip2px(mContext, 300);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        view.setLayoutParams(layoutParams);
        ImageView ivPhoto = view.findViewById(R.id.iv_photo);

        //ivPhoto.setImageResource(R.mipmap.ic_launcher);
        ivPhoto.setImageResource(R.drawable.hot_dog);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.compose_photo_rotation_scale);
        animation.setAnimationListener(new StarFragment.AnimListener());
        addRlAnimView(view);

        animation.setFillAfter(true);
        view.startAnimation(animation);
        position++;
        int pos = position;
        mHandler.postDelayed(() -> addViewStartAnim(pos), 300);

    }

    public void addRlAnimView(View view) {
        mRlAnim.addView(view);
    }


    private class AnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //需要在动画结束后删除view  否则view会越积攒越多
           // animation=null;

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }


    }


    //扫一扫回调
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                  //  LogUtils.i("result：" + result);
                    //Meet#c7a9b4794f
                    if (!TextUtils.isEmpty(result)) {
                        //是我们自己的二维码
                        if (result.startsWith("Meet")) {
                            String[] split = result.split("#");
                          //  LogUtils.i("split:" + split.toString());
                            if (split != null && split.length >= 2) {
                                try {
                                    UserInfoActivity.startActivity(getActivity(), split[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.text_toast_error_qrcode), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.text_toast_error_qrcode), Toast.LENGTH_SHORT).show();
                    }

                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), getString(R.string.text_qrcode_fail), Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}

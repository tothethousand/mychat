package com.example.mychat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.framework.base.BaseUIActivity;
import com.example.mychat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导页
 */
public class GuideActivity extends BaseUIActivity {

    private ViewPager mViewPager;
    private ImageView ivMusicSwitch;
    private TextView tvGuideSkip;
    private ImageView ivGuidePoint1;
    private ImageView ivGuidePoint2;
    private ImageView ivGuidePoint3;


    private View view1;
    private View view2;
    private View view3;

    private List<View> mPageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initView();
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        ivMusicSwitch = (ImageView) findViewById(R.id.iv_music_switch);
        tvGuideSkip = (TextView) findViewById(R.id.tv_guide_skip);
        ivGuidePoint1 = (ImageView) findViewById(R.id.iv_guide_point_1);
        ivGuidePoint2 = (ImageView) findViewById(R.id.iv_guide_point_2);
        ivGuidePoint3 = (ImageView) findViewById(R.id.iv_guide_point_3);

        view1 =View.inflate(this, R.layout.layout_pager_guide_1,null);
        view2 =View.inflate(this, R.layout.layout_pager_guide_2,null);
        view3 =View.inflate(this, R.layout.layout_pager_guide_3,null);
    }
}

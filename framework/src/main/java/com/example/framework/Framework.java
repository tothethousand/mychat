package com.example.framework;

import android.content.Context;
import android.util.Log;

import com.example.framework.bmob.BmobManager;
import com.example.framework.cloud.CloudManager;
import com.example.framework.helper.WindowHelper;
import com.example.framework.manager.MapManager;
import com.example.framework.utils.SpUtils;

import org.litepal.LitePal;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class Framework {
    private volatile static Framework mFramework;
    private Framework() {

    }

    public static Framework getFramework() {
        if (mFramework == null) {
            synchronized (Framework.class) {
                if (mFramework == null) {
                    mFramework = new Framework();
                }
            }
        }
        return mFramework;


    }
    public void initFramework(Context mContext){

        //本地存储
        SpUtils.getInstance().initSp(mContext);
        //初始化bmob
        BmobManager.getInstance().initBmob(mContext);

        //融云
        CloudManager.getInstance().initCloud(mContext);
        LitePal.initialize(mContext);
        MapManager.getInstance().initMap(mContext);
        WindowHelper.getInstance().initWindow(mContext);

        //全局捕获RxJava异常
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
              //  LogUtils.e("RxJava：" + throwable.toString());
                Log.d("qazx",throwable.toString());
            }
        });
    }



}

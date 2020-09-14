package com.example.mychat.fragment.chat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.framework.adapter.CommonAdapter;
import com.example.framework.adapter.CommonViewHolder;
import com.example.framework.base.BaseFragment;
import com.example.framework.bmob.BmobManager;
import com.example.framework.bmob.IMUser;
import com.example.framework.cloud.CloudManager;
import com.example.framework.event.EventManager;
import com.example.framework.event.MessageEvent;
import com.example.framework.gson.TextBean;
import com.example.framework.utils.CommonUtils;
import com.example.mychat.R;
import com.example.mychat.fragment.refresh_util.PullToRefreshView;
import com.example.mychat.model.ChatRecordModel;
import com.example.mychat.ui.ChatActivity;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

/**
 * Profile: 聊天记录
 */
// implements SwipeRefreshLayout.OnRefreshListener
public class ChatRecordFragment extends BaseFragment {

    private PullToRefreshView mPullToRefreshView;

    private View item_empty_view;
    private RecyclerView mChatRecordView;
   // private SwipeRefreshLayout mChatRecordRefreshLayout;

    private CommonAdapter<ChatRecordModel> mChatRecordAdapter;
    private List<ChatRecordModel> mList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_record, null);
        initView(view);
        return view;
    }

    private void initView(final View view) {

        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //新版刷新
                mPullToRefreshView.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        queryChatRecord();
                    }
                }, 1200);


            }
        });



        item_empty_view = view.findViewById(R.id.item_empty_view);
       // mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);
        mChatRecordView = view.findViewById(R.id.mChatRecordView);

       // mChatRecordRefreshLayout.setOnRefreshListener(this);

        mChatRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        mChatRecordAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<ChatRecordModel>() {
            @Override
            public void onBindViewHolder(final ChatRecordModel model, CommonViewHolder viewHolder, int type, int position) {
                viewHolder.setImageUrl(getActivity(), R.id.iv_photo, model.getUrl());
                viewHolder.setText(R.id.tv_nickname, model.getNickName());
                viewHolder.setText(R.id.tv_content, model.getEndMsg());
                viewHolder.setText(R.id.tv_time, model.getTime());

                if(model.getUnReadSize() == 0){
                    viewHolder.getView(R.id.tv_un_read).setVisibility(View.GONE);
                }else{
                    viewHolder.getView(R.id.tv_un_read).setVisibility(View.VISIBLE);
                    viewHolder.setText(R.id.tv_un_read, model.getUnReadSize() + "");
                }

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChatActivity.startActivity(getActivity(),
                                model.getUserId(),model.getNickName(),model.getUrl());
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_chat_record_item;
            }
        });
        mChatRecordView.setAdapter(mChatRecordAdapter);

        //避免重复
        //queryChatRecord();
    }

    /**
     * 查询聊天记录
     */
    private void queryChatRecord() {
       // mChatRecordRefreshLayout.setRefreshing(true);

        CloudManager.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
             //   LogUtils.i("onSuccess");
             //   mChatRecordRefreshLayout.setRefreshing(false);
                mPullToRefreshView.setRefreshing(false);
                if (CommonUtils.isEmpty(conversations)) {
                    if (mList.size() > 0) {
                        mList.clear();
                    }
                    for (int i = 0; i < conversations.size(); i++) {
                        final Conversation c = conversations.get(i);
                        String id = c.getTargetId();
                        //查询对象的信息
                        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
                            @Override
                            public void done(List<IMUser> list, BmobException e) {
                                if (e == null) {
                                    if (CommonUtils.isEmpty(list)) {
                                        IMUser imUser = list.get(0);
                                        ChatRecordModel chatRecordModel = new ChatRecordModel();
                                        chatRecordModel.setUserId(imUser.getObjectId());
                                        chatRecordModel.setUrl(imUser.getPhoto());
                                        chatRecordModel.setNickName(imUser.getNickName());
                                        chatRecordModel.setTime(new SimpleDateFormat("HH:mm:ss")
                                                .format(c.getReceivedTime()));
                                        chatRecordModel.setUnReadSize(c.getUnreadMessageCount());

                                        String objectName = c.getObjectName();
                                        if (objectName.equals(CloudManager.MSG_TEXT_NAME)) {
                                            TextMessage textMessage = (TextMessage) c.getLatestMessage();
                                            String msg = textMessage.getContent();
                                            TextBean bean = new Gson().fromJson(msg, TextBean.class);
                                            if (bean.getType().equals(CloudManager.TYPE_TEXT)) {
                                                chatRecordModel.setEndMsg(bean.getMsg());
                                                mList.add(chatRecordModel);
                                            }
                                        } else if (objectName.equals(CloudManager.MSG_IMAGE_NAME)) {
                                            chatRecordModel.setEndMsg(getString(R.string.text_chat_record_img));
                                            mList.add(chatRecordModel);
                                        } else if (objectName.equals(CloudManager.MSG_LOCATION_NAME)) {
                                            chatRecordModel.setEndMsg(getString(R.string.text_chat_record_location));
                                            mList.add(chatRecordModel);
                                        }
                                        mChatRecordAdapter.notifyDataSetChanged();

                                        if(mList.size() > 0){
                                            item_empty_view.setVisibility(View.GONE);
                                            mChatRecordView.setVisibility(View.VISIBLE);
                                        }else{
                                            item_empty_view.setVisibility(View.VISIBLE);
                                            mChatRecordView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }else{
                  //  mChatRecordRefreshLayout.setRefreshing(false);
                    item_empty_view.setVisibility(View.VISIBLE);
                    mChatRecordView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
              //  LogUtils.i("onError" + errorCode);
              //  mChatRecordRefreshLayout.setRefreshing(false);
                mPullToRefreshView.setRefreshing(false);
            }
        });
    }

//    @Override
//    public void onRefresh() {
////        if (mChatRecordRefreshLayout.isRefreshing()) {
////
////        }
//        queryChatRecord();
//    }
//
    @Override
    public void onResume() {
        super.onResume();
        queryChatRecord();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.FLAG_UPDATE_FRIEND_LIST:
//                if (mChatRecordRefreshLayout.isRefreshing()) {
//
//                }
                queryChatRecord();
                break;
        }
    }
}

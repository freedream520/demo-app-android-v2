package io.rong.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import io.rong.app.R;
import io.rong.app.server.utils.NToast;
import io.rong.app.ui.activity.FriendListActivity;
import io.rong.app.ui.activity.MainActivity;
import io.rong.app.ui.activity.SelectFriendsActivity;
import io.rong.app.ui.activity.UpdateDiscussionActivity;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.DispatchResultFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;

/**
 * Created by Bob on 2015/3/27.
 */
public class SettingFragment extends DispatchResultFragment implements View.OnClickListener {
    public static final int STARTADDDISCUSSIONMEMBER = 23;
    private String TAG = SettingFragment.class.getSimpleName();
    private String targetId;
    private Conversation.ConversationType mConversationType;
    private Button mDeleteBtn;
    private RelativeLayout mChatRoomRel;
    private TextView mChatRoomName;

    private android.support.v4.app.Fragment mAddNumberFragment;
    private android.support.v4.app.Fragment mToTopFragment;
    private String mDiscussionName;
    private FragmentTransaction fragmentTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_ac_friend_setting, container, false);

        mDeleteBtn = (Button) view.findViewById(R.id.de_fr_delete);
        mChatRoomRel = (RelativeLayout) view.findViewById(R.id.de_set_chatroom_name);
        mChatRoomName = (TextView) view.findViewById(R.id.de_chatroom_name);

        mAddNumberFragment = getChildFragmentManager().findFragmentById(R.id.de_fr_add_friend);
        mToTopFragment = getChildFragmentManager().findFragmentById(R.id.de_fr_to_top);

        fragmentTransaction = getFragmentManager().beginTransaction();

        mDeleteBtn.setOnClickListener(this);
        mChatRoomRel.setOnClickListener(this);

        Intent intent = getActivity().getIntent();

        targetId = intent.getData().getQueryParameter("targetId");

        if (targetId != null) {

            mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
            showViewByConversationType(mConversationType);

            RongContext.getInstance().setOnMemberSelectListener(new RongIM.OnSelectMemberListener() {
                @Override
                public void startSelectMember(Context context, Conversation.ConversationType conversationType, final String id) {
                    mConversationType = Conversation.ConversationType.valueOf(getActivity().getIntent().getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
                    //TODO 先获取成员再开启设置界面  代码未编写
                    if (mConversationType == Conversation.ConversationType.DISCUSSION && RongIM.getInstance() != null) {
                        RongIM.getInstance().getRongIMClient().getDiscussion(id, new RongIMClient.ResultCallback<Discussion>() {
                            @Override
                            public void onSuccess(Discussion discussion) {
                                Intent in = new Intent(getActivity(), SelectFriendsActivity.class);
                                in.putStringArrayListExtra("DISCUSSIONMEMBER", (ArrayList<String>) discussion.getMemberIdList());
                                in.putExtra("DEMO_FRIEND_TARGETID", id);
                                in.putExtra("CONVERSATION_DISCUSSION", true);
                                startActivityForResult(in, STARTADDDISCUSSIONMEMBER);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    } else {
                        Intent in = new Intent(getActivity(), SelectFriendsActivity.class);
                        in.putExtra("DEMO_FRIEND_TARGETID", id);
                        in.putExtra("CONVERSATION_PRIVATE", true);
                        startActivityForResult(in, 22);
                    }
                }
            });
        }
        return view;
    }

    /**
     * 通过 会话类型选择要展示的 fragment
     *
     * @param mConversationType 会话类型
     */
    private void showViewByConversationType(Conversation.ConversationType mConversationType) {
        if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            mDeleteBtn.setVisibility(View.VISIBLE);
            mChatRoomRel.setVisibility(View.VISIBLE);
            RongIM.getInstance().getRongIMClient().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    mDiscussionName = discussion.getName();

                    mChatRoomName.setText(mDiscussionName);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        } else if (mConversationType.equals(Conversation.ConversationType.PRIVATE)) {

        } else if (mConversationType.equals(Conversation.ConversationType.CHATROOM)) {
            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.hide(mToTopFragment);
            fragmentTransaction.commit();
        } else if (mConversationType.equals(Conversation.ConversationType.GROUP)) {

            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.commit();
        } else if (mConversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.hide(mToTopFragment);
            fragmentTransaction.commit();
        }
    }


    @Override
    protected void initFragment(Uri uri) {

    }



    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.de_fr_delete: //删除并退出讨论组
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().quitDiscussion(targetId, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {

                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                break;
            case R.id.de_set_chatroom_name:
                Intent intent = new Intent(getActivity(), UpdateDiscussionActivity.class);
                intent.putExtra("DEMO_DISCUSSIONIDS", targetId);
                if (mDiscussionName != null)
                    intent.putExtra("DEMO_DISCUSSIONNAME", mDiscussionName.toString());
                startActivityForResult(intent, 21);
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Constants.FIX_DISCUSSION_NAME:
                if (data != null)
                    mChatRoomName.setText(data.getStringExtra("UPDATA_DISCUSSION_RESULT"));

                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

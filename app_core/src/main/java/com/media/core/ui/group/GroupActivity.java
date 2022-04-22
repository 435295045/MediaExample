package com.media.core.ui.group;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.media.core.MainConstant;
import com.media.core.R;
import com.media.core.ui.member.Member;
import com.media.core.ui.member.MemberUtils;
import com.media.core.ui.utils.PermissionUtil;
import com.media.core.ui.utils.SPUtils;
import com.media.core.ui.utils.Utils;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.group.listener.GroupListener;
import com.media.rtc.media.group.listener.state.GroupState;
import com.media.rtc.webrtc.PeerFactoryHelper;
import com.media.rtc.webrtc.peer.Peer;
import com.web.socket.message.MessageConstant;
import com.web.socket.utils.LoggerUtils;
import com.web.socket.utils.StringUtils;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GroupActivity extends AppCompatActivity implements
        View.OnClickListener,
        GroupListener {
    private static final String TAG = GroupActivity.class.getSimpleName();
    private FrameLayout fLayoutVideo;
    private TextView tViewAnswer;
    private int mScreenWidth;
    //记录所有成员
    private final List<Member> members = new ArrayList<>();
    private PeerFactoryHelper peerFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initView();
        //监听状态
        MediaSDK.group().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(GroupActivity.this);
    }

    private void initView() {
        fLayoutVideo = findViewById(R.id.fLayoutVideo);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (manager != null) {
            mScreenWidth = manager.getDefaultDisplay().getWidth();
        }
        fLayoutVideo.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mScreenWidth));
        findViewById(R.id.tViewSwitchMute).setOnClickListener(this);
        findViewById(R.id.tViewHandFree).setOnClickListener(this);
        findViewById(R.id.tViewOpenCamera).setOnClickListener(this);
        findViewById(R.id.tViewSwitchCamera).setOnClickListener(this);
        tViewAnswer = findViewById(R.id.tViewAnswer);
        tViewAnswer.setOnClickListener(this);
        findViewById(R.id.tViewHangUp).setOnClickListener(this);
        String initiator = getIntent().getStringExtra("initiator");
        if (StringUtils.isEmpty(initiator)) {
            tViewAnswer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tViewSwitchMute) {
            if (peerFactory != null) {
                peerFactory.microphoneMute(!peerFactory.isMicrophoneMute());
            }
        } else if (id == R.id.tViewHandFree) {
            if (peerFactory != null) {
                peerFactory.speakerphone(!peerFactory.isSpeakerphone());
            }
        } else if (id == R.id.tViewOpenCamera) {
            if (peerFactory != null) {
                peerFactory.videoEnabled(!peerFactory.isVideoEnabled());
            }
        } else if (id == R.id.tViewSwitchCamera) {
            if (peerFactory != null) {
                peerFactory.switchCamera();
            }
        } else if (id == R.id.tViewAnswer) {
            MediaSDK.group().answer();
            tViewAnswer.setVisibility(View.GONE);
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.group().hangUp();
            finish();
        }
    }

    private void addMember(Member member, boolean isLocal) {
        fLayoutVideo.addView(member.renderer);
        if (isLocal) {
            members.add(0, member);
        } else {
            members.add(member);
        }
        int size = members.size();
        for (int i = 0; i < size; i++) {
            Member forMember = members.get(i);
            SurfaceViewRenderer renderer = forMember.renderer;
            if (renderer != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.height = Utils.getWidth(mScreenWidth, size);
                layoutParams.width = Utils.getWidth(mScreenWidth, size);
                layoutParams.leftMargin = Utils.getX(mScreenWidth, size, i);
                layoutParams.topMargin = Utils.getY(mScreenWidth, size, i);
                renderer.setLayoutParams(layoutParams);
            }
        }
    }

    private void removeMember(String clientId) {
        Iterator<Member> iterator = members.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (clientId.equals(member.peer.id)) {
                if (member.sink != null) {
                    member.sink.setTarget(null);
                }
                if (member.renderer != null) {
                    member.renderer.release();
                }
                member.peer.dispose();
                fLayoutVideo.removeView(member.renderer);
                iterator.remove();
            }
        }
        int size = members.size();
        for (int i = 0; i < size; i++) {
            Member forMember = members.get(i);
            SurfaceViewRenderer renderer = forMember.renderer;
            if (renderer != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.height = Utils.getWidth(mScreenWidth, size);
                layoutParams.width = Utils.getWidth(mScreenWidth, size);
                layoutParams.leftMargin = Utils.getX(mScreenWidth, size, i);
                layoutParams.topMargin = Utils.getY(mScreenWidth, size, i);
                renderer.setLayoutParams(layoutParams);
            }
        }
    }

    @Override  // 屏蔽返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.e(TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
    }

    @Override
    public void onGroupListener(GroupState groupState) {
        if (groupState instanceof GroupState.OfferAck) {

        } else if (groupState instanceof GroupState.Answer) {
            //有人接听, 发布媒体
            try {
                MediaSDK.group().producer(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (groupState instanceof GroupState.AnswerAck) {
            //接听反馈
            GroupState.AnswerAck answerAck = (GroupState.AnswerAck) groupState;
            //如果接听成功，发布视频，并订阅媒体
            if (answerAck.code == MessageConstant.RETURN_CPDE_SUCCESS) {
                try {
                    MediaSDK.group().producer(true);
                    MediaSDK.group().consume(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            LoggerUtils.e(TAG, answerAck.toString());
        } else if (groupState instanceof GroupState.Producer) {
            //有人发布媒体, 订阅这个媒体
            try {
                //GroupState.Producer producer = (GroupState.Producer) groupState;
                MediaSDK.group().consume(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (groupState instanceof GroupState.ProducerAck) {
            //发布反馈
            GroupState.ProducerAck producerAck = (GroupState.ProducerAck) groupState;
            LoggerUtils.e(TAG, producerAck.toString());
        } else if (groupState instanceof GroupState.ConsumeAck) {
            //订阅反馈
            GroupState.ConsumeAck consumeAck = (GroupState.ConsumeAck) groupState;
            LoggerUtils.e(TAG, consumeAck.toString());
        } else if (groupState instanceof GroupState.HangUp) {
            //收到挂断信息
            GroupState.HangUp hangUp = (GroupState.HangUp) groupState;
            String myClientId = SPUtils.getString(this, MainConstant.SP_KEY_CLIENT, "");
            //如果挂断的是自己
            if (myClientId.equals(hangUp.clientId)) {
                finish();
            } else {
                removeMember(hangUp.clientId);
            }
        } else if (groupState instanceof GroupState.LocalMedia) {
            GroupState.LocalMedia media = (GroupState.LocalMedia) groupState;
            this.peerFactory = media.factory;
            Member member = MemberUtils.createMember(this, media.peer, null);
            if (media.factory.localMedia != null) {
                media.factory.localMedia.localVideoTrack.addSink(member.sink);
                addMember(member, true);
            }
        } else if (groupState instanceof GroupState.RemoteMedia) {
            GroupState.RemoteMedia media = (GroupState.RemoteMedia) groupState;
            //媒体连接成功
            Member member = MemberUtils.createMember(this, media.peer, media.stream);
            addMember(member, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有媒体显示（必须）
        for (Member member : members) {
            if (member.renderer != null) {
                member.renderer.release();
            }
            if (member.sink != null) {
                member.sink.setTarget(null);
            }
        }
        members.clear();
        //移除监听（必须）
        MediaSDK.group().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.group().release();
    }
}

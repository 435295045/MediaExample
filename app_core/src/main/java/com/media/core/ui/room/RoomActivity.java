package com.media.core.ui.room;

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
import com.media.core.ui.member.MCUMemberHandle;
import com.media.core.ui.utils.PermissionUtil;
import com.media.core.ui.utils.SPUtils;
import com.media.core.ui.utils.Utils;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.room.listener.RoomListener;
import com.media.rtc.media.room.listener.state.RoomState;
import com.media.rtc.webrtc.PeerFactoryHelper;
import com.web.socket.message.MessageConstant;
import com.web.socket.utils.LoggerUtils;
import com.web.socket.utils.StringUtils;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener,
        RoomListener {
    private static final String TAG = RoomActivity.class.getSimpleName();
    private FrameLayout fLayoutVideo;
    private TextView tViewAnswer;
    private int mScreenWidth;
    //记录所有成员
    private MCUMemberHandle mcuMemberHandle;
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
        mcuMemberHandle = new MCUMemberHandle();
        initView();
        //监听状态
        MediaSDK.room().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(RoomActivity.this);
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
        String roomId = getIntent().getStringExtra("roomId");
        try {
            MediaSDK.room().join(roomId);
        } catch (Exception e) {
            e.printStackTrace();
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
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.room().leave();
            finish();
        }
    }

    private void addMember(RoomState.Media media) {
        Member member = mcuMemberHandle.getMember(media.peer.id);
        //如果存在先释放
        if (member != null) {
            if (member.renderer != null)
                fLayoutVideo.removeView(member.renderer);
            mcuMemberHandle.release(media.peer.id);
        }
        //本地媒体
        if (media.factory != null) {
            this.peerFactory = media.factory;
            //预览自己
            if (media.factory.localMedia != null) {
                mcuMemberHandle.createMember(this, media.peer, null);
                member = mcuMemberHandle.getMember(media.peer.id);
                media.factory.localMedia.localVideoTrack.addSink(member.sink);
                if (member.renderer != null)
                    fLayoutVideo.addView(member.renderer, 0);
            }
        } else {
            //远程视频
            mcuMemberHandle.createMember(this, media.peer, media.stream);
            member = mcuMemberHandle.getMember(media.peer.id);
            if (member.renderer != null)
                fLayoutVideo.addView(member.renderer);
        }
        refreshUI();
    }

    private void removeMember(String id) {
        Member member = mcuMemberHandle.getMember(id);
        //释放
        if (member != null) {
            if (member.renderer != null)
                fLayoutVideo.removeView(member.renderer);
            mcuMemberHandle.release(id);
        }
        refreshUI();
    }

    private void refreshUI() {
        List<Member> members = new ArrayList<>(mcuMemberHandle.memberHashtable.values());
        int size = members.size();
        for (int i = 0; i < size; i++) {
            Member member = members.get(i);
            SurfaceViewRenderer renderer = member.renderer;
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
    public void onRoomListener(RoomState roomState) {
        if (roomState instanceof RoomState.Join) {
            //有人加入房间
            RoomState.Join join = (RoomState.Join) roomState;
            LoggerUtils.e(TAG, join.toString());
        } else if (roomState instanceof RoomState.JoinAck) {
            try {
                RoomState.JoinAck joinAck = (RoomState.JoinAck) roomState;
                if (joinAck.code == MessageConstant.RETURN_CPDE_SUCCESS) {
                    //加房成功发布媒体
                    MediaSDK.room().producer(true);
                    MediaSDK.room().consume(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (roomState instanceof RoomState.Producer) {
            //有人发布媒体, 订阅这个媒体
            try {
                //RoomState.Producer producer = (RoomState.Producer) roomState;
                MediaSDK.room().consume(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (roomState instanceof RoomState.ProducerAck) {
            //发布反馈
            RoomState.ProducerAck producerAck = (RoomState.ProducerAck) roomState;
            LoggerUtils.e(TAG, producerAck.toString());
        } else if (roomState instanceof RoomState.ConsumeAck) {
            //订阅反馈
            RoomState.ConsumeAck consumeAck = (RoomState.ConsumeAck) roomState;
            LoggerUtils.e(TAG, consumeAck.toString());
        } else if (roomState instanceof RoomState.Leave) {
            RoomState.Leave leave = (RoomState.Leave) roomState;
            String myClientId = SPUtils.getString(this, MainConstant.SP_KEY_CLIENT, "");
            //如果挂断的是自己
            if (myClientId.equals(leave.clientId)) {
                finish();
            } else {
                removeMember(leave.clientId);
            }
        } else if (roomState instanceof RoomState.Media) {
            RoomState.Media media = (RoomState.Media) roomState;
            addMember(media);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有媒体显示（必须）
        mcuMemberHandle.release();
        //移除监听（必须）
        MediaSDK.room().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.room().release();
    }
}

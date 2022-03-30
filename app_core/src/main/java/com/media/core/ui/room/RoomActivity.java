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
import com.media.core.ui.member.MemberUtils;
import com.media.core.ui.utils.PermissionUtil;
import com.media.core.ui.utils.SPUtils;
import com.media.core.ui.utils.Utils;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.door.listener.state.DoorState;
import com.media.rtc.media.room.listener.RoomListener;
import com.media.rtc.media.room.listener.state.RoomState;
import com.media.rtc.media.room.model.ClientDescription;
import com.media.rtc.media.room.model.RoomModel;
import com.media.rtc.webrtc.peer.Peer;
import com.web.socket.message.MessageConstant;
import com.web.socket.utils.LoggerUtils;
import com.web.socket.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener,
        RoomListener {
    private static final String TAG = RoomActivity.class.getSimpleName();
    private FrameLayout fLayoutVideo;
    private TextView tViewAnswer;
    private int mScreenWidth;
    //记录所有成员
    private final List<Member> members = new ArrayList<>();
    private Peer localPeer;

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
            if (localPeer != null) {
                localPeer.microphoneMute(!localPeer.isMicrophoneMute());
            }
        } else if (id == R.id.tViewHandFree) {
            if (localPeer != null) {
                localPeer.speakerphone(!localPeer.isSpeakerphone());
            }
        } else if (id == R.id.tViewOpenCamera) {
            if (localPeer != null) {
                localPeer.videoEnabled(!localPeer.isVideoEnabled());
            }
        } else if (id == R.id.tViewSwitchCamera) {
            if (localPeer != null) {
                localPeer.switchCamera();
            }
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.room().leave();
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
            //媒体连接成功
            Member member = MemberUtils.createMember(this, media.peer, media.stream);
            if (media.stream == null) {
                localPeer = media.peer;
                //自己预览
                media.peer.setLocalSink(member.sink);
                addMember(member, true);
            } else {
                addMember(member, false);
            }
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
        MediaSDK.room().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.room().release();
    }
}

package com.media.core.ui.door;

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
import com.media.core.ui.member.MemberHandle;
import com.media.core.ui.utils.PermissionUtil;
import com.media.core.ui.utils.SPUtils;
import com.media.core.ui.utils.Utils;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.door.listener.DoorListener;
import com.media.rtc.media.door.listener.state.DoorState;
import com.media.rtc.webrtc.PeerFactoryHelper;
import com.web.socket.message.MessageConstant;
import com.web.socket.utils.LoggerUtils;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

public class DoorActivity extends AppCompatActivity implements
        View.OnClickListener,
        DoorListener {
    private static final String TAG = DoorActivity.class.getSimpleName();
    private FrameLayout fLayoutVideo;
    private TextView tViewAnswer;
    private int mScreenWidth;
    //记录所有成员
    private MemberHandle memberHandle;
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
        memberHandle = new MemberHandle();
        initView();
        //监听状态
        MediaSDK.door().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(DoorActivity.this);
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
        String[] clients = getIntent().getStringArrayExtra("clients");
        if (clients == null) {
            tViewAnswer.setVisibility(View.VISIBLE);
        } else {
            MediaSDK.door().call("9876543212345678", clients);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //UI初始化完成订阅一下视频
            try {
                MediaSDK.door().consume(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            MediaSDK.door().answer();
            tViewAnswer.setVisibility(View.GONE);
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.door().hangUp();
            finish();
        }
    }

    private void addMember(DoorState.Media media) {
        Member member = memberHandle.getMember(media.peer.id);
        LoggerUtils.e(TAG, "---------------:  "+media.toString());
        //如果存在先释放
        if (member != null) {
            if (member.renderer != null)
                fLayoutVideo.removeView(member.renderer);
            memberHandle.release(media.peer.id);
        }
        //本地媒体
        if (media.factory != null) {
            this.peerFactory = media.factory;
            //预览自己
            if (media.factory.localMedia != null && media.factory.localMedia.localVideoTrack != null) {
                memberHandle.createMember(this, media.peer, null);
                member = memberHandle.getMember(media.peer.id);
                media.factory.localMedia.localVideoTrack.addSink(member.sink);
                if (member.renderer != null)
                    fLayoutVideo.addView(member.renderer, 0);
            }
        } else {
            //远程视频
            memberHandle.createMember(this, media.peer, media.stream);
            member = memberHandle.getMember(media.peer.id);
            if (member.renderer != null)
                fLayoutVideo.addView(member.renderer);
        }
        refreshUI();
    }

    private void removeMember(String id) {
        Member member = memberHandle.getMember(id);
        //释放
        if (member != null) {
            if (member.renderer != null)
                fLayoutVideo.removeView(member.renderer);
            memberHandle.release(id);
        }
        refreshUI();
    }

    private void refreshUI() {
        List<Member> members = new ArrayList<>(memberHandle.memberHashtable.values());
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
    public void onDoorListener(DoorState doorState) {
        if (doorState instanceof DoorState.OfferAck) {
            //呼叫反馈
            DoorState.OfferAck offerAck = (DoorState.OfferAck) doorState;
            if (offerAck.code == MessageConstant.RETURN_CPDE_SUCCESS) {
                //有人接听, 发布媒体
                try {
                    MediaSDK.door().producer(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (doorState instanceof DoorState.Answer) {

        } else if (doorState instanceof DoorState.AnswerAck) {
            //接听反馈
            DoorState.AnswerAck answerAck = (DoorState.AnswerAck) doorState;
            //如果接听成功，发布视频
            if (answerAck.code == MessageConstant.RETURN_CPDE_SUCCESS) {
                try {
                    MediaSDK.door().producer(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            LoggerUtils.e(TAG, answerAck.toString());
        } else if (doorState instanceof DoorState.Producer) {
            //有人发布媒体, 订阅这个媒体
            try {
                //DoorState.Producer producer = (DoorState.Producer) doorState;
                MediaSDK.door().consume(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (doorState instanceof DoorState.ProducerAck) {
            //发布反馈
            DoorState.ProducerAck producerAck = (DoorState.ProducerAck) doorState;
            LoggerUtils.e(TAG, producerAck.toString());
        } else if (doorState instanceof DoorState.ConsumeAck) {
            //订阅反馈
            DoorState.ConsumeAck consumeAck = (DoorState.ConsumeAck) doorState;
            LoggerUtils.e(TAG, consumeAck.toString());
        } else if (doorState instanceof DoorState.HangUp) {
            //收到挂断信息
            DoorState.HangUp hangUp = (DoorState.HangUp) doorState;
            String myClientId = SPUtils.getString(this, MainConstant.SP_KEY_CLIENT, "");
            //如果挂断的是自己
            if (myClientId.equals(hangUp.clientId)) {
                finish();
            } else {
                removeMember(hangUp.clientId);
            }
        } else if (doorState instanceof DoorState.Media) {
            DoorState.Media media = (DoorState.Media) doorState;
            addMember(media);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有媒体显示（必须）
        memberHandle.release();
        //移除监听（必须）
        MediaSDK.door().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.door().release();
    }
}

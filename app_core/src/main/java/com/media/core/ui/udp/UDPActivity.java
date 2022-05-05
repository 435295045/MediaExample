package com.media.core.ui.udp;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.media.core.MainConstant;
import com.media.core.R;
import com.media.core.ui.member.Member;
import com.media.core.ui.member.MemberUtils;
import com.media.core.ui.utils.PermissionUtil;
import com.media.core.ui.utils.SPUtils;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.udp.listener.UDPListener;
import com.media.rtc.media.udp.listener.state.UDPState;
import com.media.rtc.webrtc.PeerFactoryHelper;

public class UDPActivity extends AppCompatActivity implements View.OnClickListener,
        UDPListener {

    private FrameLayout fLayoutLocalVideo, fLayoutRemoteVideo;
    private TextView tViewAnswer;
    private Member remoteMember;
    private PeerFactoryHelper peerFactory;
    private String[] clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p);
        initView();
        //监听状态
        MediaSDK.udp().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(UDPActivity.this);
    }

    private void initView() {
        fLayoutLocalVideo = findViewById(R.id.fLayoutLocalVideo);
        fLayoutRemoteVideo = findViewById(R.id.fLayoutRemoteVideo);
        findViewById(R.id.tViewSwitchMute).setOnClickListener(this);
        findViewById(R.id.tViewHandFree).setOnClickListener(this);
        findViewById(R.id.tViewOpenCamera).setOnClickListener(this);
        findViewById(R.id.tViewSwitchCamera).setOnClickListener(this);
        tViewAnswer = findViewById(R.id.tViewAnswer);
        tViewAnswer.setOnClickListener(this);
        findViewById(R.id.tViewHangUp).setOnClickListener(this);
        clients = getIntent().getStringArrayExtra("clients");
        if (clients == null) {
            tViewAnswer.setVisibility(View.VISIBLE);
        } else {
            try {
                MediaSDK.udp().call(clients);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //UI初始化完成订阅一下视频
            try {
                if (clients == null) {
                    MediaSDK.udp().consume();
                }
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
            MediaSDK.udp().answer();
            tViewAnswer.setVisibility(View.GONE);
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.udp().hangUp();
            finish();
        }
    }

    @Override
    public void onUDPListener(UDPState udpState) {
        if (udpState instanceof UDPState.OfferAck) {

        } else if (udpState instanceof UDPState.Answer) {

        } else if (udpState instanceof UDPState.AnswerAck) {

        } else if (udpState instanceof UDPState.HangUp) {
            UDPState.HangUp hangUp = (UDPState.HangUp) udpState;
            String myClientId = SPUtils.getString(this, MainConstant.SP_KEY_CLIENT, "");
            if (myClientId.equals(hangUp.clientId)) {
                finish();
            }
        } else if (udpState instanceof UDPState.LocalMedia) {
            UDPState.LocalMedia media = (UDPState.LocalMedia) udpState;
            Member member = MemberUtils.createMember(this, media.peer, null);
            this.peerFactory = media.factory;
            //自己预览
            /*if (media.factory.localMedia != null) {
                media.factory.localMedia.localVideoTrack.addSink(member.sink);
                fLayoutLocalVideo.addView(member.renderer);
                member.renderer.setZOrderOnTop(true);
            }*/
        } else if (udpState instanceof UDPState.RemoteMedia) {
            UDPState.RemoteMedia media = (UDPState.RemoteMedia) udpState;
            Member member = MemberUtils.createMember(this, media.peer, media.stream);
            remoteMember = member;
            fLayoutRemoteVideo.addView(member.renderer);
            member.renderer.getHolder().setFormat(PixelFormat.TRANSPARENT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteMember != null && remoteMember.renderer != null) {
            remoteMember.renderer.release();
        }
        if (remoteMember != null && remoteMember.sink != null) {
            remoteMember.sink.setTarget(null);
        }

        //移除监听（必须）
        MediaSDK.udp().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.udp().release();
    }
}

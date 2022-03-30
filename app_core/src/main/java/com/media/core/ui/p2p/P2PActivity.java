package com.media.core.ui.p2p;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.media.core.R;
import com.media.core.ui.member.Member;
import com.media.core.ui.member.MemberUtils;
import com.media.core.ui.utils.PermissionUtil;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.p2p.listener.P2PListener;
import com.media.rtc.media.p2p.listener.state.P2PState;
import com.media.rtc.webrtc.peer.Peer;
import com.web.socket.utils.LoggerUtils;
import com.web.socket.utils.StringUtils;

import org.webrtc.MediaStream;

public class P2PActivity extends AppCompatActivity implements View.OnClickListener,
        P2PListener {

    private FrameLayout fLayoutLocalVideo, fLayoutRemoteVideo;
    private TextView tViewAnswer;
    private Member localMember, remoteMember;

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
        MediaSDK.p2p().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(P2PActivity.this);
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
        String initiator = getIntent().getStringExtra("initiator");
        if (StringUtils.isEmpty(initiator)) {
            tViewAnswer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tViewSwitchMute) {
            if (localMember != null && localMember.peer != null) {
                localMember.peer.microphoneMute(!localMember.peer.isMicrophoneMute());
            }
        } else if (id == R.id.tViewHandFree) {
            if (localMember != null && localMember.peer != null) {
                localMember.peer.speakerphone(!localMember.peer.isSpeakerphone());
            }
        } else if (id == R.id.tViewOpenCamera) {
            if (localMember != null && localMember.peer != null) {
                localMember.peer.videoEnabled(!localMember.peer.isVideoEnabled());
            }
        } else if (id == R.id.tViewSwitchCamera) {
            if (localMember != null && localMember.peer != null) {
                localMember.peer.switchCamera();
            }
        } else if (id == R.id.tViewAnswer) {
            MediaSDK.p2p().answer();
            tViewAnswer.setVisibility(View.GONE);
        } else if (id == R.id.tViewHangUp) {
            MediaSDK.p2p().hangUp();
            finish();
        }
    }

    @Override
    public void onP2PListener(P2PState p2pState) {
        if (p2pState instanceof P2PState.OfferAck) {

        } else if (p2pState instanceof P2PState.Answer) {

        } else if (p2pState instanceof P2PState.AnswerAck) {

        } else if (p2pState instanceof P2PState.HangUp) {
            finish();
        } else if (p2pState instanceof P2PState.Media){
            P2PState.Media media = (P2PState.Media) p2pState;
            Member member = MemberUtils.createMember(this, media.peer, media.stream);
            if (media.stream == null) {
                localMember = member;
                //自己预览
                media.peer.setLocalSink(member.sink);
                fLayoutLocalVideo.addView(member.renderer);
                member.renderer.setZOrderOnTop(true);
            } else {
                remoteMember = member;
                fLayoutRemoteVideo.addView(member.renderer);
                member.renderer.getHolder().setFormat(PixelFormat.TRANSPARENT);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localMember != null && localMember.renderer != null) {
            localMember.renderer.release();
        }
        if (localMember != null && localMember.sink != null) {
            localMember.sink.setTarget(null);
        }

        if (remoteMember != null && remoteMember.renderer != null) {
            remoteMember.renderer.release();
        }
        if (remoteMember != null && remoteMember.sink != null) {
            remoteMember.sink.setTarget(null);
        }

        //移除监听（必须）
        MediaSDK.p2p().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.p2p().release();
    }
}
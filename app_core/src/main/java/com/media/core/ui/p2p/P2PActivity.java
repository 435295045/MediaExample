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
import com.media.core.ui.member.MCUMemberHandle;
import com.media.core.ui.member.P2PMemberHandle;
import com.media.core.ui.utils.PermissionUtil;
import com.media.rtc.MediaSDK;
import com.media.rtc.media.p2p.listener.P2PListener;
import com.media.rtc.media.p2p.listener.state.P2PState;
import com.media.rtc.webrtc.PeerFactoryHelper;
import com.web.socket.utils.StringUtils;

public class P2PActivity extends AppCompatActivity implements View.OnClickListener,
        P2PListener {

    private FrameLayout fLayoutLocalVideo, fLayoutRemoteVideo;
    private TextView tViewAnswer;
    private PeerFactoryHelper peerFactory;
    //记录所有成员
    private P2PMemberHandle p2pMemberHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p);
        p2pMemberHandle = new P2PMemberHandle();
        //监听状态
        MediaSDK.p2p().addListener(this);
        //申请权限
        PermissionUtil.isNeedRequestPermission(P2PActivity.this);
        initView();
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
        String client = getIntent().getStringExtra("client");
        if (StringUtils.isEmpty(client)) {
            tViewAnswer.setVisibility(View.VISIBLE);
        }
        try {
            MediaSDK.p2p().call(client);
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
        } else if (p2pState instanceof P2PState.Media) {
            P2PState.Media media = (P2PState.Media) p2pState;
            //自己的预览
            if (media.factory != null) {
                //如果存在先释放
                p2pMemberHandle.releaseLocal();
                this.peerFactory = media.factory;
                //预览自己
                if (media.factory.localMedia != null && media.factory.localMedia.localVideoTrack != null) {
                    Member member = p2pMemberHandle.createLocalMember(this, media.peer);
                    media.factory.localMedia.localVideoTrack.addSink(member.sink);
                    if (member.renderer != null) {
                        member.renderer.setZOrderOnTop(true);
                        fLayoutLocalVideo.addView(member.renderer);
                    }
                }
            } else {
                //如果存在先释放
                p2pMemberHandle.releaseRemote();
                //远程视频
                Member member = p2pMemberHandle.createRemoteMember(this, media.peer, media.stream);
                if (member.renderer != null) {
                    member.renderer.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    fLayoutRemoteVideo.addView(member.renderer);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有媒体显示（必须）
        p2pMemberHandle.release();
        //移除监听（必须）
        MediaSDK.p2p().removeListener(this);
        //释放呼叫（必须）
        MediaSDK.p2p().release();
    }
}

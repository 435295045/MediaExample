package com.media.core.ui.member;

import android.content.Context;

import com.media.rtc.webrtc.peer.Peer;
import com.media.rtc.webrtc.utils.ProxyVideoSink;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class P2PMemberHandle {
    public Member localMember;
    public Member remoteMember;

    public Member createLocalMember(Context context, Peer peer) {
        localMember = createMember(context, peer, null);
        return localMember;
    }

    public Member createRemoteMember(Context context, Peer peer, MediaStream stream) {
        remoteMember = createMember(context, peer, stream);
        return remoteMember;
    }

    private Member createMember(Context context, Peer peer, MediaStream stream) {
        SurfaceViewRenderer renderer = new SurfaceViewRenderer(context);
        renderer.init(peer.getEglBase().getEglBaseContext(), null);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderer.setMirror(true);
        // set render
        ProxyVideoSink sink = new ProxyVideoSink();
        sink.setTarget(renderer);
        Member member = new Member();
        member.peer = peer;
        member.renderer = renderer;
        member.sink = sink;
        if (stream != null && stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(sink);
        }
        return member;
    }

    public void releaseLocal() {
        if (localMember != null) {
            if (localMember.renderer != null) {
                localMember.renderer.release();
            }
            if (localMember.sink != null) {
                localMember.sink.setTarget(null);
            }
        }
    }

    public void releaseRemote() {
        if (remoteMember != null) {
            if (remoteMember.renderer != null) {
                remoteMember.renderer.release();
            }
            if (remoteMember.sink != null) {
                remoteMember.sink.setTarget(null);
            }
        }
    }

    public void release() {
        releaseLocal();
        releaseRemote();
    }
}

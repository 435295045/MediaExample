package com.media.core.ui.member;

import android.app.Activity;

import com.media.rtc.webrtc.peer.Peer;
import com.media.rtc.webrtc.utils.ProxyVideoSink;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MemberUtils {
    public static Member createMember(Activity activity, Peer peer, MediaStream stream) {
        SurfaceViewRenderer renderer = new SurfaceViewRenderer(activity.getApplication());
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
}

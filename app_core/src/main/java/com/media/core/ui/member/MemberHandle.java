package com.media.core.ui.member;

import android.content.Context;

import com.media.rtc.webrtc.peer.Peer;
import com.media.rtc.webrtc.utils.ProxyVideoSink;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class MemberHandle {
    //记录所有Member
    public final Hashtable<String, Member> memberHashtable;

    public MemberHandle() {
        memberHashtable = new Hashtable<>();
    }

    public Member getMember(String id) {
        return memberHashtable.get(id);
    }

    public void createMember(Context context, Peer peer, MediaStream stream) {
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
        memberHashtable.put(peer.id, member);
    }

    public void release(String id) {
        Member member = memberHashtable.get(id);
        if (member != null) {
            if (member.renderer != null) {
                member.renderer.release();
            }
            if (member.sink != null) {
                member.sink.setTarget(null);
            }
            memberHashtable.remove(id);
        }
    }

    public void release() {
        Iterator<Map.Entry<String, Member>> it = memberHashtable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Member> entry = it.next();
            if (entry.getValue().renderer != null) {
                entry.getValue().renderer.release();
            }
            if (entry.getValue().sink != null) {
                entry.getValue().sink.setTarget(null);
            }
            it.remove();
        }
    }
}

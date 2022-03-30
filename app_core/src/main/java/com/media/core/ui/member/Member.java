package com.media.core.ui.member;


import com.media.rtc.webrtc.peer.Peer;
import com.media.rtc.webrtc.utils.ProxyVideoSink;

import org.webrtc.SurfaceViewRenderer;

public class Member {
    public Peer peer;
    public SurfaceViewRenderer renderer;
    public ProxyVideoSink sink;
}

package com.media.core.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.media.rtc.MediaSDK;
import com.web.socket.utils.LoggerUtils;

import java.io.IOException;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback {
    private static final String TAG = "CameraHelper";
    private Camera mCamera;
    private int mCameraId;
    private Camera.Size previewSize;
    private int displayOrientation = 0;
    private int rotation;
    private int additionalRotation;
    private int[] cameraGlTextures = null;
    private SurfaceTexture cameraSurfaceTexture;
    private Handler handler;
    //用于判断摄像头是否出问题
    private int cameraError = 0;

    private static class Instance {
        private static final CameraHelper instance = new CameraHelper();
    }

    public static CameraHelper instance() {
        return Instance.instance;
    }

    public void init(Activity activity) {
        //防止多次初始化
        if (handler != null) {
            return;
        }
        //摄像头线程
        HandlerThread cameraThread = new HandlerThread("camera_thread");
        cameraThread.start();
        handler = new Handler(cameraThread.getLooper());
        //摄像头方向
        rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //初始化完成延迟两秒打开摄像头
                start();
            }
        }, 2 * 1000);
    }

    public void start() {
        //没有初始化，黑屏状态下不打开摄像头
        if (handler == null) {
            return;
        }
        //检测摄像头错误
        if (mCamera != null) {
            cameraError++;
            if (cameraError > 1) {
                stop();
            }
            return;
        }
        //打开摄像头
        startCamera();
    }

    private void startCamera() {
        handler.post(() -> {
            //相机数量为2则打开1,1则打开0,相机ID 1为前置，0为后置
            try {
                mCameraId = Camera.getNumberOfCameras() - 1;
                Log.e("Camera", "Camera mCameraId " + mCameraId);
                //没有相机
                if (mCameraId == -1) {
                    throw new Exception("没有找到可用摄像头!");
                }

                mCamera = Camera.open(mCameraId);
                displayOrientation = getFrameOrientation(rotation);
                mCamera.setDisplayOrientation(displayOrientation);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                //预览大小设置
                previewSize = parameters.getPreviewSize();
                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                if (supportedPreviewSizes != null && supportedPreviewSizes.size() > 0) {
                    //previewSize = getBestSupportedSize(supportedPreviewSizes);
                    previewSize = getBestMatchedCapability(supportedPreviewSizes);
                    LoggerUtils.e("previewSize: " + previewSize.width + "     " + previewSize.height);
                }
                parameters.setPreviewSize(previewSize.width, previewSize.height);

                //对焦模式设置
                List<String> supportedFocusModes = parameters.getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                }
                mCamera.setParameters(parameters);
                // No local renderer (we only care about onPreviewFrame() buffers, not a
                // directly-displayed UI element).  Camera won't capture without
                // setPreview{Texture,Display}, so we create a SurfaceTexture and hand
                // it over to Camera, but never listen for frame-ready callbacks,
                // and never call updateTexImage on it.
                cameraGlTextures = new int[1];
                // Generate one texture pointer and bind it as an external texture.
                GLES20.glGenTextures(1, cameraGlTextures, 0);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        cameraGlTextures[0]);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                cameraSurfaceTexture = new SurfaceTexture(cameraGlTextures[0]);
                cameraSurfaceTexture.setOnFrameAvailableListener(null);
                mCamera.setPreviewTexture(cameraSurfaceTexture);
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            } catch (Exception e) {
                LoggerUtils.e(e.getMessage(), e);
                stop();
            }
        });
    }

    private int getFrameOrientation(int rotation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
            rotation = 360 - rotation;
        }
        return (info.orientation + rotation) % 360;
    }

    public void stop() {
        cameraError = 0;
        stopCamera();
    }

    private void stopCamera() {
        handler.post(() -> {
            if (mCamera == null) return;
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (cameraGlTextures != null) {
                GLES20.glDeleteTextures(1, cameraGlTextures, 0);
                cameraGlTextures = null;
            }
            mCamera.release();
            mCamera = null;
            cameraSurfaceTexture = null;
        });
    }

    public void release() {
        stopCamera();
        previewSize = null;
    }

    private Camera.Size getBestMatchedCapability(List<Camera.Size> sizes) {
        Camera.Size size = null;
        int bestWidth = 0;
        int bestHeight = 0;
        Point specificPreviewSize = getVideoSize();
        for (int tmp = 0; tmp < sizes.size(); ++tmp) {
            Camera.Size capability = sizes.get(tmp);
            int diffWidth = capability.width - specificPreviewSize.x;
            int diffHeight = capability.height - specificPreviewSize.y;
            int currentbestDiffWith = bestWidth - specificPreviewSize.x;
            int currentbestDiffHeight = bestHeight - specificPreviewSize.y;
            if ((diffHeight >= 0 && diffHeight <= Math.abs(currentbestDiffHeight)) || (currentbestDiffHeight < 0 && diffHeight >= currentbestDiffHeight)) {
                if (diffHeight == currentbestDiffHeight) {
                    if ((diffWidth >= 0 && diffWidth <= Math.abs(currentbestDiffWith)) || (currentbestDiffWith < 0 && diffWidth >= currentbestDiffWith)) {
                        if (diffWidth == currentbestDiffWith && diffHeight == currentbestDiffHeight) {
                            bestWidth = capability.width;
                            bestHeight = capability.height;
                            size = capability;
                        } else {
                            bestWidth = capability.width;
                            bestHeight = capability.height;
                            size = capability;
                        }
                    }
                } else {
                    bestWidth = capability.width;
                    bestHeight = capability.height;
                    size = capability;
                }
            }// else height not good
        }
        if (size == null) {
            size = sizes.get(0);
        }
        return size;
    }

    @Override
    public void onPreviewFrame(byte[] nv21, Camera camera) {
        cameraError = 0;
        //webrtc
        MediaSDK.capturer().onData(displayOrientation, previewSize.width, previewSize.height, nv21);
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            // startCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: " + width + "  " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            // startCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LoggerUtils.e("----------------------------presentation:   surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LoggerUtils.e("----------------------------presentation:   surfaceDestroyed");
        }
    };

    private Point getVideoSize() {
        String videoSize = "vga";
        switch (videoSize) {
            case "qvga":
                return new Point(320, 240);
            case "qvga-h":
                return new Point(320, 240);
            case "cif":
                return new Point(352, 288);
            case "vga":
                return new Point(640, 480);
            case "vga-h":
                return new Point(640, 480);
            case "576p":
                return new Point(720, 576);
            case "576p-h":
                return new Point(720, 576);
            case "hd":
                return new Point(1280, 720);
            default:
                return new Point(640, 480);
        }
    }
}

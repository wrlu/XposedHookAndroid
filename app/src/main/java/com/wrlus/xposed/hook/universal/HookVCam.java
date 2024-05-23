package com.wrlus.xposed.hook.universal;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.polarxiong.videotoimages.OutputImageFormat;
import com.polarxiong.videotoimages.VideoToFrames;
import com.wrlus.xposed.framework.HookInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wrlu on 2024/3/13.
 */
public class HookVCam implements HookInterface {
    int frameCount = 0;
    static final Map<String, VideoToFrames> videoToFramesMap = new ConcurrentHashMap<>();
    static final Map<String, ConcurrentLinkedQueue<String>> savedFrameInfoMap =
            new ConcurrentHashMap<>();
    Surface displaySurface;
    Surface textureSurface;
    SurfaceTexture fakeTexture;

    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("android.hardware.Camera",
                loadPackageParam.classLoader,
                "setPreviewTexture", SurfaceTexture.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        SurfaceTexture surfaceTexture = (SurfaceTexture) param.args[0];
                        if (surfaceTexture != null && !surfaceTexture.equals(fakeTexture)) {
                            textureSurface = new Surface(surfaceTexture);
                            param.args[0] = new SurfaceTexture(10);
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.hardware.Camera",
                loadPackageParam.classLoader,
                "setPreviewDisplay", SurfaceHolder.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Camera thisCamera = (Camera) param.thisObject;
                        SurfaceHolder surfaceHolder = (SurfaceHolder) param.args[0];
                        if (surfaceHolder != null) {
                            displaySurface = surfaceHolder.getSurface();
                            fakeTexture = new SurfaceTexture(10);
                            // Give up setPreviewDisplay call and use setPreviewTexture instead.
                            thisCamera.setPreviewTexture(fakeTexture);
                        }
                        return null;
                    }
                });
        XposedHelpers.findAndHookMethod("android.hardware.Camera",
                loadPackageParam.classLoader,
                "startPreview", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        File videoFile = new File(Environment.getExternalStorageDirectory(),
                                "Android/data/" + loadPackageParam.packageName +
                                        "/files/ccc/virtual.mp4");
                        if (videoFile.exists()) {
                            if (displaySurface != null &&
                                    displaySurface.isValid()) {
                                playVideo(videoFile, displaySurface);
                            }
                            if (textureSurface != null &&
                                    textureSurface.isValid()) {
                                playVideo(videoFile, textureSurface);
                            }
                        } else {
                            XposedBridge.log("Video not exists!");
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.hardware.Camera",
                loadPackageParam.classLoader,
                "setPreviewCallback", PreviewCallback.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        PreviewCallback callback = (PreviewCallback) param.args[0];
                        if (callback != null) {
                            File videoFile = new File(Environment.getExternalStorageDirectory(),
                                    "Android/data/" + loadPackageParam.packageName +
                                            "/files/ccc/virtual.mp4");
                            if (videoFile.exists()) {
                                decodeVideo(videoFile, loadPackageParam.packageName);
                                hookPreviewCallback(callback, videoFile,
                                        loadPackageParam.packageName);
                            } else {
                                XposedBridge.log("Video not exists!");
                            }
                        } else {
                            if (displaySurface != null) displaySurface.release();
                            if (textureSurface != null) textureSurface.release();
                            frameCount = 0;
                            XposedBridge.log("Camera.setPreviewCallback: " +
                                    "callback is null, skip.");
                        }
                    }
                });
    }

    private void hookPreviewCallback(PreviewCallback callback, File videoFile,
                                     String packageName) {
        File dumpFrameOutput = new File(
                Environment.getExternalStorageDirectory(),
                "Android/data/" + packageName +
                        "/files/dump_frame_" + UUID.randomUUID() + "/");
        if (!dumpFrameOutput.exists()) {
            XposedBridge.log("dump frame output mkdir: " +
                    dumpFrameOutput.mkdir());
        }
        Class<?> cbClass = callback.getClass();
        XposedBridge.log("Callback class name: " + cbClass.getName());
        XposedHelpers.findAndHookMethod(cbClass,
                "onPreviewFrame", byte[].class, Camera.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log("Before onPreviewFrame");
                        Camera camera = (Camera) param.args[1];
                        Camera.Size previewSize = camera
                                .getParameters().getPreviewSize();
                        byte[] newData = getReplacedPreviewFrame(videoFile, packageName);
                        if (newData != null) {
                            // We need exchange width and height for rotation.
                            int videoWidth = previewSize.height;
                            int videoHeight = previewSize.width;
                            byte[] rotateData = rotateNV21(newData,
                                    videoWidth, videoHeight, 90);
                            param.args[0] = rotateData;
                        } else {
                            // We do not want to leak real camera data here.
                            param.args[0] = null;
                            XposedBridge.log("Replace " +
                                    "onPreviewFrame data failed !!!");
                        }
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        XposedBridge.log("After onPreviewFrame");
                        byte[] data = (byte[]) param.args[0];
                        Camera camera = (Camera) param.args[1];
                        Camera.Size previewSize = camera
                                .getParameters().getPreviewSize();
                        savePreviewFrameImage(data,
                                previewSize.width, previewSize.height,
                                dumpFrameOutput);
                    }
                });
    }

    private void playVideo(File videoFile, Surface surface) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setSurface(surface);
        mediaPlayer.setVolume(0, 0);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
        try {
            mediaPlayer.setDataSource(videoFile.getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            XposedBridge.log(e);
        }
    }

    private void decodeVideo(File videoFile, String pkgName) {
        String mapKey = pkgName + "_" + videoFile.getAbsolutePath();
        if (!videoToFramesMap.containsKey(mapKey)) {
            File outputFolder = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/" + pkgName + "/files/decode_video_" + UUID.randomUUID() + "/");
            VideoToFrames videoToFrames = new VideoToFrames();
            videoToFrames.setSaveFrames(outputFolder.getAbsolutePath(), OutputImageFormat.NV21);
            videoToFrames.setCallback(new VideoToFrames.Callback() {
                @Override
                public void onDecodeFrameToFile(int index, String fileName) {
                    ConcurrentLinkedQueue<String> infoQueue;
                    synchronized (savedFrameInfoMap) {
                        if (!savedFrameInfoMap.containsKey(mapKey)) {
                            infoQueue = new ConcurrentLinkedQueue<>();
                            savedFrameInfoMap.put(mapKey, infoQueue);
                        } else {
                            infoQueue = savedFrameInfoMap.get(mapKey);
                        }
                    }
                    if (infoQueue != null) {
                        infoQueue.add(fileName);
                    }
                }

                @Override
                public void onFinishDecode() {
                    XposedBridge.log("onFinishDecode: finish decode video: " +
                            videoFile.getAbsolutePath() + ", to output path: " +
                            outputFolder.getAbsolutePath());
                }
            });
            videoToFrames.decode(videoFile.getAbsolutePath());
            videoToFramesMap.put(mapKey, videoToFrames);
        }
    }

    private byte[] getReplacedPreviewFrame(File videoFile, String pkgName) {
        String mapKey = pkgName + "_" + videoFile.getAbsolutePath();
        ConcurrentLinkedQueue<String> infoQueue = null;
        synchronized (savedFrameInfoMap) {
            if (savedFrameInfoMap.containsKey(mapKey)) {
                infoQueue = savedFrameInfoMap.get(mapKey);
            } else {
                XposedBridge.log("replacePreviewFrame: " +
                        "cannot find saved frame queue.");
            }
        }
        if (infoQueue != null) {
            String savedFrameFileName = infoQueue.poll();
            if (savedFrameFileName != null) {
                infoQueue.add(savedFrameFileName);
                return readFile(savedFrameFileName);
            }
        } else {
            XposedBridge.log("replacePreviewFrame: " +
                    "found null saved frame queue.");
        }
        return null;
    }

    public static byte[] rotateNV21(byte[] yuv, int width, int height, int rotation) {
        if (rotation == 0) return yuv;
        if (rotation % 90 != 0 || rotation < 0 || rotation > 270) {
            throw new IllegalArgumentException("0 <= rotation < 360, rotation % 90 == 0");
        }

        final byte[]  output    = new byte[yuv.length];
        final int     frameSize = width * height;
        final boolean swap      = rotation % 180 != 0;
        final boolean xflip     = rotation % 270 != 0;
        final boolean yflip     = rotation >= 180;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                final int yIn = j * width + i;
                final int uIn = frameSize + (j >> 1) * width + (i & ~1);
                final int vIn = uIn       + 1;

                final int wOut     = swap  ? height              : width;
                final int hOut     = swap  ? width               : height;
                final int iSwapped = swap  ? j                   : i;
                final int jSwapped = swap  ? i                   : j;
                final int iOut     = xflip ? wOut - iSwapped - 1 : iSwapped;
                final int jOut     = yflip ? hOut - jSwapped - 1 : jSwapped;

                final int yOut = jOut * wOut + iOut;
                final int uOut = frameSize + (jOut >> 1) * wOut + (iOut & ~1);
                final int vOut = uOut + 1;

                output[yOut] = (byte)(0xff & yuv[yIn]);
                output[uOut] = (byte)(0xff & yuv[uIn]);
                output[vOut] = (byte)(0xff & yuv[vIn]);
            }
        }
        return output;
    }

    private byte[] readFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            int size = (int) Files.size(path);
            byte[] data = new byte[size];
            FileInputStream fis = new FileInputStream(filePath);
            int readSize = fis.read(data);
            if (readSize != size) {
                XposedBridge.log("readFile: readSize != size");
            }
            fis.close();
            return data;
        } catch (IOException e) {
            XposedBridge.log(e);
        }
        return null;
    }

    private void savePreviewFrameImage(byte[] data, int width, int height, File dumpFrameOutput) {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                width, height, null);
        try {
            FileOutputStream fos = new FileOutputStream(
                    new File(dumpFrameOutput, frameCount++ + ".jpeg"));
            yuvImage.compressToJpeg(new Rect(0, 0, width, height)
                    , 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            XposedBridge.log(e);
        }
    }
}

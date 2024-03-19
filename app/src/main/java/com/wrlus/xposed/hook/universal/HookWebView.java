package com.wrlus.xposed.hook.universal;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;
import com.wrlus.xposed.framework.PrintCallback.HookTime;
import com.wrlus.xposed.framework.PrintCallback.PrintPolicy;
import com.wrlus.xposed.util.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Map;

public class HookWebView implements HookInterface {
    private static final String TAG = "HookWebView";
    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            hookAndroidWebView(loadPackageParam.classLoader);
        } catch (Exception e) {
            Log.e(TAG, "Hook error.", e);
            e.printStackTrace();
        }
    }

    public static void hookAndroidWebView(ClassLoader classLoader) {
        MethodHook loadUrlHooker = new MethodHook.Builder(WebView.class)
                .setMethodName("loadUrl")
                .addParameter(String.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(HookTime.AFTER)
                        .addParameterPrint("url")
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook loadUrlHooker2 = new MethodHook.Builder(WebView.class)
                .setMethodName("loadUrl")
                .addParameter(String.class)
                .addParameter(Map.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(HookTime.AFTER)
                        .addParameterPrint("url")
                        .addParameterPrint("additionalHttpHeaders")
                        .setTag(TAG)
                        .build())
                .build();

        MethodHook getUrlHooker =  new MethodHook.Builder(WebView.class)
                .setMethodName("getUrl")
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(HookTime.AFTER)
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook addJavascriptInterfaceHooker = new MethodHook.Builder(WebView.class)
                .setMethodName("addJavascriptInterface")
                .addParameter(Object.class)
                .addParameter(String.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(HookTime.AFTER)
                        .addParameterPrint("object", PrintPolicy.CLASSNAME)
                        .addParameterPrint("name")
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook removeJavascriptInterfaceHooker = new MethodHook.Builder(WebView.class)
                .setMethodName("removeJavascriptInterface")
                .addParameter(String.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(HookTime.AFTER)
                        .addParameterPrint("name")
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook setWebViewClientHooker = new MethodHook.Builder(WebView.class)
                .setMethodName("setWebViewClient")
                .addParameter(WebViewClient.class)
                .setCallback(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        WebViewClient client = (WebViewClient) param.args[0];
                        String className = Utils.checkNonNull(client).getClass().getName();
                        Log.i(TAG, "setWebViewClient: client = " +
                                className);
                        MethodHook shouldOverrideUrlLoadingHooker = new MethodHook.Builder(className, classLoader)
                                .setMethodName("shouldOverrideUrlLoading")
                                .addParameter(WebView.class)
                                .addParameter(WebResourceRequest.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addParameterPrint("webView",
                                                PrintPolicy.CLASSNAME)
                                        .addSpecParameterPrint("request",
                                                WebResourceRequest.class
                                                        .getMethod("getUrl"))
                                        .setTag(TAG)
                                        .build())
                                .build();
                        shouldOverrideUrlLoadingHooker.install();
                    }
                }).build();
        MethodHook setWebChromeClientHooker = new MethodHook.Builder(WebView.class)
                .setMethodName("setWebChromeClient")
                .addParameter(WebChromeClient.class)
                .setCallback(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        WebChromeClient client = (WebChromeClient) param.args[0];
                        String className = Utils.checkNonNull(client).getClass().getName();
                        Log.i(TAG, "setWebChromeClient: client = " +
                                className);
                        MethodHook onConsoleMessageHooker = new MethodHook.Builder(className, classLoader)
                                .setMethodName("onConsoleMessage")
                                .addParameter(ConsoleMessage.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addSpecParameterPrint("message",
                                                ConsoleMessage.class.getMethod("message"))
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook onConsoleMessageHooker2 = new MethodHook.Builder(className, classLoader)
                                .setMethodName("onConsoleMessage")
                                .addParameter(String.class)
                                .addParameter(int.class)
                                .addParameter(String.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addParameterPrint("message")
                                        .addParameterPrint("lineNumber")
                                        .addParameterPrint("sourceID")
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook onJsAlertHooker = new MethodHook.Builder(className, classLoader)
                                .setMethodName("onJsAlert")
                                .addParameter(WebView.class)
                                .addParameter(String.class)
                                .addParameter(String.class)
                                .addParameter(JsResult.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addParameterPrint("webView",
                                                PrintPolicy.CLASSNAME)
                                        .addParameterPrint("url")
                                        .addParameterPrint("message")
                                        .addParameterPrint("result",
                                                PrintPolicy.CLASSNAME)
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook onJsConfirmHooker = new MethodHook.Builder(className, classLoader)
                                .setMethodName("onJsConfirm")
                                .addParameter(WebView.class)
                                .addParameter(String.class)
                                .addParameter(String.class)
                                .addParameter(JsResult.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addParameterPrint("webView",
                                                PrintPolicy.CLASSNAME)
                                        .addParameterPrint("url")
                                        .addParameterPrint("message")
                                        .addParameterPrint("result",
                                                PrintPolicy.CLASSNAME)
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook onJsPromptHooker = new MethodHook.Builder(className, classLoader)
                                .setMethodName("onJsPrompt")
                                .addParameter(WebView.class)
                                .addParameter(String.class)
                                .addParameter(String.class)
                                .addParameter(JsPromptResult.class)
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(HookTime.AFTER)
                                        .addParameterPrint("webView",
                                                PrintPolicy.CLASSNAME)
                                        .addParameterPrint("url")
                                        .addParameterPrint("message")
                                        .addParameterPrint("result",
                                                PrintPolicy.CLASSNAME)
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook.normalInstall(
                                onConsoleMessageHooker, onConsoleMessageHooker2,
                                onJsAlertHooker, onJsConfirmHooker, onJsPromptHooker);
                    }
                }).build();

        MethodHook.normalInstall(loadUrlHooker, loadUrlHooker2, getUrlHooker,
                addJavascriptInterfaceHooker, removeJavascriptInterfaceHooker,
                setWebViewClientHooker, setWebChromeClientHooker);
    }
}

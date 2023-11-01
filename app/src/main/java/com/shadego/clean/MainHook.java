package com.shadego.clean;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;
import java.util.List;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 过滤不必要的应用
        if (lpparam.packageName.equals("com.cainiao.wireless")){
            // 菜鸟裹裹
            hookCaiNiao(lpparam);
        }else if (lpparam.packageName.equals("tv.danmaku.bili")){
            // 哔哩哔哩
            hookBili(lpparam);
        }
    }

    private void hookBili(XC_LoadPackage.LoadPackageParam lpparam){
        Class<?> mainActivity = XposedHelpers.findClass("tv.danmaku.bili.MainActivityV2", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(mainActivity,"S9","tv.danmaku.bili.ui.splash.ad.model.Splash",boolean.class,new XC_MethodHook(){
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Toast.makeText((Activity) param.thisObject, "模块加载成功！", Toast.LENGTH_SHORT).show();
                param.args[0]=null;
            }
        });
    }

    private void hookCaiNiao(XC_LoadPackage.LoadPackageParam lpparam) {
        // 具体流程
        //去除摇一摇广告
        Class<?> mmAdSdkUtil = XposedHelpers.findClass("com.cainiao.wireless.utils.MmAdSdkUtil", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(mmAdSdkUtil, "isMmSdkEnable", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return false;
            }
        });
        XposedHelpers.findAndHookMethod(mmAdSdkUtil, "isRtbAdEnable", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return false;
            }
        });
        //去除积分等滚动栏
        Class<?> CubeXLinearLayoutFragment = XposedHelpers.findClass("com.cainiao.wireless.cubex.mvvm.view.CubeXLinearLayoutFragment", lpparam.classLoader);
        for (Method method : CubeXLinearLayoutFragment.getDeclaredMethods()) {
            if("setEmpty".equals(method.getName())){
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        List<Object> jsonArray = (List<Object>) param.args[1];
                        Object temp = jsonArray.get(0);
                        jsonArray.clear();
                        jsonArray.add(temp);
                    }
                });
            }
        }
        //去除底部发现、裹裹券
        Class<?> homePageActivity = XposedHelpers.findClass("com.cainiao.wireless.homepage.view.activity.HomePageActivity", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(homePageActivity,"onCreate",Bundle.class,new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                Resources resources = activity.getResources();
                int id = resources.getIdentifier("ll_navigation_tab_layout", "id", lpparam.packageName);
                LinearLayout view = activity.findViewById(id);
                view.getChildAt(1).setVisibility(View.GONE);
                view.getChildAt(2).setVisibility(View.GONE);
            }
        });
        //去除最下方滚动推荐
        XposedHelpers.findAndHookMethod("com.cainiao.wireless.recommend.CNRecommendView",lpparam.classLoader,"initView",new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup viewGroup = (ViewGroup) param.thisObject;
                Resources resources = viewGroup.getResources();
                int id = resources.getIdentifier("recommend_view_root", "id", lpparam.packageName);
                View view = viewGroup.findViewById(id);
                view.setVisibility(View.GONE);
            }
        });
    }
}

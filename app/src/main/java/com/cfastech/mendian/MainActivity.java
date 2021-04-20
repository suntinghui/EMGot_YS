package com.cfastech.mendian;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title bar  即隐藏标题栏
        getSupportActionBar().hide();// 隐藏ActionBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//remove notification bar  即全屏
        setContentView(R.layout.activity_main);

        if (this.checkPrivacy()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);

                        // 启动Uni小程序
                        MainActivity.this.startUniApp();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            this.showPrivacyDialog();
        }
    }

    private void showPrivacyDialog() {
        String privacyStr = "<p style=\"font-size:16px; text-indent:2em;\">感谢您使用易商App。为了更好地保护您的权益，" +
                "请在使用前充分阅读并理解<a href=\"https://qbhb.emgot.com/service_ys.html\" style=\"color: #0081ff; display:inline;\">《用户服务协议》</a>" +
                "和<a href=\"https://qbhb.emgot.com/privacy_ys.html\" style=\"color: #0081ff; display:inline;\">《隐私政策》</a>，" +
                "点击“同意”按钮代表你已同意前述协议及约定。</p>" +
                "<p>1、在仅浏览时，我们可能会申请系统设备权限收集设备信息、日志信息，用于信息推送和安全风控，并申请存储权限，用于下载及缓存相关文件。</p>" +
                "<p>2、我们可能会申请位置权限，用于向您推荐您可能感兴趣的附近的门店信息及确定邮费，城市、区县位置无需使用位置权限，仅通过IP地址确定相关功能中所展示的城市信息，不会收集精确位置信息。</p>" +
                "<p>3、上述权限以及摄像头、麦克风、相册、存储空间、GPS等敏感权限均不会默认或强制开启收集信息。</p>" +
                "<p>4、我们可能会收集设备MAC地址和软件安装应用列表，主要用于提供移动端设备推送服务及辅助用户实现打开第三方应用（淘宝、京东）分享或购买商品。我们承诺不会记录或上传您的个人信息用于任何非法目的。</p>" +
                "<p>5、为实现信息分享、第三方登录、参加相关活动、综合统计分析等目的所必需，我们可能会调用剪切板并使用与功能相关的最小必要信息（口令、链接、统计参数）。</p>";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("个人信息保护指引");
        builder.setMessage(Html.fromHtml(privacyStr));
        //点击对话框以外的区域是否让对话框消失
        builder.setCancelable(false);
        //设置正面按钮
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.savePrivacy(true);
                MainActivity.this.startUniApp();
                dialog.dismiss();
            }
        });

        //设置反面按钮
        builder.setNegativeButton("暂不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.savePrivacy(false);
                MainActivity.this.exitApp();
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // 设置AlertDialog的宽、高限制
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6);   //高度设置为屏幕的0.3
        p.width = (int) (d.getWidth() * 0.95);    //宽度设置为屏幕的0.5
        dialog.getWindow().setAttributes(p);     //设置生效

        // 设置隐私协议内容可点击
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void startUniApp() {

        DCSDKInitConfig config = new DCSDKInitConfig.Builder()
                .setCapsule(false) // 设置是否使用胶囊按钮
                .setEnableBackground(false) // true表示小程序退出时进入后台 false表示直接退出
                .build();

        DCUniMPSDK.getInstance().initialize(this, config, new DCUniMPSDK.IDCUNIMPPreInitCallback() {
            @Override
            public void onInitFinished(boolean b) {
                try {
                    DCUniMPSDK.getInstance().startApp(MainActivity.this, "__UNI__F802314");
                    // 监听Uni小程序退出事件
                    MainActivity.this.regUniCloseListener();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void regUniCloseListener() {
        // 小程序被关闭事件监听
        DCUniMPSDK.getInstance().setUniMPOnCloseCallBack(new DCUniMPSDK.IUniMPOnCloseCallBack(){
            @Override
            public void onClose(String s) {
                MainActivity.this.exitApp();
            }
        });
    }

    // 检查当前隐私协议同意情况
    private boolean checkPrivacy() {
        SharedPreferences pref = getSharedPreferences("EMGOT_YS", MODE_PRIVATE);
        boolean isPrivacy = pref.getBoolean("PRIVACY",false);
        return isPrivacy;
    }

    // 保存当前隐私协议状态
    private void savePrivacy(boolean privacyState) {
        SharedPreferences.Editor editor = getSharedPreferences("EMGOT_YS",MODE_PRIVATE).edit();
        editor.putBoolean("PRIVACY", privacyState);
        editor.apply();
    }

    private void exitApp() {
        this.finish();
    }
}
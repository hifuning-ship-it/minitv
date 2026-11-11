package net.minimaltvlite.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Tiny Android 4.4-compatible, network-free, remote-control-focused TV home. */
public class MainActivity extends Activity {
    private final int bg = Color.rgb(12, 16, 22);
    private final int tile = Color.rgb(33, 41, 52);
    private final int accent = Color.rgb(62, 122, 224);
    private LinearLayout root;
    private boolean showingApps;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        showHome();
    }

    private int dp(float value) { return (int)(getResources().getDisplayMetrics().density * value + .5f); }

    private GradientDrawable rounded(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(12));
        return d;
    }

    private void initLayout(String subtitle) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(34), dp(24), dp(34), dp(22));
        root.setBackgroundColor(bg);
        setContentView(root);
        TextView header = new TextView(this);
        header.setText("极简电视  /  Minimal TV Lite");
        header.setTextSize(25); header.setTextColor(Color.WHITE);
        header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(header);
        TextView hint = new TextView(this);
        hint.setText(subtitle);
        hint.setTextSize(13); hint.setTextColor(Color.rgb(170, 184, 203));
        LinearLayout.LayoutParams h = new LinearLayout.LayoutParams(-1, -2);
        h.topMargin = dp(7); h.bottomMargin = dp(22);
        root.addView(hint, h);
    }

    private Button button(String label, final Runnable action) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(18); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setGravity(Gravity.CENTER);
        b.setBackground(rounded(tile));
        b.setFocusable(true); b.setFocusableInTouchMode(false);
        b.setPadding(dp(6), dp(8), dp(6), dp(8));
        b.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focus) {
                view.setBackground(rounded(focus ? accent : tile));
                view.setScaleX(focus ? 1.035f : 1f);
                view.setScaleY(focus ? 1.035f : 1f);
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { action.run(); }
        });
        return b;
    }

    private void showHome() {
        showingApps = false;
        initLayout("仅保留常用入口  ·  方向键选择  ·  确认键打开  ·  原厂桌面随时可返回");
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        root.addView(grid);
        add(grid, "电视 / 信号源", new Runnable() { public void run() { openFactory(); } });
        add(grid, "本地视频", new Runnable() { public void run() { openPackage("com.yunos.app.videoplayer"); } });
        add(grid, "无线投屏", new Runnable() { public void run() { openPackage("com.rtk.android.miracast"); } });
        add(grid, "全部应用", new Runnable() { public void run() { showApps(); } });
        add(grid, "系统设置", new Runnable() { public void run() { openSettings(); } });
        add(grid, "原厂桌面", new Runnable() { public void run() { openFactory(); } });
        grid.getChildAt(0).requestFocus();
        TextView footer = new TextView(this);
        footer.setText("测试版：不会停用或删除任何原厂组件。电视 / 信号源暂通过原厂桌面访问。");
        footer.setTextSize(12); footer.setTextColor(Color.rgb(145, 160, 177));
        LinearLayout.LayoutParams f = new LinearLayout.LayoutParams(-1, -2);
        f.topMargin = dp(22); root.addView(footer, f);
    }

    private void add(GridLayout grid, String text, Runnable task) {
        Button b = button(text, task);
        GridLayout.LayoutParams p = new GridLayout.LayoutParams();
        p.width = dp(190); p.height = dp(94);
        p.setMargins(0, 0, dp(14), dp(14));
        grid.addView(b, p);
    }

    private void openPackage(String name) {
        Intent i = getPackageManager().getLaunchIntentForPackage(name);
        if (i == null) {
            Toast.makeText(this, "未找到可直接启动的入口；请通过原厂桌面打开", Toast.LENGTH_LONG).show();
            return;
        }
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "启动失败：" + name, Toast.LENGTH_LONG).show(); }
    }

    private void openFactory() {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.yunos.tv.homeshell", "com.yunos.tv.homeshell.activity.MainActivity"));
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "原厂桌面无法打开，请用 ADB 恢复", Toast.LENGTH_LONG).show(); }
    }

    private void openSettings() {
        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        catch (Exception e) { openPackage("com.android.settings"); }
    }

    private void showApps() {
        showingApps = true;
        initLayout("应用列表  ·  返回键返回首页");
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        Button back = button("← 返回首页", new Runnable() { public void run() { showHome(); } });
        list.addView(back, listParams());
        List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>(getPackageManager().getInstalledApplications(0));
        Collections.sort(apps, new Comparator<ApplicationInfo>() {
            @Override public int compare(ApplicationInfo a, ApplicationInfo b) {
                return a.packageName.compareToIgnoreCase(b.packageName);
            }
        });
        final PackageManager pm = getPackageManager();
        for (final ApplicationInfo app : apps) {
            if (app.packageName.equals(getPackageName())) continue;
            if (pm.getLaunchIntentForPackage(app.packageName) == null) continue;
            CharSequence label = pm.getApplicationLabel(app);
            Button b = button(label + "  (" + app.packageName + ")", new Runnable() {
                public void run() { openPackage(app.packageName); }
            });
            b.setTextSize(14); list.addView(b, listParams());
        }
        back.requestFocus();
    }

    private LinearLayout.LayoutParams listParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(55));
        p.bottomMargin = dp(8); return p;
    }

    @Override public void onBackPressed() {
        if (showingApps) showHome(); else super.onBackPressed();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && !showingApps) { showApps(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}

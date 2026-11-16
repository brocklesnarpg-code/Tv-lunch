package com.example.glassylauncher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(8,19,33);
    private final int WHITE = Color.rgb(239,247,255);
    private final Handler handler = new Handler();
    private LinearLayout appGrid;
    private EditText search;
    private TextView clock, date, count;
    private final ArrayList<AppEntry> apps = new ArrayList<>();
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            clock.setText(new SimpleDateFormat("h:mm", Locale.getDefault()).format(now));
            date.setText(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now));
            handler.postDelayed(this, 1000);
        }
    };

    static class AppEntry {
        String label, pkg;
        android.graphics.drawable.Drawable icon;
        AppEntry(String l, String p, android.graphics.drawable.Drawable i) { label=l; pkg=p; icon=i; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildScreen();
        loadApps();
        renderApps("");
        ticker.run();
    }

    private GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius);
        if (stroke != 0) d.setStroke(2, stroke);
        return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size);
        t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(34), dp(22), dp(34), dp(28));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(13,35,58), Color.rgb(8,19,33), Color.rgb(21,27,54)});
        root.setBackground(bg);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(94)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("GLASSY", 27, WHITE, true);
        title.setLetterSpacing(0.12f);
        brand.addView(title);
        TextView sub = text("YOUR SPACE. YOUR APPS.", 10, 0xFF9AB5CA, true);
        sub.setLetterSpacing(0.16f);
        brand.addView(sub);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout timePill = new LinearLayout(this);
        timePill.setGravity(Gravity.CENTER_VERTICAL);
        timePill.setOrientation(LinearLayout.VERTICAL);
        timePill.setPadding(dp(22), dp(10), dp(22), dp(10));
        timePill.setBackground(shape(0x332B526C, 0x5579C8E8, dp(22)));
        clock = text("--:--", 25, WHITE, true);
        date = text("Loading date", 11, 0xFFB5C9D8, false);
        timePill.addView(clock); timePill.addView(date);
        top.addView(timePill);

        TextView heading = text("Your applications", 23, WHITE, true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(18); hp.bottomMargin = dp(12);
        root.addView(heading, hp);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        searchRow.setPadding(dp(16), 0, dp(16), 0);
        searchRow.setBackground(shape(0x332C526C, 0x5575B7D5, dp(16)));
        TextView magnify = text("⌕", 27, 0xFFBFEAFF, true);
        searchRow.addView(magnify);
        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(0xFF9BB2C5);
        search.setHint("Search your apps");
        search.setTextSize(15);
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setPadding(dp(10), dp(13), dp(8), dp(13));
        searchRow.addView(search, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(searchRow, new LinearLayout.LayoutParams(-1, dp(52)));

        count = text("Loading apps…", 12, 0xFF9CB4C7, false);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(18); cp.bottomMargin = dp(12);
        root.addView(count, cp);

        appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);
        root.addView(appGrid, new LinearLayout.LayoutParams(-1, -2));

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { renderApps(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        LinkedHashMap<String, AppEntry> found = new LinkedHashMap<>();
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String category : cats) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(category);
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            for (ResolveInfo ri : list) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue;
                String label = ri.loadLabel(pm).toString();
                found.put(pkg, new AppEntry(label, pkg, ri.loadIcon(pm)));
            }
        }
        apps.clear(); apps.addAll(found.values());
        Collections.sort(apps, (a,b) -> a.label.compareToIgnoreCase(b.label));
    }

    private void renderApps(String query) {
        if (appGrid == null) return;
        appGrid.removeAllViews();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        ArrayList<AppEntry> shown = new ArrayList<>();
        for (AppEntry a : apps) if (a.label.toLowerCase(Locale.getDefault()).contains(q)) shown.add(a);
        count.setText(shown.size() + (shown.size()==1 ? " APP" : " APPS"));
        if (shown.isEmpty()) {
            TextView empty = text(apps.isEmpty() ? "No launchable apps found." : "No apps match your search.", 16, 0xFFB7CAD8, false);
            empty.setPadding(dp(8), dp(25), dp(8), dp(25)); appGrid.addView(empty); return;
        }
        int columns = 5;
        for (int start=0; start<shown.size(); start+=columns) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(126));
            rp.bottomMargin = dp(12); appGrid.addView(row, rp);
            for (int col=0; col<columns; col++) {
                int index = start+col;
                if (index >= shown.size()) {
                    View spacer = new View(this);
                    row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
                    continue;
                }
                AppEntry app = shown.get(index);
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setGravity(Gravity.CENTER);
                card.setFocusable(true); card.setFocusableInTouchMode(true);
                card.setPadding(dp(6), dp(10), dp(6), dp(8));
                card.setBackground(shape(0x332C526C, 0x337AAAC4, dp(18)));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
                lp.setMargins(dp(5), 0, dp(5), 0);
                row.addView(card, lp);
                ImageView icon = new ImageView(this);
                icon.setImageDrawable(app.icon);
                card.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
                TextView name = text(app.label, 12, WHITE, true);
                name.setGravity(Gravity.CENTER);
                name.setMaxLines(2);
                LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
                np.topMargin = dp(8); card.addView(name, np);
                card.setOnFocusChangeListener((v, focused) -> {
                    v.setBackground(shape(focused ? 0x66518FB2 : 0x332C526C,
                        focused ? 0xFF9CEBFF : 0x337AAAC4, dp(18)));
                    v.setScaleX(focused ? 1.06f : 1f);
                    v.setScaleY(focused ? 1.06f : 1f);
                });
                card.setOnClickListener(v -> {
                    Intent launch = pmLaunch(app.pkg);
                    if (launch != null) try { startActivity(launch); } catch (Exception ignored) {}
                });
            }
        }
    }

    private Intent pmLaunch(String pkg) {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) {
            i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            i.setPackage(pkg);
            List<ResolveInfo> matches = getPackageManager().queryIntentActivities(i, 0);
            if (!matches.isEmpty()) {
                i.setClassName(pkg, matches.get(0).activityInfo.name);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else return null;
        }
        return i;
    }

    @Override protected void onResume() {
        super.onResume();
        if (appGrid != null) { loadApps(); renderApps(search == null ? "" : search.getText().toString()); }
    }
    @Override protected void onDestroy() {
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }
}

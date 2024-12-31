package awais.taskill.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import awais.taskill.BuildConfig;
import awais.taskill.R;
import awais.taskill.databinding.DialogWidgetSettingsBinding;
import awais.taskill.tools.PackagesHelper;
import awais.taskill.tools.SettingsHelper;
import awais.taskill.tools.Utils;
import awais.taskill.widgets.KillerWidget;

public final class ShortcutActivity extends Activity {
    private final AtomicBoolean performedAction = new AtomicBoolean(false);
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public Window getWindow() {
        Context context = getApplicationContext();
        if (context == null) context = getBaseContext();
        doStuff(context);
        return super.getWindow();
    }

    private void doStuff(final Context context) {
        final Intent intent = getIntent();
        if (context == null || intent == null || performedAction.get()) return;

        final String intentAction = intent.getAction();

        if (true) Log.d("AWAISKING_APP", "---> shortcut::intent: " + intent
                                         + "\n" + getCallingPackage()
                                         + "\n" + performedAction
                       );
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intentAction)) {
            performedAction.set(true);
            doWidgetStuff(intent);
            return;
        }

        final boolean killIgnoringExceptions = "awais.action.KILLALL_IGNORED".equals(intentAction);
        if ("awais.action.KILLALL".equals(intentAction) || killIgnoringExceptions) {
            performedAction.set(true);
            killAllApps(context, killIgnoringExceptions);
        }

        finishAndRemoveTask();
    }

    private void doWidgetStuff(@NonNull Intent intent) {
        setResult(RESULT_CANCELED);

        final Bundle extras = intent.getExtras();
        appWidgetId = extras == null ? appWidgetId : extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finishAndRemoveTask();
            return;
        }

        final SettingsHelper settingsHelper = SettingsHelper.getInstance(this);
        final DialogWidgetSettingsBinding settingsBinding = DialogWidgetSettingsBinding.inflate(getLayoutInflater());
        final RadioGroup bindingRoot = settingsBinding.getRoot();

        bindingRoot.check(settingsHelper.getWidgetKillIgnoring() ? R.id.rbKillAllIgnore : R.id.rbKillAll);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton("OK", (d, w) -> {
            settingsHelper.setWidgetKillIgnoring(bindingRoot.getCheckedRadioButtonId() == R.id.rbKillAllIgnore);

            KillerWidget.updateWidget(this, appWidgetId);

            setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));
        }).setView(bindingRoot).setTitle("Set force stop mode").setOnDismissListener(dlg -> finishAndRemoveTask()).show();

        makeSystemBarsTranslucent(getWindow());
        makeSystemBarsTranslucent(alertDialog.getWindow());
    }

    private void killAllApps(Context context, final boolean killIgnoringExceptions) {
        final PackagesHelper packagesHelper = PackagesHelper.getInstance(context);
        final List<PackageInfo> packageInfos = packagesHelper.getAllPackages();

        if (packageInfos.isEmpty()) return;

        final Set<String> excludedPackages = packagesHelper.getExcludedPackages();

        Shell shell = Shell.getCachedShell();
        if (shell == null) shell = Shell.getShell();
        final Shell.Job job = shell.newJob();

        job.add("am kill-all");
        for (final PackageInfo pkgInfo : packageInfos) {
            if (pkgInfo == null) continue;

            boolean canKill = killIgnoringExceptions || !Utils.isExcluded(excludedPackages, pkgInfo);
            if (canKill && pkgInfo.applicationInfo != null)
                canKill = (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) != ApplicationInfo.FLAG_STOPPED;

            if (canKill) job.add("am force-stop " + pkgInfo.packageName);
        }
        job.exec();

        shell.newJob().add("am force-stop " + BuildConfig.APPLICATION_ID).submit();
    }

    @Override
    protected void onStop() {
        try {performedAction.set(false);} catch (Exception ignore) {}
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {performedAction.set(false);} catch (Exception ignore) {}
        super.onDestroy();
    }

    @SuppressLint("RestrictedApi")
    private static void makeSystemBarsTranslucent(Window window) {
        if (window == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false);
        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        final View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
package awais.taskill.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Set;

import awais.taskill.tools.PackagesHelper;
import awais.taskill.tools.Utils;

public final class ShortcutActivity extends Activity {
    @Override
    public Context getBaseContext() {
        final Context baseContext = super.getBaseContext();
        killAllApps(baseContext);
        return baseContext;
    }

    public void killAllApps(final Context context) {
        final Intent intent = getIntent();
        if (context == null || intent == null) return;

        final PackagesHelper packagesHelper = PackagesHelper.getInstance(context);
        final List<PackageInfo> packageInfos = packagesHelper.getAllPackages();
        if (packageInfos.isEmpty()) return;

        final boolean ignoreExcluded = "awais.action.KILLALL_IGNORED".equals(intent.getAction());
        final Set<String> excludedPackages = packagesHelper.getExcludedPackages();

        Shell shell = Shell.getCachedShell();
        if (shell == null) shell = Shell.getShell();
        final Shell.Job job = shell.newJob();

        job.add("am kill-all");

        for (final PackageInfo pkgInfo : packageInfos) {
            if (pkgInfo == null) continue;

            if (Utils.isExcluded(excludedPackages, pkgInfo)) {
                if (ignoreExcluded) job.add("am force-stop " + pkgInfo.packageName);
                continue;
            }
            if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) != ApplicationInfo.FLAG_STOPPED) {
                job.add("am force-stop " + pkgInfo.packageName);
            }
        }
        job.exec();

        finishAffinity();
    }
}
package awais.taskill.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("ApplySharedPref")
public final class SettingsHelper {
    private static SettingsHelper _instance;
    private final SharedPreferences prefs;

    public static SettingsHelper getInstance(Context context) {
        synchronized (SettingsHelper.class) {
            if (_instance == null) _instance = new SettingsHelper(context);
            return _instance;
        }
    }

    public SettingsHelper(@NonNull final Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public long getDonationLastShown() {
        return prefs == null ? -1L : prefs.getLong(KEY_LAST_DONATION, -1L);
    }

    @AppCompatDelegate.NightMode
    public int getDarkMode() {
        if (prefs != null) {
            int darkMode = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            if (darkMode == AppCompatDelegate.MODE_NIGHT_NO) return AppCompatDelegate.MODE_NIGHT_NO;
            if (darkMode == AppCompatDelegate.MODE_NIGHT_YES) return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    public boolean showAppIcon() {
        return prefs == null || prefs.getBoolean(KEY_APP_ICON, true);
    }

    public boolean showPackageLabel() {
        return prefs == null || prefs.getBoolean(KEY_APP_PACKAGE_LABEL, true);
    }

    public boolean showVersionLabel() {
        return prefs == null || prefs.getBoolean(KEY_APP_VERSION_LABEL, true);
    }

    public boolean showAppTypeIndicator() {
        return prefs == null || prefs.getBoolean(KEY_APP_TYPE_INDICATOR, true);
    }

    public boolean showKillExcluded() {
        return prefs != null && prefs.getBoolean(KEY_SHOW_KILL_EXCLUDED, false);
    }

    public boolean showExcludedApps() {
        return prefs == null || prefs.getBoolean(KEY_SHOW_EXCLUDED, true);
    }

    public boolean getWidgetKillIgnoring() {
        return prefs == null || prefs.getBoolean(KEY_WIDGET_KILL_IGNORING, true);
    }

    public void setDonationLastShown() {
        if (prefs != null) prefs.edit().putLong(KEY_LAST_DONATION, System.currentTimeMillis()).apply();
    }

    public void setDarkMode(@AppCompatDelegate.NightMode final int darkMode) {
        if (prefs != null) prefs.edit().putInt(KEY_DARK_MODE, darkMode).apply();
    }

    public void setShowAppIcon(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_APP_ICON, show).apply();
    }

    public void setShowPackageLabel(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_APP_PACKAGE_LABEL, show).apply();
    }

    public void setShowVersionLabel(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_APP_VERSION_LABEL, show).apply();
    }

    public void setShowAppTypeIndicator(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_APP_TYPE_INDICATOR, show).apply();
    }

    public void setShowKillExcluded(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_SHOW_KILL_EXCLUDED, show).apply();
    }

    public void setShowExcludedApps(final boolean show) {
        if (prefs != null) prefs.edit().putBoolean(KEY_SHOW_EXCLUDED, show).apply();
    }

    public void setWidgetKillIgnoring(final boolean killIgnoring) {
        if (prefs != null) prefs.edit().putBoolean(KEY_WIDGET_KILL_IGNORING, killIgnoring).commit();
    }

    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_LAST_DONATION = "donationLastShown";
    private static final String KEY_APP_ICON = "showAppIcon";
    private static final String KEY_APP_PACKAGE_LABEL = "showAppPkg";
    private static final String KEY_APP_VERSION_LABEL = "showAppVersion";
    private static final String KEY_APP_TYPE_INDICATOR = "showAppTypeIndicator";
    private static final String KEY_SHOW_KILL_EXCLUDED = "showExcludedKill";
    private static final String KEY_SHOW_EXCLUDED = "showExcludedApps";
    private static final String KEY_WIDGET_KILL_IGNORING = "widgetAction";
}
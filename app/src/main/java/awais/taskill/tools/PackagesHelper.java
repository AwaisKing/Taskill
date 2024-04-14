package awais.taskill.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.collection.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import awais.taskill.BuildConfig;

@SuppressLint({"RestrictedApi", "StaticFieldLeak"})
public final class PackagesHelper {
    private static PackagesHelper _instance;

    private final LruCache<String, Bitmap> iconsLruCache = new LruCache<>(60) {
        @Override
        protected void entryRemoved(final boolean evicted, @NonNull final String key, @NonNull final Bitmap oldValue, @Nullable final Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            // boolean wasRecycled = oldValue.isRecycled();
            // if (
            //         (!evicted || newValue != null) &&
            //                 !oldValue.isRecycled()) oldValue.recycle();
            // Log.d("AWAISKING_APP", "key: " + key
            //         + " -- hasNew: " + (newValue != null)
            //         + " -- wasRecycled: " + wasRecycled
            //         + " -- isRecycled: " + oldValue.isRecycled()
            // );
        }
    };
    private final HashMap<String, String> appNamesCache = new HashMap<>(12, 0.95f);

    private final TaskExecutor taskExecutor = ArchTaskExecutor.getInstance();
    private final File iconsCacheDir;
    private final Context context;
    private PackageManager packageManager;
    private SharedPreferences prefs;

    public static PackagesHelper getInstance(final Context context) {
        synchronized (PackagesHelper.class) {
            PackagesHelper instance = _instance;
            if (instance != null && instance.context == null) instance = null;
            if (instance == null) {
                instance = new PackagesHelper(context);
                _instance = instance;
            }
            if (instance.packageManager == null) instance.packageManager = context.getPackageManager();
            return _instance;
        }
    }

    /** @noinspection ResultOfMethodCallIgnored */
    public PackagesHelper(@NonNull final Context context) {
        this.context = context;

        prefs = getPackageHelperPrefs();
        packageManager = context.getPackageManager();

        iconsCacheDir = new File(context.getCacheDir(), "iconsCache");
        if (iconsCacheDir.isFile()) iconsCacheDir.delete();
        if (!iconsCacheDir.exists() || !iconsCacheDir.isDirectory()) iconsCacheDir.mkdirs();
        refreshPackageIcons();
    }

    public void refreshPackageIcons() {
        final List<PackageInfo> installedPackages = getAllPackages();
        for (final PackageInfo pkgInfo : installedPackages) {
            if (!pkgInfo.applicationInfo.enabled) continue;

            final String packageName = pkgInfo.packageName;
            final Bitmap bmp = iconsLruCache.get(packageName);
            if (bmp == null || bmp.isRecycled()) loadIcon(packageName, null);
        }
    }

    public void destroyAndRecycle() {
        final Map<String, Bitmap> snapshot = iconsLruCache.snapshot();
        final Set<String> keySet = snapshot.keySet();
        for (final String key : keySet) {
            final Bitmap bitmap = iconsLruCache.get(key);
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            iconsLruCache.remove(key);
        }
        snapshot.clear();
        iconsLruCache.evictAll();

        appNamesCache.clear();

        try {
            // noinspection FinalizeCalledExplicitly
            _instance.finalize();
            _instance = null;
        } catch (final Throwable e) {
            // ignore
        }
    }

    @NonNull
    public List<PackageInfo> getAllPackages() {
        final SharedPreferences prefs = getPackageHelperPrefs();

        final boolean showMyApp = prefs.getBoolean("showTaskill", false);
        final boolean showSystem = prefs.getBoolean("showSystem", false);
        final boolean showLauncher = prefs.getBoolean("showLauncher", false);
        final boolean showOnlyRunning = prefs.getBoolean("showOnlyRunning", true);
        final boolean showThemesFonts = prefs.getBoolean("showThemesFonts", false);
        final boolean showAutoGenerated = prefs.getBoolean("showAutoGenerated", false);
        final boolean showSystemProviders = prefs.getBoolean("showSystemProviders", false);

        final ResolveInfo resolveInfo = packageManager.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                                                                       PackageManager.MATCH_DEFAULT_ONLY);
        final String launcherApp = resolveInfo != null ? resolveInfo.activityInfo.packageName : null;

        final List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        final List<PackageInfo> returnPackages = new ArrayList<>(installedPackages.size() >>> 1);

        for (final PackageInfo pkgInfo : installedPackages) {
            if (!pkgInfo.applicationInfo.enabled) continue;

            final String pkgName = pkgInfo.packageName;
            if (TextUtils.equals(pkgName, "android")) continue;
            if (TextUtils.equals(pkgName, ".qtidataservices")) continue;
            if (TextUtils.equals(pkgName, "android.ext.services")) continue;

            if (!showAutoGenerated) {
                if (pkgName.contains(".auto_generated_rro_")) continue;
                if (pkgName.startsWith("com.android.internal.")) continue;
            }

            if (!showThemesFonts) {
                if (pkgName.startsWith("com.android.system.navbar.")) continue;
                if (pkgName.startsWith("com.android.systemui.")) {
                    if (pkgName.indexOf("bar_", 20) != -1) continue;
                }
                if (pkgName.startsWith("com.android.theme.")) {
                    if (pkgName.indexOf("font", 17) != -1) continue;
                    if (pkgName.indexOf("icon", 17) != -1) continue;
                    if (pkgName.indexOf("lockscreen", 17) != -1) continue;
                }
            }

            if (!showMyApp && TextUtils.equals(pkgName, BuildConfig.APPLICATION_ID)) continue;
            if (!showLauncher && TextUtils.equals(pkgName, launcherApp)) continue;
            if (!showSystemProviders && pkgName.startsWith("com.android.providers.")) continue;
            if (!showSystem && (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) continue;
            if (showOnlyRunning && (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) continue;

            returnPackages.add(pkgInfo);
        }

        return returnPackages;
    }

    @NonNull
    public Set<String> getExcludedPackages() {
        final HashSet<String> returnSet = new HashSet<>(0);
        final Set<String> excludedPackages = getPackageHelperPrefs().getStringSet("excludedPackages", returnSet);
        if (!excludedPackages.isEmpty()) {
            returnSet.clear();
            returnSet.addAll(excludedPackages);
        }
        return returnSet;
    }

    public void setExcludedPackages(final Set<String> stringSet) {
        final SharedPreferences.Editor editor = getPackageHelperPrefs().edit();
        if (stringSet == null || stringSet.isEmpty()) editor.remove("excludedPackages");
        else editor.putStringSet("excludedPackages", stringSet);
        editor.apply();
    }

    @Nullable
    public String getAppName(@NonNull final ApplicationInfo appInfo) {
        String name = appNamesCache.get(appInfo.packageName);
        if (TextUtils.isEmpty(name)) {
            try {
                name = packageManager.getApplicationLabel(appInfo).toString();
                appNamesCache.put(appInfo.packageName, name);
            } catch (final Exception e) {
                name = null;
            }
        }
        return name;
    }

    public SharedPreferences getPackageHelperPrefs() {
        if (prefs == null) prefs = context.getApplicationContext().getSharedPreferences("pkgHelper", Context.MODE_PRIVATE);
        return prefs;
    }

    public void loadIcon(@NonNull final String pkgName, @Nullable final ImageView imageView) {
        taskExecutor.executeOnDiskIO(() -> {
            final File file = new File(iconsCacheDir, pkgName);

            final boolean canWrite = !file.exists() || !(file.isFile() || !file.delete());
            Bitmap bitmap;
            if ((bitmap = iconsLruCache.get(pkgName)) != null && !bitmap.isRecycled()) {
                setIconToImageView(bitmap, imageView);
                return;
            }

            if (file.exists() && file.isFile()) {
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                } catch (final Exception e) {
                    bitmap = null;
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    setIconToImageView(bitmap, imageView);
                    iconsLruCache.put(pkgName, bitmap);
                    return;
                }
            }

            Drawable drawable;
            try {
                drawable = packageManager.getApplicationIcon(pkgName);
            } catch (final Exception e) {
                drawable = null;
            }
            if (drawable == null) drawable = packageManager.getDefaultActivityIcon();
            if (canWrite) {
                bitmap = drawableToBitmap(drawable);
                try (final FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (final Exception e) {
                    Log.e("AWAISKING_APP", "", e);
                }
                iconsLruCache.put(pkgName, bitmap);
            }

            setIconToImageView(drawable, imageView);
        });
    }

    private void setIconToImageView(final Object icon, final ImageView imageView) {
        if (imageView != null) {
            try {
                if (icon instanceof Bitmap) imageView.setImageBitmap((Bitmap) icon);
                else if (icon instanceof Drawable) imageView.setImageDrawable((Drawable) icon);
            } catch (final Exception e) {
                // ignore
            }
        }
    }

    @NonNull
    private static Bitmap drawableToBitmap(@NonNull final Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) return bitmapDrawable.getBitmap();
        }

        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();
        final Bitmap bitmap = width > 0 && height > 0
                              ? Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                              : Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
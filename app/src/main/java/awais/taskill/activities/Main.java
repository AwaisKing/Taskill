package awais.taskill.activities;

import static awais.taskill.dialogs.BaseBottomSheetDialog.DialogType;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.graphics.Insets;
import androidx.core.util.TypedValueCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.topjohnwu.superuser.internal.UiThreadHandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import awais.taskill.R;
import awais.taskill.adapters.TasksAdapter;
import awais.taskill.databinding.ActivityMainBinding;
import awais.taskill.dialogs.BaseBottomSheetDialog;
import awais.taskill.dialogs.ProgressDialog;
import awais.taskill.tools.BackgroundExecutor;
import awais.taskill.tools.PackagesHelper;
import awais.taskill.tools.SettingsHelper;
import awais.taskill.tools.Utils;

/** @noinspection deprecation, RestrictedApi*/
public final class Main extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TasksAdapter tasksAdapter;
    private ActivityMainBinding mainBinding;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        this.onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!performAndBack()) return;

                final ActionBar actionBar = getSupportActionBar();
                if (actionBar != null && actionBar.collapseActionView()) return;

                boolean canFinish = true;

                android.app.FragmentManager fm = getFragmentManager();
                if (fm != null) {
                    boolean hasState = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || fm.isStateSaved();
                    if (!hasState && fm.popBackStackImmediate()) canFinish = false;
                }
                if (canFinish) {
                    FragmentManager fmSupport = getSupportFragmentManager();
                    if (!fmSupport.isStateSaved() && fmSupport.popBackStackImmediate()) canFinish = false;
                }

                if (canFinish) pressExplicitBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, this.onBackPressedCallback);

        final SettingsHelper settingsHelper = SettingsHelper.getInstance(this);
        AppCompatDelegate.setDefaultNightMode(settingsHelper.getDarkMode());

        ActivityMainBinding mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        this.mainBinding = mainBinding;

        final long donationLastShown = settingsHelper.getDonationLastShown();
        if (donationLastShown == -1 || System.currentTimeMillis() - donationLastShown >= 432000000L)
            BaseBottomSheetDialog.showDialog(DialogType.DONATE, this);

        final RecyclerView rvItems = mainBinding.rvItems;
        final MaterialToolbar toolbar = mainBinding.toolbar;
        final MaterialButton btnKillAll = mainBinding.btnKillAll;
        final LinearLayoutCompat bindingRoot = mainBinding.getRoot();

        setSupportActionBar(toolbar);

        final int _64dp = Math.round(TypedValueCompat.dpToPx(64f, getResources().getDisplayMetrics()));

        ViewCompat.setOnApplyWindowInsetsListener(bindingRoot, (v, insets) -> {
            final Insets sysInsets = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures() | WindowInsetsCompat.Type.systemGestures()
                                                      | WindowInsetsCompat.Type.systemBars());

            toolbar.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0);
            rvItems.setPadding(sysInsets.left, 0, sysInsets.right, sysInsets.bottom + _64dp);

            btnKillAll.setTranslationY(-sysInsets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });

        if (tasksAdapter == null) tasksAdapter = new TasksAdapter(this);
        rvItems.setHasFixedSize(true);
        rvItems.setItemAnimator(null);
        rvItems.setAdapter(tasksAdapter);

        btnKillAll.setOnClickListener(this);
        mainBinding.rlItems.setOnRefreshListener(this);

        final SharedPreferences prefs = PackagesHelper.getInstance(this).getPackageHelperPrefs();
        final boolean showMyApp = prefs.getBoolean("showTaskill", false);
        final boolean showSystem = prefs.getBoolean("showSystem", false);
        final boolean showLauncher = prefs.getBoolean("showLauncher", false);
        final boolean showOnlyRunning = prefs.getBoolean("showOnlyRunning", true);
        final boolean showThemesFonts = prefs.getBoolean("showThemesFonts", false);
        final boolean showAutoGenerated = prefs.getBoolean("showAutoGenerated", false);
        final boolean showSystemProviders = prefs.getBoolean("showSystemProviders", false);

        mainBinding.cbMyApp.setOnCheckedChangeListener(null);
        mainBinding.cbSystem.setOnCheckedChangeListener(null);
        mainBinding.cbLauncher.setOnCheckedChangeListener(null);
        mainBinding.cbThemesFonts.setOnCheckedChangeListener(null);
        mainBinding.cbOnlyRunning.setOnCheckedChangeListener(null);
        mainBinding.cbAutoGenerated.setOnCheckedChangeListener(null);
        mainBinding.cbSystemProviders.setOnCheckedChangeListener(null);

        mainBinding.cbMyApp.setChecked(showMyApp);
        mainBinding.cbSystem.setChecked(showSystem);
        mainBinding.cbLauncher.setChecked(showLauncher);
        mainBinding.cbOnlyRunning.setChecked(showOnlyRunning);
        mainBinding.cbThemesFonts.setChecked(showThemesFonts);
        mainBinding.cbAutoGenerated.setChecked(showAutoGenerated);
        mainBinding.cbSystemProviders.setChecked(showSystemProviders);

        setContentView(bindingRoot);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final RecyclerView.Adapter<?> adapter = mainBinding.rvItems.getAdapter();
        if (tasksAdapter == null) tasksAdapter = new TasksAdapter(this);
        if (adapter == null || adapter != tasksAdapter) mainBinding.rvItems.setAdapter(tasksAdapter);

        final SharedPreferences helperPrefs = PackagesHelper.getInstance(this).getPackageHelperPrefs();
        @SuppressLint("ApplySharedPref")
        final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
            final SharedPreferences.Editor editor = helperPrefs.edit();
            if (buttonView == mainBinding.cbSystem) editor.putBoolean("showSystem", isChecked);
            else if (buttonView == mainBinding.cbMyApp) editor.putBoolean("showTaskill", isChecked);
            else if (buttonView == mainBinding.cbLauncher) editor.putBoolean("showLauncher", isChecked);
            else if (buttonView == mainBinding.cbThemesFonts) editor.putBoolean("showThemesFonts", isChecked);
            else if (buttonView == mainBinding.cbOnlyRunning) editor.putBoolean("showOnlyRunning", isChecked);
            else if (buttonView == mainBinding.cbAutoGenerated) editor.putBoolean("showAutoGenerated", isChecked);
            else if (buttonView == mainBinding.cbSystemProviders) editor.putBoolean("showSystemProviders", isChecked);
            editor.commit();
            onRefresh();
        };

        mainBinding.cbMyApp.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbSystem.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbLauncher.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbThemesFonts.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbOnlyRunning.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbAutoGenerated.setOnCheckedChangeListener(onCheckedChangeListener);
        mainBinding.cbSystemProviders.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                onRefresh();
            }
        }, 300);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        PackagesHelper.getInstance(this).refreshPackageIcons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PackagesHelper.getInstance(this).destroyAndRecycle();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.mSupport) {
            BaseBottomSheetDialog.showDialog(DialogType.DONATE, this);
            return true;
        }
        if (itemId == R.id.mAbout) {
            BaseBottomSheetDialog.showDialog(DialogType.ABOUT, this);
            return true;
        }
        if (itemId == R.id.mSettings) {
            BaseBottomSheetDialog.showDialog(DialogType.SETTINGS, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        if (v == mainBinding.btnKillAll) {
            v.setEnabled(false);

            final PackagesHelper packagesHelper = PackagesHelper.getInstance(this);
            final ProgressDialog progressDialog = ProgressDialog.showDialog(this);
            BackgroundExecutor.getThreadPoolExecutor().execute(() -> {
                final boolean hasSelection = tasksAdapter.hasSelection();

                final Collection<PackageInfo> packageInfos;
                if (hasSelection) {
                    packageInfos = tasksAdapter.getSelectedApps();
                } else {
                    packageInfos = tasksAdapter.getRunningAppsList();
                    if (packageInfos != null) {
                        final Set<String> excludedPackages = packagesHelper.getExcludedPackages();

                        final Iterator<PackageInfo> iterator = packageInfos.iterator();
                        while (iterator.hasNext()) if (Utils.isExcluded(excludedPackages, iterator.next())) iterator.remove();
                    }
                }

                if (packageInfos != null)
                    for (final PackageInfo packageInfo : packageInfos) Utils.killProcess(packageInfo);

                if (!hasSelection) Utils.killAll();

                UiThreadHandler.run(() -> {
                    tasksAdapter.clearSelection();
                    onRefresh();
                    v.setEnabled(true);
                    progressDialog.cancel();
                });
            });
        }
    }

    private boolean performAndBack() {
        if (tasksAdapter == null || !tasksAdapter.hasSelection()) return true;
        tasksAdapter.clearSelection();
        return false;
    }

    private void pressExplicitBack() {
        OnBackPressedCallback onBackPressedCallback = this.onBackPressedCallback;
        if (onBackPressedCallback != null) onBackPressedCallback.setEnabled(false);
        super.onBackPressed();
        if (onBackPressedCallback != null) onBackPressedCallback.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        mainBinding.rlItems.setRefreshing(true);
        tasksAdapter.setPackageInfos(PackagesHelper.getInstance(this).getAllPackages());
        tasksAdapter.refreshList();
        mainBinding.rlItems.setRefreshing(false);
    }
}
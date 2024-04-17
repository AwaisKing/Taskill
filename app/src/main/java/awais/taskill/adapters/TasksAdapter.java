package awais.taskill.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.topjohnwu.superuser.internal.UiThreadHandler;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import awais.taskill.R;
import awais.taskill.activities.Main;
import awais.taskill.databinding.ItemTaskBinding;
import awais.taskill.dialogs.ProgressDialog;
import awais.taskill.tools.BackgroundExecutor;
import awais.taskill.tools.PackagesHelper;
import awais.taskill.tools.SettingsHelper;
import awais.taskill.tools.Utils;

public final class TasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final int VIEW_TYPE_PACKAGE = 990;
    private static final int VIEW_TYPE_HEADER = 991;

    private static final Drawable DRAWABLE_USER_APP = new ColorDrawable(0xff_00ff00).mutate();
    private static final Drawable DRAWABLE_SYSTEM_APP = new ColorDrawable(0xff_ff0000).mutate();
    private static final Drawable DRAWABLE_SYSTEM_UPDATED_APP = new ColorDrawable(0xff_0000ff).mutate();

    /** @noinspection ConstantValue */
    private final Comparator<PackageInfo> packageInfoComparator = (pkgInfo1, pkgInfo2) -> {
        final ApplicationInfo appInfo1 = pkgInfo1.applicationInfo;
        final ApplicationInfo appInfo2 = pkgInfo2.applicationInfo;

        String app1Name = this.packagesHelper.getAppName(appInfo1);
        String app2Name = this.packagesHelper.getAppName(appInfo2);
        {
            if (TextUtils.isEmpty(app1Name)) app1Name = appInfo1.name;
            if (TextUtils.isEmpty(app1Name)) app1Name = "";
            if (TextUtils.isEmpty(app2Name)) app2Name = appInfo1.name;
            if (TextUtils.isEmpty(app2Name)) app2Name = "";
        }

        boolean isApp1System = (appInfo1.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        boolean isApp2System = (appInfo2.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;

        boolean isApp1Updated = (appInfo1.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        boolean isApp2Updated = (appInfo2.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

        boolean isApp1User = !isApp1System & !isApp1Updated;
        boolean isApp2User = !isApp2System & !isApp2Updated;

        final Collator mCollator = Collator.getInstance();
        mCollator.setStrength(Collator.SECONDARY);

        if (isApp1User && isApp2User) return 0;
        if (isApp1User && isApp2Updated) return -1;
        if (isApp1User && isApp2System) return -1;

        if (isApp1Updated && isApp2User) return 1;
        if (isApp1Updated && isApp2Updated) return 0;
        if (isApp1Updated && isApp2System) return -1;

        if (isApp1System && isApp2User) return 1;
        if (isApp1System && isApp2Updated) return 1;
        if (isApp1System && isApp2System) return -1;

        return mCollator.compare(app1Name, app2Name);
    };
    private final MenuItemClickListener menuItemClickListener = new MenuItemClickListener();
    private final HashSet<PackageInfo> runningApps = new HashSet<>(12, 0.95f);
    private final HashSet<PackageInfo> selectedApps = new HashSet<>(12, 0.95f);
    private final LayoutInflater layoutInflater;
    private final SettingsHelper settingsHelper;
    private PackagesHelper packagesHelper;
    private List<Object> objectsList;

    public TasksAdapter(final Context context) {
        setHasStableIds(true);
        this.settingsHelper = SettingsHelper.getInstance(context);
        this.packagesHelper = PackagesHelper.getInstance(context);
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_HEADER) return new HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false));
        if (viewType == VIEW_TYPE_PACKAGE) return new TaskViewHolder(ItemTaskBinding.inflate(layoutInflater, parent, false));
        throw new RuntimeException("unknown viewType");
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Object obj = objectsList.get(position);

        if (holder instanceof HeaderViewHolder) {
            final String str = obj instanceof String ? (String) obj : obj.toString();
            ((TextView) holder.itemView).setText(str);
            return;
        }

        if (holder instanceof TaskViewHolder && obj instanceof PackageInfo) {
            final ItemTaskBinding taskBinding = ((TaskViewHolder) holder).taskBinding;
            final PackageInfo packageInfo = (PackageInfo) obj;

            final SettingsHelper settingsHelper = this.settingsHelper;
            final PackagesHelper packagesHelper = this.packagesHelper;
            final ApplicationInfo appInfo = packageInfo.applicationInfo;
            final MaterialCardView bindingRoot = taskBinding.getRoot();

            bindingRoot.setChecked(selectedApps.contains(packageInfo));
            if (settingsHelper.showExcludedApps()) {
                final boolean isExcluded = Utils.isExcluded(packagesHelper.getExcludedPackages(), packageInfo);
                bindingRoot.setAlpha(isExcluded ? 0.6f : 1f);
            }

            final boolean showAppTypeIndicator = settingsHelper.showAppTypeIndicator();
            final boolean showAppIcon = settingsHelper.showAppIcon();
            final boolean showPackageLabel = settingsHelper.showPackageLabel();
            final boolean showVersionLabel = settingsHelper.showVersionLabel();
            final boolean showKillExcluded = settingsHelper.showKillExcluded();

            final View appTypeIndicator = bindingRoot.getChildAt(1);
            appTypeIndicator.setVisibility(showAppTypeIndicator ? View.VISIBLE : View.GONE);
            if (showAppTypeIndicator) {
                final boolean isAppSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
                final boolean isAppUpdated = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                final boolean isAppUser = !isAppUpdated & !isAppSystem;

                if (isAppUser) appTypeIndicator.setBackground(DRAWABLE_USER_APP);
                else if (isAppUpdated) appTypeIndicator.setBackground(DRAWABLE_SYSTEM_UPDATED_APP);
                else appTypeIndicator.setBackground(DRAWABLE_SYSTEM_APP);
            }

            final boolean canKill = selectedApps.isEmpty() && (showKillExcluded || isAppRunning(packageInfo));

            if (false) ;// taskBinding.contentPanel.setShowDividers(LinearLayoutCompat.SHOW_DIVIDER_NONE);

            taskBinding.ivKill.setVisibility(canKill ? View.VISIBLE : View.GONE);
            taskBinding.ivIcon.setVisibility(showAppIcon ? View.VISIBLE : View.GONE);
            taskBinding.tvPackage.setVisibility(showPackageLabel ? View.VISIBLE : View.GONE);
            taskBinding.tvVersion.setVisibility(showVersionLabel ? View.VISIBLE : View.GONE);

            if (showAppIcon) packagesHelper.loadIcon(packageInfo.packageName, taskBinding.ivIcon);

            String appName = packagesHelper.getAppName(appInfo);
            if (TextUtils.isEmpty(appName)) appName = appInfo.name;
            taskBinding.tvName.setText(appName);
            taskBinding.tvName.setSelected(true);

            if (showPackageLabel) {
                taskBinding.tvPackage.setText(format("package: %s", appInfo.packageName));
                taskBinding.tvPackage.setSelected(true);
            }

            if (showVersionLabel) {
                taskBinding.tvVersion.setText(format("version: %s {%d}", packageInfo.versionName, packageInfo.versionCode));
                taskBinding.tvVersion.setSelected(true);
            }

            // set tags
            holder.itemView.setTag(packageInfo);
            taskBinding.ivKill.setTag(packageInfo);

            // set click listeners
            holder.itemView.setOnClickListener(this);
            holder.itemView.setOnLongClickListener(this);
            taskBinding.ivKill.setOnClickListener(this);
        }
    }

    @Override
    public boolean onLongClick(@NonNull final View v) {
        final Object tag = v.getTag();
        if (tag instanceof PackageInfo) return startSelection((PackageInfo) tag);
        return false;
    }

    @Override
    public void onClick(@NonNull final View v) {
        final Object tag = v.getTag();
        final Context context = v.getContext();

        if (tag instanceof PackageInfo) {
            final PackageInfo pkgInfo = (PackageInfo) tag;

            if (!selectedApps.isEmpty()) startSelection(pkgInfo);
            else if (v instanceof ImageView) killProcess(pkgInfo);
            else if (v instanceof MaterialCardView) {
                if (true) Log.d("AWAISKING_APP", "pkgInfo: " + pkgInfo);

                final Set<String> excludedPackages = packagesHelper.getExcludedPackages();
                final boolean isExcluded = Utils.isExcluded(excludedPackages, pkgInfo);

                menuItemClickListener.context = context;
                menuItemClickListener.pkgInfo = pkgInfo;
                menuItemClickListener.isExcluded = isExcluded;
                menuItemClickListener.excludedPackages = excludedPackages;

                final Object tagPopup = v.getTag(R.id.key_popupMenu);
                final PopupMenu popupMenu;
                if (tagPopup instanceof PopupMenu) popupMenu = (PopupMenu) tagPopup;
                else {
                    popupMenu = new PopupMenu(context, v, GravityCompat.END);
                    v.setTag(R.id.key_popupMenu, popupMenu);
                }

                final Menu menu = popupMenu.getMenu();
                if (menu.hasVisibleItems() || menu.size() > 0) {
                    final MenuItem menuItem = menu.findItem(2);
                    if (menuItem != null) menuItem.setTitle(isExcluded ? "Include" : "Exclude");
                } else {
                    menu.add(0, 1, Menu.NONE, "Kill");
                    menu.add(0, 2, Menu.NONE, isExcluded ? "Include" : "Exclude");
                    menu.add(0, 3, Menu.NONE, "App Info");
                }
                popupMenu.setOnMenuItemClickListener(menuItemClickListener);
                popupMenu.show();
            }
        }
    }

    public int getItemPos(final PackageInfo pkgInfo) {
        if (objectsList == null) return -1;
        int index = objectsList.indexOf(pkgInfo);
        if (index == -1) {
            for (int i = 0, objectsListSize = objectsList.size(); i < objectsListSize; i++) {
                final Object obj = objectsList.get(i);
                if (obj == pkgInfo || Objects.equals(obj, pkgInfo)
                    || obj instanceof PackageInfo && TextUtils.equals(pkgInfo.packageName, ((PackageInfo) obj).packageName)) {
                    return i;
                }
            }
        }
        return index;
    }

    private void killProcess(final PackageInfo pkgInfo) {
        if (pkgInfo == null) return;
        final ProgressDialog progressDialog = ProgressDialog.showDialog(layoutInflater.getContext());
        BackgroundExecutor.getThreadPoolExecutor().execute(() -> {
            Utils.killProcess(pkgInfo);
            removeSelection(pkgInfo);
            setPackageInfos(packagesHelper.getAllPackages());
            UiThreadHandler.run(this::refreshList);
            progressDialog.cancel();
        });
    }

    private boolean isAppRunning(final PackageInfo packageInfo) {
        if (runningApps.contains(packageInfo)) return true;
        for (final PackageInfo runningApp : runningApps) {
            if (!(Objects.equals(runningApp, packageInfo) || TextUtils.equals(runningApp.packageName, packageInfo.packageName))) continue;
            return true;
        }
        return false;
    }

    public Collection<PackageInfo> getRunningAppsList() {
        return runningApps;
    }

    public Collection<PackageInfo> getSelectedApps() {
        return selectedApps;
    }

    public void setPackageInfos(final List<PackageInfo> packageInfos) {
        this.runningApps.clear();

        if (packageInfos == null) {
            if (objectsList != null) {
                objectsList.clear();
                objectsList = null;
            }
            refreshList();
            return;
        }

        final int size = packageInfos.size();

        final List<PackageInfo> excludedApps = new ArrayList<>(size >>> 2);
        final List<PackageInfo> runningApps = new ArrayList<>(size >>> 2);
        final List<PackageInfo> updatedApps = new ArrayList<>(size >>> 2);
        final List<PackageInfo> systemApps = new ArrayList<>(size >>> 2);
        final List<PackageInfo> userApps = new ArrayList<>(size >>> 2);

        final Set<String> excludedPackages = packagesHelper.getExcludedPackages();
        for (final PackageInfo pkgInfo : packageInfos) {
            final ApplicationInfo appInfo = pkgInfo.applicationInfo;
            final boolean isExcluded = Utils.isExcluded(excludedPackages, pkgInfo);
            final boolean isAppRunning = (appInfo.flags & ApplicationInfo.FLAG_STOPPED) != ApplicationInfo.FLAG_STOPPED;
            final boolean isAppSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
            final boolean isAppUpdated = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

            if (isExcluded) {
                excludedApps.add(pkgInfo);
                continue;
            }
            if (isAppRunning) {
                runningApps.add(pkgInfo);
                continue;
            }
            if (isAppUpdated) {
                updatedApps.add(pkgInfo);
                continue;
            }
            if (isAppSystem) {
                systemApps.add(pkgInfo);
                continue;
            }
            userApps.add(pkgInfo);
        }

        Collections.sort(userApps, packageInfoComparator);
        Collections.sort(systemApps, packageInfoComparator);
        Collections.sort(updatedApps, packageInfoComparator);
        Collections.sort(runningApps, packageInfoComparator);
        if (settingsHelper.showExcludedApps()) Collections.sort(excludedApps, packageInfoComparator);
        else excludedApps.clear();

        final List<Object> allObjects = new ArrayList<>(Math.max(0, runningApps.size() + updatedApps.size() + systemApps.size() + userApps.size()
                                                                    + excludedApps.size() - 1));
        if (!runningApps.isEmpty()) {
            this.runningApps.addAll(runningApps);
            allObjects.add("[ RUNNING APPS (" + runningApps.size() + ") ]");
            allObjects.addAll(runningApps);
        }
        if (!userApps.isEmpty()) {
            allObjects.add("[ USER APPS (" + userApps.size() + ") ]");
            allObjects.addAll(userApps);
        }
        if (!updatedApps.isEmpty()) {
            allObjects.add("[ SYSTEM UPDATED APPS (" + updatedApps.size() + ") ]");
            allObjects.addAll(updatedApps);
        }
        if (!systemApps.isEmpty()) {
            allObjects.add("[ SYSTEM APPS (" + systemApps.size() + ") ]");
            allObjects.addAll(systemApps);
        }
        if (!excludedApps.isEmpty()) {
            allObjects.add("[ EXCLUDED APPS (" + excludedApps.size() + ") ]");
            allObjects.addAll(excludedApps);
        }

        this.objectsList = allObjects;
    }

    @SuppressLint("NotifyDataSetChanged")
    public synchronized void refreshList() {
        final Context context = layoutInflater == null ? null : layoutInflater.getContext();
        final Button btnKill = context instanceof Main ? ((Main) context).findViewById(R.id.btnKillAll) : null;
        if (btnKill != null) btnKill.setText(hasSelection() ? "Kill Selected" : "Kill All");

        synchronized (TasksAdapter.class) {
            notifyDataSetChanged();
        }
    }

    public boolean hasSelection() {
        return !selectedApps.isEmpty();
    }

    private boolean startSelection(final PackageInfo pkgInfo) {
        if (pkgInfo == null) return false;
        int index = objectsList == null ? -1 : objectsList.indexOf(pkgInfo);
        if (index == -1) index = getItemPos(pkgInfo);
        if (index != -1) {
            if (selectedApps.contains(pkgInfo)) selectedApps.remove(pkgInfo);
            else selectedApps.add(pkgInfo);

            refreshList();
        }
        return index != -1;
    }

    public void removeSelection(final PackageInfo pkgInfo) {
        if (this.selectedApps.remove(pkgInfo)) return;
        for (final PackageInfo selectedApp : selectedApps) {
            if (selectedApp == null || !TextUtils.equals(selectedApp.packageName, pkgInfo.packageName)) continue;
            this.selectedApps.remove(selectedApp);
            break;
        }
    }

    public void clearSelection() {
        selectedApps.clear();
        UiThreadHandler.run(this::refreshList);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemViewType(final int position) {
        final Object obj = objectsList.get(position);
        if (obj instanceof PackageInfo) return VIEW_TYPE_PACKAGE;
        if (obj instanceof CharSequence) return VIEW_TYPE_HEADER;
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return objectsList == null ? 0 : objectsList.size();
    }

    @NonNull
    private static String format(final String format, final Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemTaskBinding taskBinding;

        public TaskViewHolder(@NonNull final ItemTaskBinding taskBinding) {
            super(taskBinding.getRoot());
            this.taskBinding = taskBinding;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull final View itemView) {
            super(itemView);
        }
    }

    private class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Context context;
        private boolean isExcluded;
        private PackageInfo pkgInfo;
        private Set<String> excludedPackages;

        @Override
        public boolean onMenuItemClick(final MenuItem menuItem) {
            if (menuItem == null || context == null || excludedPackages == null || pkgInfo == null) return false;
            switch (menuItem.getItemId()) {
                case 1:
                    killProcess(pkgInfo);
                    return true;

                case 2:
                    if (isExcluded) {
                        excludedPackages.remove(pkgInfo.packageName);
                        excludedPackages.remove(pkgInfo.applicationInfo.packageName);
                    } else {
                        excludedPackages.add(pkgInfo.packageName);
                        excludedPackages.add(pkgInfo.applicationInfo.packageName);
                    }
                    packagesHelper.setExcludedPackages(excludedPackages);
                    setPackageInfos(packagesHelper.getAllPackages());
                    refreshList();
                    return true;

                case 3:
                    context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkgInfo.packageName))
                                                  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY
                                                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS).addCategory(Intent.CATEGORY_DEFAULT));
                    return true;
            }
            return false;
        }
    }
}
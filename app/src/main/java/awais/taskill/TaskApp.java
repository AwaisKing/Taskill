package awais.taskill;

import androidx.multidex.MultiDexApplication;

import awais.taskill.tools.PackagesHelper;
import awais.taskill.tools.SettingsHelper;

public final class TaskApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        PackagesHelper.getInstance(this).refreshPackageIcons();
        SettingsHelper.getInstance(this);
    }
}
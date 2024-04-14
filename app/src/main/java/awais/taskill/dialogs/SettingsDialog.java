package awais.taskill.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import awais.taskill.R;
import awais.taskill.activities.Main;
import awais.taskill.databinding.DialogSettingsBinding;
import awais.taskill.tools.SettingsHelper;
import awais.taskill.tools.Utils;

public final class SettingsDialog extends BaseBottomSheetDialog {
    private DialogSettingsBinding settingsBinding;

    public SettingsDialog() {
        super();
    }

    @Override
    protected boolean runStateFixer() {
        return false;
    }

    @NonNull
    @Override
    protected View onDialogCreated(@NonNull final Dialog dialog) {
        Utils.setupDialogState(dialog, true, true, 0b010011, 0);
        if (settingsBinding == null) settingsBinding = DialogSettingsBinding.inflate(LayoutInflater.from(context));
        settingsBinding.btnOK.setOnClickListener(this);
        return settingsBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        final SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        final DialogSettingsBinding settingsBinding = this.settingsBinding;
        if (settingsBinding == null || settingsHelper == null) return;

        settingsBinding.btgTheme.clearChecked();
        if (settingsHelper.getDarkMode() == AppCompatDelegate.MODE_NIGHT_YES) settingsBinding.btgTheme.check(settingsBinding.btnThemeDark.getId());
        else if (settingsHelper.getDarkMode() == AppCompatDelegate.MODE_NIGHT_NO) settingsBinding.btgTheme.check(settingsBinding.btnThemeLight.getId());
        else settingsBinding.btgTheme.check(settingsBinding.btnThemeAuto.getId());

        settingsBinding.cbAppIcon.setChecked(settingsHelper.showAppIcon());
        settingsBinding.cbAppType.setChecked(settingsHelper.showAppTypeIndicator());
        settingsBinding.cbAppPackage.setChecked(settingsHelper.showPackageLabel());
        settingsBinding.cbAppVersion.setChecked(settingsHelper.showVersionLabel());
        settingsBinding.cbShowKill.setChecked(settingsHelper.showKillExcluded());
        settingsBinding.cbShowExcluded.setChecked(settingsHelper.showExcludedApps());
    }

    @Override
    public void onClick(final View v) {
        final DialogSettingsBinding settingsBinding = this.settingsBinding;
        synchronized (Main.class) {
            if (settingsBinding != null) {
                final SettingsHelper settingsHelper = SettingsHelper.getInstance(context);

                final int checkedTheme = settingsBinding.btgTheme.getCheckedButtonId();
                if (checkedTheme == R.id.btnThemeDark) settingsHelper.setDarkMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if (checkedTheme == R.id.btnThemeLight) settingsHelper.setDarkMode(AppCompatDelegate.MODE_NIGHT_NO);
                else settingsHelper.setDarkMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                settingsHelper.setShowAppIcon(settingsBinding.cbAppIcon.isChecked());
                settingsHelper.setShowPackageLabel(settingsBinding.cbAppPackage.isChecked());
                settingsHelper.setShowVersionLabel(settingsBinding.cbAppVersion.isChecked());
                settingsHelper.setShowAppTypeIndicator(settingsBinding.cbAppType.isChecked());
                settingsHelper.setShowKillExcluded(settingsBinding.cbShowKill.isChecked());
                settingsHelper.setShowExcludedApps(settingsBinding.cbShowExcluded.isChecked());
            }

            dismiss();
            if (context instanceof Activity) ActivityCompat.recreate((Activity) context);
        }
    }
}
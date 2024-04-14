package awais.taskill.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

import awais.taskill.BuildConfig;
import awais.taskill.databinding.DialogAboutBinding;
import awais.taskill.databinding.ItemLibraryDataBinding;
import awais.taskill.tools.Utils;

public final class AboutDialog extends BaseBottomSheetDialog {
    private DialogAboutBinding aboutBinding;

    public AboutDialog() {
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
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        if (aboutBinding == null) aboutBinding = DialogAboutBinding.inflate(layoutInflater);

        aboutBinding.tvVersion.setText(String.format(Locale.ENGLISH, "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        aboutBinding.btnOK.setOnClickListener(this);
        aboutBinding.btnDonate.setOnClickListener(this);
        aboutBinding.btnGitHub.setOnClickListener(this);
        aboutBinding.btnWebsite.setOnClickListener(this);

        final LinearLayoutCompat contentPanel = aboutBinding.contentPanel;
        for (final LibraryInfo libraryInfo : LibraryInfo.values()) {
            final ItemLibraryDataBinding dataBinding = ItemLibraryDataBinding.inflate(layoutInflater, contentPanel, false);
            dataBinding.tvName.setText(libraryInfo.name);
            dataBinding.tvLicense.setText(libraryInfo.license);
            dataBinding.btnWebsite.setOnClickListener(this);
            final MaterialCardView bindingRoot = dataBinding.getRoot();
            bindingRoot.setTag(libraryInfo);
            contentPanel.addView(bindingRoot);
        }

        return aboutBinding.getRoot();
    }

    @Override
    public void onClick(@NonNull final View v) {
        final Object tag = v.getTag();
        if (v == aboutBinding.btnOK) dismiss();
        else if (v == aboutBinding.btnGitHub) startActivity(new Intent(Intent.ACTION_VIEW, Utils.GITHUB_LINK));
        else if (v == aboutBinding.btnDonate) startActivity(new Intent(Intent.ACTION_VIEW, Utils.BUYMEACOFFEE_LINK));
        else if (v == aboutBinding.btnWebsite) startActivity(new Intent(Intent.ACTION_VIEW, Utils.AWAISOME_LINK));
        else if (tag instanceof LibraryInfo) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((LibraryInfo) tag).url)));
    }

    private enum LibraryInfo {
        ANDROID_MATERIAL("Material Components", "Apache 2.0 license", "https://github.com/material-components/material-components-android"),
        RECYCLER_VIEW("Recycler View", "Apache 2.0 license", "https://developer.android.com/jetpack/androidx/releases/recyclerview"),
        SWIPE_REFRESH("Swipe Refresh Layout", "Apache 2.0 license", "https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout"),
        LIBSU("topjohnwu's libsu", "Apache 2.0 license", "https://github.com/topjohnwu/libsu"),
        ;

        private final String name, license, url;

        LibraryInfo(final String name, final String license, final String url) {
            this.name = name;
            this.license = license;
            this.url = url;
        }
    }
}
package awais.taskill.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import awais.taskill.databinding.DialogDonateBinding;
import awais.taskill.tools.SettingsHelper;
import awais.taskill.tools.Utils;

public final class DonateDialog extends BaseBottomSheetDialog {
    private DialogDonateBinding dialogDonateBinding;

    @NonNull
    @Override
    protected View onDialogCreated(@NonNull final Dialog dialog) {
        dialog.setCanceledOnTouchOutside(false);

        if (dialogDonateBinding == null) dialogDonateBinding = DialogDonateBinding.inflate(LayoutInflater.from(context));

        dialogDonateBinding.btnClose.setOnClickListener(this);
        dialogDonateBinding.btnDonate.setOnClickListener(this);

        return dialogDonateBinding.getRoot();
    }

    @Override
    public void onClick(final View v) {
        if (v == dialogDonateBinding.btnClose) dismiss();
        else if (v == dialogDonateBinding.btnDonate)
            ContextCompat.startActivity(context, new Intent(Intent.ACTION_VIEW, Utils.BUYMEACOFFEE_LINK), null);
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        SettingsHelper.getInstance(context).setDonationLastShown();
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull final DialogInterface dialog) {
        SettingsHelper.getInstance(context).setDonationLastShown();
        super.onCancel(dialog);
    }
}
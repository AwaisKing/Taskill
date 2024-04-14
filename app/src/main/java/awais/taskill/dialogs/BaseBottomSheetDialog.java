package awais.taskill.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import awais.taskill.R;
import awais.taskill.tools.Utils;

public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    protected Context context;

    public static void showDialog(final DialogType dialogType, final FragmentActivity activity) {
        final BaseBottomSheetDialog sheetDialog;
        if (dialogType == DialogType.ABOUT) sheetDialog = new AboutDialog();
        else if (dialogType == DialogType.SETTINGS) sheetDialog = new SettingsDialog();
        else sheetDialog = new DonateDialog();
        sheetDialog.context = activity;
        sheetDialog.show(activity.getSupportFragmentManager(), null);
    }

    public BaseBottomSheetDialog() {
        super();
        setStyle(STYLE_NORMAL, R.style.BottomSheeter);
    }

    @NonNull
    protected abstract View onDialogCreated(@NonNull final Dialog dialog);

    protected boolean runStateFixer() {
        return true;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = Utils.getDialogFixedContext(context, this, null);
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog instanceof BottomSheetDialog) ((BottomSheetDialog) dialog).setDismissWithAnimation(true);

        context = Utils.getDialogFixedContext(context, this, dialog);
        if (runStateFixer()) Utils.setupDialogState(dialog, true, false, 0b111100, 0);

        dialog.setContentView(onDialogCreated(dialog));

        return dialog;
    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheeter;
    }

    public enum DialogType {
        DONATE, ABOUT, SETTINGS
    }
}
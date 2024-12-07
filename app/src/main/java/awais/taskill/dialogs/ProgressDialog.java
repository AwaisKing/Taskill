package awais.taskill.dialogs;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.TypedValueCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import awais.taskill.R;

public final class ProgressDialog {
    private final Context context;
    private ProgressBar mProgress;
    private AlertDialog materialDialog;
    private final int _16dp, _96dp;

    @NonNull
    public static ProgressDialog showDialog(final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        return progressDialog;
    }

    private ProgressDialog(@NonNull final Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this._96dp = Math.round(TypedValueCompat.dpToPx(96f, displayMetrics));
        this._16dp = Math.round(TypedValueCompat.dpToPx(16f, displayMetrics));

        this.context = context;
        this.mProgress = new ProgressBar(context);
        this.materialDialog = getMaterialDialog();
    }

    private AlertDialog getMaterialDialog() {
        if (materialDialog == null) {
            if (mProgress == null) mProgress = new ProgressBar(context);
            mProgress.setIndeterminate(true);
            this.materialDialog = new MaterialAlertDialogBuilder(context, R.style.Theme_Dialog_Progress).setView(mProgress).create();
        }
        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        return materialDialog;
    }

    private void show() {
        getMaterialDialog().show();
        if (mProgress != null) {
            ViewGroup.LayoutParams layoutParams = mProgress.getLayoutParams();
            if (layoutParams != null) layoutParams.height = layoutParams.width = _96dp;
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                ((FrameLayout.LayoutParams) layoutParams).setMargins(_16dp, _16dp, _16dp, _16dp);
            }
            mProgress.setLayoutParams(layoutParams);
        }
    }

    public void cancel() {
        if (materialDialog != null && materialDialog.isShowing()) materialDialog.cancel();
        materialDialog = null;
        mProgress = null;
    }
}
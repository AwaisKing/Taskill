package awais.taskill.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public final class ProgressDialog extends AlertDialog {
    private final Context context;
    private ProgressBar mProgress;

    private ProgressDialog(@NonNull final Context context) {
        super(context, 0);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        this.context = context;
        this.mProgress = new ProgressBar(context);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Context context = this.context;
        if (context == null) context = getContext();
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int _128dp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128f, displayMetrics));
        int _16dp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics));

        if (mProgress == null) mProgress = new ProgressBar(context);
        mProgress.setIndeterminate(true);

        ViewGroup.LayoutParams layoutParams = mProgress.getLayoutParams();
        if (layoutParams == null) layoutParams = new ViewGroup.LayoutParams(_128dp, _128dp);
        else layoutParams.height = layoutParams.width = _128dp;

        final ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(layoutParams);
        marginLayoutParams.setMargins(_16dp, _16dp, _16dp, _16dp);
        mProgress.setLayoutParams(marginLayoutParams);

        setView(mProgress, _16dp, _16dp, _16dp, _16dp);

        layoutParams = mProgress.getLayoutParams();
        if (layoutParams instanceof FrameLayout.LayoutParams) {
            final FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) layoutParams;
            frameParams.gravity = Gravity.CENTER;
            mProgress.setLayoutParams(frameParams);
        }

        super.onCreate(savedInstanceState);
    }

    @NonNull
    public static ProgressDialog showDialog(final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        return progressDialog;
    }
}
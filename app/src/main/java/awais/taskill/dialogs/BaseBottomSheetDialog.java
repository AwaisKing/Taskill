package awais.taskill.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

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
        setStyle(STYLE_NORMAL, R.style.Theme_Dialog_BottomSheet);
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
        final BottomSheetDialog dialog = new BottomSheetDialog(Objects.requireNonNullElse(getContext(), context), getTheme()) {
            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
                final Window window = getWindow();
                if (window != null) WindowCompat.setDecorFitsSystemWindows(window, false);

                View container = findViewById(R.id.container);
                View coordinator = findViewById(R.id.coordinator);
                View bottomSheet = findViewById(R.id.design_bottom_sheet);

                if (container != null) {
                    container.setFitsSystemWindows(false);
                    if (coordinator == null) coordinator = ((ViewGroup) container).getChildAt(0);
                }
                if (coordinator != null) {
                    coordinator.setFitsSystemWindows(false);
                    if (bottomSheet == null) bottomSheet = ((ViewGroup) coordinator).getChildAt(1);
                    ViewCompat.setOnApplyWindowInsetsListener(coordinator, (view, insets) -> new WindowInsetsCompat.Builder(insets)
                                   .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.NONE)
                                   .build());
                }
                if (bottomSheet != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(bottomSheet, (view, insets) -> {
                        Insets sysInsets = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()
                                                            | WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());

                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), sysInsets.bottom);

                        return WindowInsetsCompat.CONSUMED;
                    });
                }

                if (coordinator != null) coordinator.requestApplyInsets();
                if (bottomSheet != null) bottomSheet.requestApplyInsets();
            }
        };
        dialog.setDismissWithAnimation(true);

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
        return R.style.Theme_Dialog_BottomSheet;
    }

    public enum DialogType {
        DONATE, ABOUT, SETTINGS
    }
}
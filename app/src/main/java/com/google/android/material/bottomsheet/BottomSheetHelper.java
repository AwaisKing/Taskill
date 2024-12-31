package com.google.android.material.bottomsheet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import awais.taskill.R;

public final class BottomSheetHelper {
    @Nullable
    public static <V extends View> View getSheetView(final BottomSheetBehavior<V> behavior) {
        if (behavior == null) return null;
        final WeakReference<V> viewRef = behavior.viewRef;
        return viewRef != null ? viewRef.get() : null;
    }

    @Nullable
    public static View getSheetView(final DialogInterface dialogInterface) {
        View sheetView = null;
        if (dialogInterface instanceof Dialog) {
            sheetView = ((Dialog) dialogInterface).findViewById(R.id.design_bottom_sheet);
            if (!(sheetView instanceof FrameLayout)) sheetView = null;
        }
        if (sheetView == null && dialogInterface instanceof BottomSheetDialog)
            sheetView = getSheetView(((BottomSheetDialog) dialogInterface).getBehavior());
        return sheetView;
    }

    public static View getCoordinatorView(final DialogInterface dialogInterface) {
        if (dialogInterface instanceof Dialog) return ((Dialog) dialogInterface).findViewById(R.id.coordinator);
        return null;
    }
}
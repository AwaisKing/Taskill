package awais.taskill.tools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheeter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Set;

import awais.taskill.R;
import awais.taskill.adapters.TasksAdapter;

public final class Utils {
    public static final Uri GITHUB_LINK = Uri.parse("https://github.com/AwaisKing/Taskill");
    public static final Uri AWAISOME_LINK = Uri.parse("https://awaisome.com");
    public static final Uri BUYMEACOFFEE_LINK = Uri.parse("https://buymeacoffee.com/awaisome");
    // public static final String PS_CMD = "ps -Ao USER:12,GROUP,PCY,UID,PID,%CPU,%MEM,NAME:32,CMDLINE"
    //         + " | grep -E \"([^\\S]:[^\\S]:.*app[:/].*|^u[0-9]+_)\""
    //         + " | awk '{ print $1 \"ɵ\" $2 \"ɵ\" $3 \"ɵ\" $4 \"ɵ\" $5 \"ɵ\" $6 \"ɵ\" $7 \"ɵ\" $8 \"ɵ\" $9 \"ɵ\" $10 }'";
    // public static final char SPLIT_CHAR = 'ɵ';

    public synchronized static void killAll() {
        synchronized (Utils.class) {
            Shell shell = Shell.getCachedShell();
            if (shell == null) shell = Shell.getShell();
            shell.newJob().add("am kill-all").exec();
        }
    }

    public static void killProcess(@Nullable final PackageInfo pkgInfo) {
        killProcess(null, null, pkgInfo);
    }

    public static void killProcess(@Nullable final TasksAdapter tasksAdapter, @Nullable final PackagesHelper packagesHelper, @Nullable final PackageInfo pkgInfo) {
        if (pkgInfo != null) {
            Shell shell = Shell.getCachedShell();
            if (shell == null) shell = Shell.getShell();

            final Shell.Job job = shell.newJob();
            job.add("am force-stop " + pkgInfo.packageName)
            // .add("am kill " + pkgInfo.packageName)
            // .add("am stop-app " + pkgInfo.packageName)
            ;
            job.exec();
        }

        // if (tasksAdapter != null) {
        //     tasksAdapter.clearSelection();
        //     if (packagesHelper != null) tasksAdapter.setPackageInfos(packagesHelper.getAllPackages());
        //     UiThreadHandler.run(tasksAdapter::refreshList);
        // }
    }

    @NonNull
    public static String getOutputFromShell(@NonNull final Shell shell, final String... cmds) {
        // if (shell == null) shell = Shell.getCachedShell();
        // if (shell == null) shell = Shell.getShell();
        final List<String> out = shell.newJob().add(cmds).exec().getOut();

        final StringBuilder output = new StringBuilder();
        for (final String str : out) output.append(str).append('\n');
        output.trimToSize();

        return output.toString().trim();
    }

    public static boolean isExcluded(@NonNull final Set<String> excludedPackages, @NonNull final PackageInfo pkgInfo) {
        final ApplicationInfo appInfo = pkgInfo.applicationInfo;

        boolean isExcluded = excludedPackages.contains(pkgInfo.packageName);
        if (!isExcluded) isExcluded = excludedPackages.contains(appInfo.packageName);
        if (!isExcluded) for (final String excludedPackage : excludedPackages) {
            if (TextUtils.equals(excludedPackage, pkgInfo.packageName)) return true;
            if (TextUtils.equals(excludedPackage, appInfo.packageName)) return true;
        }

        return isExcluded;
    }

    public static Context getDialogFixedContext(Context context, final BottomSheetDialogFragment dialogFragment, Dialog dialog) {
        if (context == null) context = dialogFragment.getContext();
        if (context == null) context = dialogFragment.getActivity();
        if (dialog == null) {
            try {
                dialog = dialogFragment.getDialog();
            } catch (final Throwable e) {
                // ignore
            }
        }
        if (dialog != null) {
            if (context == null) context = dialog.getOwnerActivity();
            if (context == null) context = dialog.getContext();
        }
        return context;
    }

    /**
     * @param options       sets options for dialog<br/>
     *                      0b000001 = isDraggable<br/>
     *                      0b000010 = fitToContents<br/>
     *                      0b000100 = setExpandOffset<br/>
     *                      0b001000 = setIgnoreGesture<br/>
     *                      0b010000 = setStateExpanded<br/>
     *                      0b100000 = contentFullHeight<br/>
     * @param cornerOptions sets corner options for dialog<br/>
     *                      0b1111 = all corners<br/>
     *                      0b1000 = top left corner<br/>
     *                      0b0100 = top right corner<br/>
     *                      0b0010 = bottom left corner<br/>
     *                      0b0001 = bottom right corner<br/>
     */
    @SuppressLint("RestrictedApi")
    public static void setupDialogState(DialogInterface dialog, final boolean setupShowListenerState, final Boolean setupBackground,
                                        final int options, final int cornerOptions) {
        if (setupShowListenerState && dialog instanceof Dialog)
            ((Dialog) dialog).setOnShowListener(intDialog -> setupDialogState(intDialog, false, setupBackground, options, cornerOptions));

        if (dialog instanceof BottomSheetDialogFragment) {
            final BottomSheetDialogFragment dialogFragment = (BottomSheetDialogFragment) dialog;
            dialog = dialogFragment.getDialog();
        }

        if (dialog instanceof BottomSheetDialog) {
            final BottomSheetDialog sheetDialog = (BottomSheetDialog) dialog;
            final BottomSheetBehavior<FrameLayout> sheetBehavior = sheetDialog.getBehavior();
            final View sheetView = BottomSheeter.getSheetView(dialog);

            final Context context = sheetDialog.getContext();
            final Resources resources = context.getResources();
            final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

            final boolean isDraggable = (options & 0b000001) != 0; // false
            final boolean fitToContents = (options & 0b000010) != 0; // false
            final boolean setExpandOffset = (options & 0b000100) != 0; // true
            final boolean setIgnoreGesture = (options & 0b001000) != 0;// true
            final boolean setStateExpanded = (options & 0b010000) != 0;// true
            final boolean contentFullHeight = (options & 0b100000) != 0;// false

            if (sheetView != null) {
                if (contentFullHeight) {
                    final ViewGroup.LayoutParams layoutParams = sheetView.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    // if (layoutParams instanceof FrameLayout.LayoutParams) ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                    // if (layoutParams instanceof CoordinatorLayout.LayoutParams) ((CoordinatorLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                    sheetView.setLayoutParams(layoutParams);
                }

                int defaultColor;
                float cornerSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, displayMetrics);

                // get default color
                {
                    final Resources.Theme contextTheme = context.getTheme();
                    final TypedValue tempTypedValue = new TypedValue();

                    contextTheme.resolveAttribute(R.attr.colorSurface, tempTypedValue, true);
                    defaultColor = getColorFromTypedValue(context, tempTypedValue);

                    if (defaultColor == Color.TRANSPARENT || defaultColor == Color.WHITE || defaultColor == Color.BLACK)
                        defaultColor = new MaterialCardView(new ContextThemeWrapper(context, R.style.Theme_Awais))
                                               .getCardBackgroundColor().getDefaultColor();
                }

                ShapeAppearanceModel.Builder shapeBuilder = ShapeAppearanceModel.builder();

                MaterialShapeDrawable sheetDrawable;
                if (sheetView.getBackground() instanceof MaterialShapeDrawable) {
                    sheetDrawable = (MaterialShapeDrawable) sheetView.getBackground();

                    final float topLeftSize = sheetDrawable.getTopLeftCornerResolvedSize();
                    final float topRightSize = sheetDrawable.getTopRightCornerResolvedSize();
                    if ((int) topLeftSize == 0 && (int) topRightSize != 0) cornerSize = topRightSize;
                    else if ((int) topRightSize == 0 && (int) topLeftSize != 0) cornerSize = topLeftSize;

                    final ColorStateList fillColor = sheetDrawable.getFillColor();
                    if (fillColor != null && defaultColor != fillColor.getDefaultColor()) defaultColor = fillColor.getDefaultColor();

                    shapeBuilder = sheetDrawable.getShapeAppearanceModel().toBuilder();
                }

                if (cornerOptions == 0b1111) shapeBuilder.setAllCorners(CornerFamily.ROUNDED, cornerSize);
                if ((cornerOptions & 0b1000) != 0) shapeBuilder.setTopLeftCorner(CornerFamily.ROUNDED, cornerSize);
                if ((cornerOptions & 0b0100) != 0) shapeBuilder.setTopRightCorner(CornerFamily.ROUNDED, cornerSize);
                if ((cornerOptions & 0b0010) != 0) shapeBuilder.setBottomLeftCorner(CornerFamily.ROUNDED, cornerSize);
                if ((cornerOptions & 0b0001) != 0) shapeBuilder.setBottomRightCorner(CornerFamily.ROUNDED, cornerSize);
                sheetDrawable = new MaterialShapeDrawable(shapeBuilder.build());

                if (setupBackground != null) {
                    final int bgColor = setupBackground == Boolean.TRUE || setupBackground ? defaultColor : Color.TRANSPARENT;
                    sheetDrawable.setFillColor(ColorStateList.valueOf(bgColor));
                }

                sheetDrawable.setElevation(8f);
                sheetDrawable.setShadowVerticalOffset(16);
                if (sheetDrawable.requiresCompatShadow()) sheetDrawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);

                sheetView.setBackground(sheetDrawable);
            }

            final int expandedOffset = !setExpandOffset ? 0 : Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics));

            sheetBehavior.setDraggable(isDraggable);
            sheetBehavior.setFitToContents(fitToContents);
            sheetBehavior.setExpandedOffset(expandedOffset);
            sheetBehavior.setGestureInsetBottomIgnored(setIgnoreGesture);
            if (setStateExpanded) sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private static int getColorFromTypedValue(final Context context, @NonNull final TypedValue typedValue) {
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
            && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) return typedValue.data;

        final Resources.Theme theme = context.getTheme();
        final Resources resources = context.getResources();

        final Drawable drawable = ResourcesCompat.getDrawable(resources, typedValue.resourceId, theme);
        final ColorStateList fillColor = drawable instanceof MaterialShapeDrawable ? ((MaterialShapeDrawable) drawable).getFillColor() : null;
        if (fillColor != null) return fillColor.getDefaultColor();
        else if (drawable instanceof ColorDrawable) return ((ColorDrawable) drawable).getColor();

        return Color.TRANSPARENT;
    }
}
package awais.taskill.widgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.PendingIntentCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import awais.taskill.R;
import awais.taskill.activities.ShortcutActivity;
import awais.taskill.tools.SettingsHelper;

public final class KillerWidget extends AppWidgetProvider {
    private static final ColorStateList cslIgnoredKill = ColorStateList.valueOf(Color.WHITE);
    private static final Set<String> WIDGET_ACTIONS = new HashSet<>(Arrays.asList(
            "android.appwidget.action.APPWIDGET_ENABLE_AND_UPDATE", AppWidgetManager.ACTION_APPWIDGET_CONFIGURE,
            AppWidgetManager.ACTION_APPWIDGET_ENABLED, AppWidgetManager.ACTION_APPWIDGET_RESTORED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE, AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED));

    @SuppressLint("RestrictedApi")
    public static void updateWidget(@NonNull final Context context, final int appWidgetId) {
        boolean killAllIgnoring = SettingsHelper.getInstance(context.getApplicationContext()).getWidgetKillIgnoring();
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_killer);

        final IconCompat iconCompat = IconCompat.createWithResource(context, R.mipmap.ic_launcher_round);
        iconCompat.setTintList(killAllIgnoring ? cslIgnoredKill : null);
        if (killAllIgnoring) iconCompat.setTintMode(PorterDuff.Mode.SRC_OUT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) views.setImageViewIcon(android.R.id.closeButton, iconCompat.toIcon(context));
        else try {
            views.setImageViewBitmap(android.R.id.closeButton, iconCompat.getBitmap());
        } catch (Throwable e) {
            views.setImageViewResource(android.R.id.closeButton, R.mipmap.ic_launcher_round);
        }

        views.setTextViewText(android.R.id.text1, context.getString(killAllIgnoring ? R.string.shortut_kill_all_ignored : R.string.shortut_kill_all));

        final Intent intent = new Intent(context, ShortcutActivity.class).setAction("awais.action.KILLALL");
        views.setOnClickPendingIntent(android.R.id.closeButton, PendingIntentCompat.getActivity(context, 1010, intent,
                                                                                                PendingIntent.FLAG_ONE_SHOT, false));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(final Context context, @NonNull final Intent intent) {
        // super.onReceive(context, intent);

        Bundle extras = intent.getExtras();
        if (extras == null || !WIDGET_ACTIONS.contains(intent.getAction())) return;
        int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        final ArrayList<Integer> widgetIds = new ArrayList<>((appWidgetIds == null ? 0 : appWidgetIds.length) + 2);

        if (appWidgetIds != null) for (final int appWidgetId : appWidgetIds) widgetIds.add(appWidgetId);
        if (extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) widgetIds.add(extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
        widgetIds.trimToSize();

        if (!widgetIds.isEmpty()) for (int appWidgetId : widgetIds) updateWidget(context, appWidgetId);
    }
}
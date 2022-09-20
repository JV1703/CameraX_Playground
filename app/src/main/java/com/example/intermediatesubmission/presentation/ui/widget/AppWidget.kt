package com.example.intermediatesubmission.presentation.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast
import com.example.intermediatesubmission.R

class AppWidget : AppWidgetProvider() {

    companion object {
        private const val TOAST_ACTION = "com.example.intermediatesubmission.TOAST_ACTION"
        const val EXTRA_ITEM = "com.example.intermediatesubmission.EXTRA_ITEM"
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is TOAST_ACTION. If it is, the
    // widget displays a Toast message for the current item.
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action != null) {
            if (intent.action == TOAST_ACTION) {

                // EXTRA_ITEM represents a custom value provided by the Intent
                // passed to the setOnClickFillInIntent() method, to indicate which
                // position of the item was clicked. See StackRemoteViewsFactory in
                // Set the fill-in Intent for details.
                val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
                Toast.makeText(context, "Touched view $viewIndex", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
    ) {

        // Sets up the intent that points to the StackViewService that will
        // provide the views for this collection.
        val intent = Intent(context, StackWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }

        val views = RemoteViews(context.packageName, R.layout.app_widget).apply {
            // Tells view where to get the data. must pass in which layout to show the view and the intent of view service
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects to a RemoteViewsService through the
            // specified intent.
            // This is how you populate the data.
            setRemoteAdapter(R.id.stack_view, intent)

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the
            // RemoteViews object.
            setEmptyView(R.id.stack_view, R.id.empty_view)
        }

        val toastIntent = Intent(context, AppWidget::class.java)
        toastIntent.action = TOAST_ACTION
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)


        // This section makes it possible for items to have individualized
        // behavior. It does this by setting up a pending intent template.
        // Individuals items of a collection cannot set up their own pending
        // intents. Instead, the collection as a whole sets up a pending
        // intent template, and the individual items set a fillInIntent
        // to create unique behavior on an item-by-item basis.
        val toastPendingIntent: PendingIntent = Intent(context, AppWidget::class.java).run {
            action = TOAST_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
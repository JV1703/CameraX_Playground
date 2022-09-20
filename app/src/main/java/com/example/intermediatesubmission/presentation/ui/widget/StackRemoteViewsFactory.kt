package com.example.intermediatesubmission.presentation.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.Constants.NETWORK_PAGE_SIZE
import com.example.intermediatesubmission.data.model.Story
import com.example.intermediatesubmission.data.repository.AuthRepository
import com.example.intermediatesubmission.data.repository.StoryRepository
import com.example.intermediatesubmission.presentation.ui.widget.AppWidget.Companion.EXTRA_ITEM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class StackRemoteViewsFactory @Inject constructor(
    private val mContext: Context,
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) :
    RemoteViewsService.RemoteViewsFactory {

    private var job: Job? = null
    private var token: String = ""

    private val story = ArrayList<Story>()
    private var mWidgetItems = ArrayList<Bitmap>()

    override fun onDataSetChanged() {
        TODO("Not yet implemented")
    }

    override fun onCreate() {

    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {

        job = CoroutineScope(Dispatchers.IO).launch {
            if (authRepository.getAuthTokenFromPreferencesStore.first() != "") {
                val loader = ImageLoader(mContext)
                try {
                    val storiesResponse = storyRepository.getAllStories(1, NETWORK_PAGE_SIZE)
                    if (storiesResponse.isSuccessful && storiesResponse.body() != null) {

                        storiesResponse.body()?.let {
                            for (story in it.listNetworkStory) {
                                val request = ImageRequest.Builder(mContext)
                                    .data(story.photoUrl)
                                    .allowHardware(false) // Disable hardware bitmaps.
                                    .build()
                                val result = (loader.execute(request) as SuccessResult).drawable
                                val bitmap = (result as BitmapDrawable).bitmap
                                mWidgetItems.add(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StackRemoteViewsFactory", "getViewAt - ${e.localizedMessage}")
                }
            }
        }

        // Construct a remote views item based on the widget item XML file,
        // and set the text based on the position
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        val fillInIntent = Intent().apply {
            Bundle().also { extras ->
                extras.putInt(EXTRA_ITEM, position)
                putExtras(extras)
            }
        }

        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}
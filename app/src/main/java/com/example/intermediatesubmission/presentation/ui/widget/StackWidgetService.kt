package com.example.intermediatesubmission.presentation.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.example.intermediatesubmission.data.repository.AuthRepository
import com.example.intermediatesubmission.data.repository.StoryRepository
import javax.inject.Inject

class StackWidgetService @Inject constructor(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext, authRepository, storyRepository)

}
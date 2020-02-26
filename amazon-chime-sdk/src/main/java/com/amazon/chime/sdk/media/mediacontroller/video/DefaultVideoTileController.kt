/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package com.amazon.chime.sdk.media.mediacontroller.video

import com.amazon.chime.sdk.media.clientcontroller.VideoClientController
import com.amazon.chime.sdk.utils.logger.Logger
import com.amazon.chime.webrtc.EglBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DefaultVideoTileController(
    private val logger: Logger,
    private val videoClientController: VideoClientController
) : VideoTileController {
    private val NO_PAUSE = 0
    private val videoTileMap = mutableMapOf<Int, VideoTile>()
    private val TAG = "DefaultVideoTileController"
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var videoTileObservers = mutableSetOf<VideoTileObserver>()
    private var rootEglBase: EglBase? = null

    override fun initialize() {
        logger.info(TAG, "initializing VideoTileController")
        rootEglBase = EglBase.create()
    }

    override fun destroy() {
        logger.info(TAG, "destroying VideoTileController")
        rootEglBase?.release()
    }

    /**
     * There are FOUR possible outcomes:
     * 1) Create - Someone has started sharing video
     * 2) Render / Resume - Someone is sending new frames for their video
     * 3) Pause - Someone is sending a pause frame
     * 4) Stop - Someone has stopped sharing video
     *
     * In both pause and stop cases, the frame is null but the pauseType differs
     */
    override fun onReceiveFrame(
        frame: Any?,
        attendeeId: String?,
        displayId: Int,
        pauseType: Int,
        videoId: Int
    ) {
        val tile: VideoTile? = videoTileMap[videoId]
        if (tile != null) {
            if (frame != null) {
                tile.renderFrame(frame)
            } else if (pauseType == NO_PAUSE) {
                uiScope.launch {
                    logger.info(
                        TAG,
                        "Removing video tile with videoId = $videoId & attendeeId = $attendeeId"
                    )
                    onRemoveTrack(videoId)
                }
            }
        } else {
            frame?.run {
                uiScope.launch {
                    logger.info(
                        TAG,
                        "Adding video tile with videoId = $videoId & attendeeId = $attendeeId"
                    )
                    onAddTrack(videoId, attendeeId)
                }
            }
        }
    }

    override fun addVideoTileObserver(observer: VideoTileObserver) {
        videoTileObservers.add(observer)
    }

    override fun removeVideoTileObserver(observer: VideoTileObserver) {
        videoTileObservers.remove(observer)
    }

    override fun pauseRemoteVideoTile(tileId: Int) {
        videoTileMap[tileId]?.let {
            // Local attendeeId is null because VideoClient doesn't know its attendeeId
            it.attendeeId ?: run {
                logger.warn(TAG, "Cannot pause local video tile $tileId!")
                return
            }

            logger.info(TAG, "Pausing tile $tileId")
            videoClientController.setRemotePaused(
                true,
                tileId
            )
        }
    }

    override fun resumeRemoteVideoTile(tileId: Int) {
        videoTileMap[tileId]?.let {
            // Local attendeeId is null because VideoClient doesn't know its attendeeId
            it.attendeeId ?: run {
                logger.warn(TAG, "Cannot resume local video tile $tileId!")
                return
            }

            logger.info(TAG, "Resuming tile $tileId")
            videoClientController.setRemotePaused(
                false,
                tileId
            )
        }
    }

    override fun bindVideoView(videoView: DefaultVideoRenderView, tileId: Int) {
        logger.info(TAG, "Binding VideoView to Tile with tileId = $tileId")
        videoTileMap[tileId]?.let { it.bind(rootEglBase, videoView) }
    }

    override fun unbindVideoView(tileId: Int) {
        logger.info(TAG, "Unbinding Tile with tileId = $tileId")
        videoTileMap[tileId]?.let { it.unbind() }
        videoTileMap.remove(tileId)
    }

    private fun onRemoveTrack(tileId: Int) {
        videoTileMap[tileId]?.let {
            forEachObserver { observer -> observer.onRemoveVideoTrack(it) }
        }
    }

    private fun onAddTrack(tileId: Int, profileId: String?) {
        val tile = DefaultVideoTile(logger, tileId, profileId)
        videoTileMap[tileId] = tile
        forEachObserver { observer -> observer.onAddVideoTrack(tile) }
    }

    private fun forEachObserver(observerFunction: (observer: VideoTileObserver) -> Unit) {
        for (observer in videoTileObservers) {
            observerFunction(observer)
        }
    }
}

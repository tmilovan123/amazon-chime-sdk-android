/*
 * Copyright (c) 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package com.amazon.chime.sdk.media.mediacontroller

import com.amazon.chime.sdk.session.MeetingSessionStatus

/**
 * [AudioVideoObserver] handles audio/video client events.
 */
interface AudioVideoObserver {
    /**
     * Called when the audio session is connecting or reconnecting.
     *
     * @param reconnecting: Boolean - Whether the session is reconnecting or not.
     */
    fun onAudioClientConnecting(reconnecting: Boolean)

    /**
     * Called when the audio session has started.
     *
     * @param reconnecting: Boolean - Whether the session is reconnecting or not.
     */
    fun onAudioClientStart(reconnecting: Boolean)

    /**
     * Called when the audio session has stopped from a started state with the reason
     * provided in the status.
     *
     * @param sessionStatus: [MeetingSessionStatus] - The reason why the session has stopped.
     */
    fun onAudioClientStop(sessionStatus: MeetingSessionStatus)

    /**
     * Called when audio reconnection is canceled.
     */
    fun onAudioClientReconnectionCancel()

    /**
     * Called when the connection health is recovered.
     */
    fun onConnectionRecover()

    /**
     * Called when connection is becoming poor.
     */
    fun onConnectionBecomePoor()

    /**
     * Called when the video session is connecting or reconnecting.
     */
    fun onVideoClientConnecting()

    /**
     * Called when the video session has started.
     */
    fun onVideoClientStart()

    /**
     * Called when the video session has stopped from a started state with the reason
     * provided in the status.
     *
     * @param sessionStatus: [MeetingSessionStatus] - The reason why the session has stopped.
     */
    fun onVideoClientStop(sessionStatus: MeetingSessionStatus)
}

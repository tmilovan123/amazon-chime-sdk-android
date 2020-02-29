/*
 * Copyright (c) 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package com.amazon.chime.sdk.media.clientcontroller

import com.amazon.chime.sdk.media.enums.ObservableMetric
import com.amazon.chime.sdk.media.mediacontroller.MetricsObserver
import com.xodee.client.audio.audioclient.AudioClient
import com.xodee.client.video.VideoClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DefaultClientMetricsCollectorTest {

    private lateinit var clientMetricsCollector: DefaultClientMetricsCollector

    @MockK
    private lateinit var mockMetricsObserver: MetricsObserver

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        clientMetricsCollector = DefaultClientMetricsCollector()

        // TODO: Investigate and implement mocking of passage of time intervals
    }

    @Test
    fun `onMetrics should not call observer before interval has passed`() {
        val rawMetrics = mutableMapOf(AudioClient.AUDIO_CLIENT_METRIC_POST_JB_SPK_1S_PACKETS_LOST_PERCENT to 1.0)
        clientMetricsCollector.processAudioClientMetrics(rawMetrics)

        val observableMetrics = mutableMapOf(ObservableMetric.audioPacketsReceivedFractionLossPercent to 1.0)
        verify(exactly = 0) { mockMetricsObserver.onMetricsReceive(observableMetrics) }
    }

    @Test
    fun `onMetrics for audio should call observer after interval has passed and observer should not receive any null metrics`() {
        Thread.sleep(1100)

        clientMetricsCollector.subscribeToMetrics(mockMetricsObserver)
        val rawMetrics = mutableMapOf(AudioClient.AUDIO_CLIENT_METRIC_POST_JB_SPK_1S_PACKETS_LOST_PERCENT to 1.0)
        clientMetricsCollector.processAudioClientMetrics(rawMetrics)

        val observableMetrics = mutableMapOf(ObservableMetric.audioPacketsReceivedFractionLossPercent to 1.0)
        verify(exactly = 1) { mockMetricsObserver.onMetricsReceive(observableMetrics) }
    }

    @Test
    fun `onMetrics for video should call observer after interval has passed and observer should not receive any null metrics`() {
        Thread.sleep(1100)

        clientMetricsCollector.subscribeToMetrics(mockMetricsObserver)
        val rawMetrics = mutableMapOf(VideoClient.VIDEO_AVAILABLE_RECEIVE_BANDWIDTH to 10.0)
        clientMetricsCollector.processVideoClientMetrics(rawMetrics)

        val observableMetrics = mutableMapOf(ObservableMetric.videoAvailableReceiveBandwidth to 10.0)
        verify(exactly = 1) { mockMetricsObserver.onMetricsReceive(observableMetrics) }
    }

    @Test
    fun `onMetrics should not emit non-observable metrics`() {
        clientMetricsCollector.subscribeToMetrics(mockMetricsObserver)
        val rawMetrics = mutableMapOf(AudioClient.AUDIO_CLIENT_METRIC_MIC_DEVICE_FRAMES_LOST_PERCENT to 1.0)
        clientMetricsCollector.processAudioClientMetrics(rawMetrics)

        val observableMetrics = mutableMapOf<ObservableMetric, Double>()
        verify(exactly = 0) { mockMetricsObserver.onMetricsReceive(observableMetrics) }
    }

    @Test
    fun `onMetrics should not emit invalid metrics`() {
        clientMetricsCollector.subscribeToMetrics(mockMetricsObserver)
        val rawMetrics = mutableMapOf(999 to 1.0)
        clientMetricsCollector.processAudioClientMetrics(rawMetrics)

        val observableMetrics = mutableMapOf<ObservableMetric, Double>()
        verify(exactly = 0) { mockMetricsObserver.onMetricsReceive(observableMetrics) }
    }
}

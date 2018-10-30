package com.bpt.tipi.streaming.camera_state_machine;

/**
 * Created by jpujolji on 16/03/18.
 */

public enum StateAction {
    CAMERA_START,
    CAMERA_STOP,
    AUDIO_START,
    AUDIO_STOP,
    STREAMING_START,
    STREAMING_STOP,
    LR_START,
    LR_START_WITHOUT_SOUND,
    LR_STOP,
    LR_STOP_WITHOUT_SOUND,
    PR_START,
    PR_STOP,
    TAKE_PHOTO
}
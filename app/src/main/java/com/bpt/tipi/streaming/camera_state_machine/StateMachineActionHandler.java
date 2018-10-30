package com.bpt.tipi.streaming.camera_state_machine;


import com.bpt.tipi.streaming.service.CameraService;

/**
 * Created by jpujolji on 16/03/18.
 */

public class StateMachineActionHandler {
    static void manageState(CameraService cameraService, StateAction... actions) {
        for (StateAction action : actions) {
            switch (action) {
                case CAMERA_START:
                    cameraService.initCamera();
                    break;
                case CAMERA_STOP:
                    cameraService.finishCamera();
                    break;
                case AUDIO_START:
                    cameraService.initAudioRecord();
                    break;
                case AUDIO_STOP:
                    cameraService.finishAudioRecord();
                    break;
                case LR_START:
                    cameraService.configLocalRecorder(true);
                    cameraService.startLocalRecorder(true);
                    break;
                case LR_START_WITHOUT_SOUND:
                    cameraService.configLocalRecorder(false);
                    cameraService.startLocalRecorder(false);
                    break;
                case LR_STOP:
                    cameraService.stopLocalRecorder(true);
                    break;
                case LR_STOP_WITHOUT_SOUND:
                    cameraService.stopLocalRecorder(false);
                    break;
                case STREAMING_START:
                    cameraService.configStreamingRecorder();
                    cameraService.startStreamingRecorder();
                    break;
                case STREAMING_STOP:
                    cameraService.stopStreamingRecorder();
                    break;
                case TAKE_PHOTO:
                    cameraService.takePhoto();
                    break;
            }
        }
    }

}

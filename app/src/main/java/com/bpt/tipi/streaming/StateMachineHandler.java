package com.bpt.tipi.streaming;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bpt.tipi.streaming.service.RecorderService;

/**
 * Created by jpujolji on 15/03/18.
 */

public class StateMachineHandler extends Handler {

    private RecorderService recorderService;

    private StateMachine state = StateMachine.INIT;

    public static final int LOCAL_RECORDER_PRESSED = 1;
    public static final int STREAMING = 3;
    public static final int POST_RECORDING = 4;
    public static final int TAKE_PHOTO = 5;

    public static final int PLAY_SOUND = 0;
    public static final int DO_NOT_PLAY_SOUND = 1;

    public StateMachineHandler(RecorderService recorderService) {
        Log.i("Depuracion", "New instance StateMachineHandler");
        this.recorderService = recorderService;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.i("Depuracion", "Prev State " + state);
        switch (msg.what) {
            case LOCAL_RECORDER_PRESSED:
                switch (state) {
                    case INIT:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.LR;
                            StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_START, StateAction.AUDIO_START, StateAction.LR_START);
                        }
                        break;
                    case LR:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.INIT;
                            StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_STOP, StateAction.AUDIO_STOP, StateAction.LR_STOP);
                        }
                        break;
                    case ST:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.LR_ST;
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_START);
                        }
                        break;
                    case LR_ST:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.ST;
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP);
                        }
                        break;
                    case PH:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.AUDIO_START, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.LR_PH;
                            StateMachineActionHandler.manageState(recorderService, StateAction.AUDIO_START, StateAction.LR_START);
                        }
                        break;
                    case LR_PH:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.PH;
                            StateMachineActionHandler.manageState(recorderService, StateAction.AUDIO_STOP, StateAction.LR_STOP);
                        }
                        break;
                    case ST_PH:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.LR_ST_PH;
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_START);
                        }
                        break;
                    case LR_ST_PH:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            state = StateMachine.ST_PH;
                            StateMachineActionHandler.manageState(recorderService, StateAction.LR_STOP);
                        }
                        break;
                }
                break;
            case STREAMING:
                switch (state) {
                    case INIT:
                        state = StateMachine.ST;
                        StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_START, StateAction.AUDIO_START, StateAction.STREAMING_START);
                        break;
                    case ST:
                        state = StateMachine.INIT;
                        StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_STOP, StateAction.AUDIO_STOP, StateAction.STREAMING_STOP);
                        break;
                    case LR:
                        state = StateMachine.LR_ST;
                        StateMachineActionHandler.manageState(recorderService, StateAction.STREAMING_START);
                        break;
                    case LR_ST:
                        state = StateMachine.LR;
                        StateMachineActionHandler.manageState(recorderService, StateAction.STREAMING_STOP);
                        break;
                    case PH:
                        state = StateMachine.ST_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.AUDIO_START, StateAction.STREAMING_START);
                        break;
                    case ST_PH:
                        state = StateMachine.PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.AUDIO_STOP, StateAction.STREAMING_STOP);
                        break;
                    case LR_PH:
                        state = StateMachine.LR_ST_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.STREAMING_START);
                        break;
                    case LR_ST_PH:
                        state = StateMachine.LR_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.STREAMING_STOP);
                        break;
                }
                break;
            case TAKE_PHOTO:
                switch (state) {
                    case INIT:
                        state = StateMachine.PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_START, StateAction.TAKE_PHOTO);
                        break;
                    case PH:
                        state = StateMachine.INIT;
                        StateMachineActionHandler.manageState(recorderService, StateAction.CAMERA_STOP);
                        break;
                    case ST:
                        state = StateMachine.ST_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.TAKE_PHOTO);
                        break;
                    case LR:
                        state = StateMachine.LR_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.TAKE_PHOTO);
                        break;
                    case LR_ST:
                        state = StateMachine.LR_ST_PH;
                        StateMachineActionHandler.manageState(recorderService, StateAction.TAKE_PHOTO);
                        break;
                    case LR_ST_PH:
                        state = StateMachine.LR_ST;
                        break;
                    case ST_PH:
                        state = StateMachine.ST;
                        break;
                    case LR_PH:
                        state = StateMachine.LR;
                        break;
                }
                break;
        }
    }
}
package com.bpt.tipi.streaming;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.service.RecorderService;

/**
 * Created by jpujolji on 15/03/18.
 */

public class StateMachineHandler extends Handler {

    private RecorderService mRecorderService;

    private StateMachine state = StateMachine.INIT;

    public static final int LOCAL_RECORDER_PRESSED = 1;
    public static final int STREAMING = 3;
    public static final int POST_RECORDING = 4;

    public static final int PLAY_SOUND = 0;
    public static final int DO_NOT_PLAY_SOUND = 1;

    public StateMachineHandler(RecorderService recorderService) {
        Log.i("Depuracion", "New instance StateMachineHandler");
        this.mRecorderService = recorderService;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.i("Depuracion", "Prev State " + state);
        switch (msg.what) {
            case LOCAL_RECORDER_PRESSED:
                switch (state) {
                    case INIT:
                        state = StateMachine.LR;
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_START_WITHOUT_SOUND);
                        } else {
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_START);
                        }
                        break;
                    case ST:
                        state = StateMachine.LR_ST;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_START);
                        break;
                    case LR:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            state = StateMachine.INIT;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP_WITHOUT_SOUND);
                        } else if (ConfigHelper.getLocalPostRecorder(mRecorderService.context)) {
                            state = StateMachine.LR_PR;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_START);
                        } else {
                            state = StateMachine.INIT;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP);
                        }
                        break;
                    case LR_ST:
                        if (msg.arg1 == DO_NOT_PLAY_SOUND) {
                            state = StateMachine.ST;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP_WITHOUT_SOUND);
                        } else if (ConfigHelper.getLocalPostRecorder(mRecorderService.context)) {
                            state = StateMachine.LR_ST_PR;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_START);
                        } else {
                            state = StateMachine.ST;
                            StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP);
                        }
                        break;
                    case LR_PR:
                        state = StateMachine.LR;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_STOP, StateAction.LR_START);
                        break;
                    case LR_ST_PR:
                        state = StateMachine.LR_ST;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_STOP, StateAction.LR_START);
                        break;
                }
                break;
            case STREAMING:
                switch (state) {
                    case INIT:
                        state = StateMachine.ST;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.STREAMING_START);
                        break;
                    case ST:
                        state = StateMachine.INIT;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.STREAMING_STOP);
                        break;
                    case LR:
                        state = StateMachine.LR_ST;
                        //StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.STREAMING_START, StateAction.LR_START_WITHOUT_SOUND);
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.STREAMING_START);
                        break;
                    case LR_ST:
                        state = StateMachine.LR;
                        //StateMachineActionHandler.manageState(mRecorderService, StateAction.LR_STOP_WITHOUT_SOUND, StateAction.STREAMING_STOP, StateAction.LR_START_WITHOUT_SOUND);
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.STREAMING_STOP);
                        break;
                    case LR_PR:
                        state = StateMachine.ST;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_STOP, StateAction.STREAMING_START);
                        break;
                    case LR_ST_PR:
                        state = StateMachine.INIT;
                        StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_STOP, StateAction.STREAMING_STOP);

                }
                break;
            case POST_RECORDING:
                state = StateMachine.INIT;
                StateMachineActionHandler.manageState(mRecorderService, StateAction.PR_STOP);
                break;
        }
        Log.i("Depuracion", "Curr State " + state);
        if (state == StateMachine.INIT) {
            IrHelper.setIrState(IrHelper.STATE_OFF);
        }
    }
}
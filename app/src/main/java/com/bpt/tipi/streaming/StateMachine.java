package com.bpt.tipi.streaming;

/**
 * Created by jpujolji on 16/03/18.
 */

public enum StateMachine {
    INIT,
    PH,
    ST,
    LR,
    LR_SOS,
    LR_PR,
    LR_ST,
    LR_ST_SOS,
    LR_ST_PR,
    ST_PH,
    LR_PH,
    LR_ST_PH
}
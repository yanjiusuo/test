package com.jd.workflow.soap.common.lang;

public interface IStepProgressListener {
    void onStep(long step);

    void begin();

    void end();
}

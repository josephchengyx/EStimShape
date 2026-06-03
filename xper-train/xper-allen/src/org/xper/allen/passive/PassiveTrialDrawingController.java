package org.xper.allen.passive;

import org.xper.allen.app.fixation.PngScene;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.vo.TrialContext;

public interface PassiveTrialDrawingController extends TrialDrawingController {
    void showSample(PassiveExperimentTask task, TrialContext context);
    void showDelay(PassiveExperimentTask task, TrialContext context);
    void showMatch(PassiveExperimentTask task, TrialContext context);
}

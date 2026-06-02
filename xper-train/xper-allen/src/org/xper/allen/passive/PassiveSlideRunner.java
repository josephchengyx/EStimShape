package org.xper.allen.passive;

import org.apache.log4j.Logger;
import org.xper.classic.ClassicSlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialContext;
import org.xper.classic.vo.TrialResult;
import org.xper.experiment.TaskDoneCache;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;

public class PassiveSlideRunner extends ClassicSlideRunner {
    static Logger logger = Logger.getLogger(PassiveSlideRunner.class);

    @Override
    public TrialResult runSlides(SlideTrialExperimentState stateObject, ThreadHelper threadHelper) {
        TrialDrawingController drawingController = stateObject.getDrawingController();
        PassiveExperimentTask currentTask = (PassiveExperimentTask) stateObject.getCurrentTask();
        TrialContext currentContext = stateObject.getCurrentContext();
        TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
        TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();

        try {
            // draw cue stimulus
            currentTask.setStimSpec(currentTask.getSampleSpec());
            TrialResult result = doSlide(0, stateObject);
            if (result != TrialResult.SLIDE_OK) {
                return result;
            }

            // inter slide interval
            result = waitInterSlideInterval(stateObject, threadHelper);
            if (result != TrialResult.SLIDE_OK) {
                return result;
            }

            // prepare next slide
            currentTask.setStimSpec(currentTask.getMatchSpec());
            currentContext.setSlideIndex(1);
            drawingController.prepareNextSlide(currentTask, currentContext);

            // draw match stimulus
            result = doSlide(1, stateObject);
            if (result != TrialResult.SLIDE_OK) {
                return result;
            }

            // slide done successfully
            taskDoneCache.put(currentTask, globalTimeClient.currentTimeMicros(), false);
            currentTask = null;
            stateObject.setCurrentTask(currentTask);
//            punisher.resetPunishment();
            return TrialResult.TRIAL_COMPLETE;
            // end of SlideRunner.runSlide
        } finally {
            try {
                cleanupTask(stateObject);
            } catch (Exception e) {
                logger.warn(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

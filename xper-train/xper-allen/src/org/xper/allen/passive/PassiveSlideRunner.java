package org.xper.allen.passive;

import org.apache.log4j.Logger;
import org.xper.classic.ClassicSlideRunner;
import org.xper.classic.SlideEventListener;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialContext;
import org.xper.classic.vo.TrialResult;
import org.xper.experiment.EyeController;
import org.xper.experiment.TaskDoneCache;
import org.xper.time.TimeUtil;
import org.xper.util.EventUtil;
import org.xper.util.ThreadHelper;

import java.sql.Timestamp;
import java.util.List;

public class PassiveSlideRunner extends ClassicSlideRunner {
    static Logger logger = Logger.getLogger(PassiveSlideRunner.class);

    @Override
    public TrialResult runSlides(SlideTrialExperimentState stateObject, ThreadHelper threadHelper) {
        PassiveTrialDrawingController drawingController = (PassiveTrialDrawingController) stateObject.getDrawingController();
        PassiveExperimentTask currentTask = (PassiveExperimentTask) stateObject.getCurrentTask();
        TrialContext currentContext = stateObject.getCurrentContext();
        TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
        TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();
        List<? extends SlideEventListener> slideEventListeners = stateObject.getSlideEventListeners();
        EyeController eyeController = stateObject.getEyeController();
        TimeUtil timeUtil = stateObject.getLocalTimeUtil();

        try {
            // draw cue stimulus
            drawingController.prepareSample(currentTask, currentContext);
            drawingController.showSample(currentTask, currentContext);
            TrialResult result = doSlide(0, stateObject);
            if (result != TrialResult.SLIDE_OK) {
                return result;
            }

            // inter slide interval
            drawingController.showDelay(currentTask, currentContext);
            result = waitInterSlideInterval(stateObject, threadHelper);
            if (result != TrialResult.SLIDE_OK) {
                return result;
            }

            // prepare next slide
            currentTask.setStimSpec(currentTask.getMatchSpec());
            currentContext.setSlideIndex(1);
            drawingController.prepareNextSlide(currentTask, currentContext);

            // draw match stimulus
            drawingController.prepareMatch(currentTask, currentContext);
            drawingController.showMatch(currentTask, currentContext);
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

    @Override
    public TrialResult doSlide (int i, SlideTrialExperimentState stateObject) {
        PassiveTrialDrawingController drawingController = (PassiveTrialDrawingController) stateObject.getDrawingController();
        PassiveExperimentTask currentTask = (PassiveExperimentTask) stateObject.getCurrentTask();
        TrialContext currentContext = stateObject.getCurrentContext();
        List<? extends SlideEventListener> slideEventListeners = stateObject.getSlideEventListeners();
        EyeController eyeController = stateObject.getEyeController();
        TimeUtil timeUtil = stateObject.getLocalTimeUtil();

        // show current slide
        long slideOnLocalTime = timeUtil.currentTimeMicros();
        currentContext.setCurrentSlideOnTime(slideOnLocalTime);
        long taskId;
        try {
            taskId = currentTask.getTaskId();
        } catch (NullPointerException e){
            taskId = timeUtil.currentTimeMicros();
        }
        EventUtil.fireSlideOnEvent(i, slideOnLocalTime,
                slideEventListeners, taskId);

        // wait for current slide to finish
        do {
            if (!eyeController.isEyeIn()) {
                breakTrial(stateObject);
                currentContext.setAnimationFrameIndex(0);
//                punisher.punish();
                return TrialResult.EYE_BREAK;
            }
            if (stateObject.isAnimation()) {
                currentContext.setAnimationFrameIndex(currentContext.getAnimationFrameIndex()+1);
                drawingController.animateSlide(currentTask,
                        currentContext);
                if (logger.isDebugEnabled()) {
                    long t = timeUtil.currentTimeMicros();
                    logger.debug(new Timestamp(t/1000).toString() + " " + t % 1000 + " frame: " + currentContext.getAnimationFrameIndex());
                }
            } else{
            }
        } while (timeUtil.currentTimeMicros() < slideOnLocalTime
                + stateObject.getSlideLength() * 1000);

        // finish current slide
        drawingController.slideFinish(currentTask, currentContext);
        long slideOffLocalTime = timeUtil.currentTimeMicros();
        currentContext.setCurrentSlideOffTime(slideOffLocalTime);
        EventUtil.fireSlideOffEvent(i, slideOffLocalTime,
                currentContext.getAnimationFrameIndex(),
                slideEventListeners, taskId);
        currentContext.setAnimationFrameIndex(0);

        return TrialResult.SLIDE_OK;
    }
}

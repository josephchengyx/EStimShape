package org.xper.allen.passive;

import org.xper.Dependency;
import org.xper.allen.drawing.LeftRightScreenMarker;
import org.xper.allen.nafc.experiment.ScreenShotter;
import org.xper.classic.MarkStimTrialDrawingController;
import org.xper.classic.vo.TrialContext;
import org.xper.experiment.ExperimentTask;

public class PassiveMarkStimTrialDrawingController extends MarkStimTrialDrawingController implements PassiveTrialDrawingController {
    @Dependency
    ScreenShotter screenShotter;

    @Dependency
    LeftRightScreenMarker leftRightMarker;

    @Override
    public void prepareSample(PassiveExperimentTask task, TrialContext context) {
        task.setStimSpec(task.getSampleSpec());
    }

    @Override
    public void showSample(PassiveExperimentTask task, TrialContext context) {
        leftRightMarker.right();
        drawTaskScene(task, context);
        getWindow().swapBuffers();
    }

    @Override
    public void showDelay(PassiveExperimentTask task, TrialContext context) {
        leftRightMarker.left();
        getTaskScene().drawBlank(context, true, true);
        getWindow().swapBuffers();
    }

    @Override
    public void prepareMatch(PassiveExperimentTask task, TrialContext context) {
        task.setStimSpec(task.getMatchSpec());
    }

    @Override
    public void showMatch(PassiveExperimentTask task, TrialContext context) {
        leftRightMarker.right();
        drawTaskScene(task, context);
        getWindow().swapBuffers();
    }

    @Override
    public void prepareFirstSlide(ExperimentTask task, TrialContext context) {}
    @Override
    public void prepareNextSlide(ExperimentTask task, TrialContext context) {}
    @Override
    public void slideFinish(ExperimentTask task, TrialContext context) {}

    public ScreenShotter getScreenShotter() {
        return screenShotter;
    }

    public void setScreenShotter(ScreenShotter screenShotter) {
        this.screenShotter = screenShotter;
    }

    public LeftRightScreenMarker getLeftRightMarker() {
        return leftRightMarker;
    }

    public void setLeftRightMarker(LeftRightScreenMarker leftRightMarker) {
        this.leftRightMarker = leftRightMarker;
    }
}

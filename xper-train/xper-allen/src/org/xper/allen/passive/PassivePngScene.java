package org.xper.allen.passive;

import org.lwjgl.opengl.GL11;
import org.xper.Dependency;
import org.xper.classic.vo.TrialContext;
import org.xper.drawing.AbstractTaskScene;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.Drawable;
import org.xper.experiment.ExperimentTask;
import org.xper.rfplot.drawing.png.ImageDimensions;
import org.xper.rfplot.drawing.png.PngSpec;
import org.xper.rfplot.drawing.png.TranslatableResizableImages;

public class PassivePngScene extends AbstractTaskScene implements PassiveTaskScene{
    @Dependency
    double distance;
    @Dependency
    double screenWidth;
    @Dependency
    double screenHeight;

    TranslatableResizableImages image;

    Coordinates2D pngLocation;
    ImageDimensions pngDimensions;
    double pngAlpha;

    public void trialStart(TrialContext context) {
        image = new TranslatableResizableImages(1);
        image.initTextures();
    }

    public void initGL(int w, int h) {

        super.setUseStencil(true);
        super.initGL(w, h);
        //System.out.println("JK 32838 w = " + screenWidth + ", h = " + screenHeight);
        GL11.glViewport(0,0,w,h);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GL11.glOrtho(0, w, h, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void setSample(PassiveExperimentTask task) {
        PngSpec sampleSpec = PngSpec.fromXml(task.getSampleSpec());
        pngLocation = new Coordinates2D(sampleSpec.getxCenter(), sampleSpec.getyCenter());
        pngDimensions = sampleSpec.getDimensions();
        image.loadTexture(sampleSpec.getPath(), 0);
    }

    public void setMatch(PassiveExperimentTask task) {
        PngSpec matchSpec = PngSpec.fromXml(task.getMatchSpec());
        pngLocation = new Coordinates2D(matchSpec.getxCenter(), matchSpec.getyCenter());
        pngDimensions = matchSpec.getDimensions();
        image.loadTexture(matchSpec.getPath(), 0);
    }

    public void drawTask(Context context, final boolean fixationOn) {
        // clear the whole screen before define view ports in renderer
        blankScreen.draw(null);
        renderer.draw(new Drawable() {
            public void draw(Context context) {
                if (useStencil) {
                    // 0 will pass for stimulus region
                    GL11.glStencilFunc(GL11.GL_EQUAL, 0, 1);
                }
                drawStimulus(context);
                if (useStencil) {
                    // 1 will pass for fixation and marker regions
                    GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
                }

                if (true) {
                    getFixation().draw(context);
                }
                marker.draw(context);
                if (useStencil) {
                    // 0 will pass for stimulus region
                    GL11.glStencilFunc(GL11.GL_EQUAL, 0, 1);
                }
            }}, context);
    }

    @Override
    public void drawStimulus(Context context) {

        int index = 0; //Should be zero, the sample is assigned index of zero.
        image.draw(context, index, pngLocation, pngDimensions);

    }

    public void setTask(ExperimentTask task) {

    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(double screenWidth) {
        this.screenWidth = screenWidth;
    }

    public double getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(double screenHeight) {
        this.screenHeight = screenHeight;
    }
}

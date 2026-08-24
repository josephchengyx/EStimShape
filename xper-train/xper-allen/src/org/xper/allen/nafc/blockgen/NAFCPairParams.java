package org.xper.allen.nafc.blockgen;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation parameters for {@link NAFCPairBlockGen}.
 *
 * Field initializers are the canonical defaults, so a freshly constructed
 * instance is a runnable configuration. Serialized into the NAFCPairParams
 * table as a per-session record of what produced a block of training trials.
 */
public class NAFCPairParams {

    private int    numTrials                   = 100;
    private int    numChoices                  = 4;
    private double width                       = 30.0;
    private double height                      = 30.0;
    private double eyeWinSize                  = 5.0;
    private double choiceRadiusLowerLim        = 25.0;
    private double choiceRadiusUpperLim        = 25.0;
    private double alphaLowerLim               = 1.0;
    private double alphaUpperLim               = 1.0;
    private double distractorDistanceLowerLim  = 0.0;
    private double distractorDistanceUpperLim  = 0.0;
    private double distractorScaleLowerLim     = 1.0;
    private double distractorScaleUpperLim     = 1.0;
    private int    distractorPresentationDelay = 0;

    private static final XStream xstream = new XStream();

    static {
        xstream.alias("NAFCPairParams", NAFCPairParams.class);
    }

    public NAFCPairParams() {
    }

    public String toXml() {
        return xstream.toXML(this);
    }

    public static NAFCPairParams fromXml(String xml) {
        return (NAFCPairParams) xstream.fromXML(xml);
    }

    public static String listToXml(List<NAFCPairParams> conditions) {
        return xstream.toXML(new ArrayList<NAFCPairParams>(conditions));
    }

    /** Accepts either a list of conditions or a single legacy condition. */
    @SuppressWarnings("unchecked")
    public static List<NAFCPairParams> listFromXml(String xml) {
        Object parsed = xstream.fromXML(xml);
        if (parsed instanceof List) {
            return new ArrayList<NAFCPairParams>((List<NAFCPairParams>) parsed);
        }
        List<NAFCPairParams> single = new ArrayList<NAFCPairParams>();
        single.add((NAFCPairParams) parsed);
        return single;
    }

    public int getNumTrials() { return numTrials; }
    public void setNumTrials(int numTrials) { this.numTrials = numTrials; }

    public int getNumChoices() { return numChoices; }
    public void setNumChoices(int numChoices) { this.numChoices = numChoices; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getEyeWinSize() { return eyeWinSize; }
    public void setEyeWinSize(double eyeWinSize) { this.eyeWinSize = eyeWinSize; }

    public double getChoiceRadiusLowerLim() { return choiceRadiusLowerLim; }
    public void setChoiceRadiusLowerLim(double v) { this.choiceRadiusLowerLim = v; }

    public double getChoiceRadiusUpperLim() { return choiceRadiusUpperLim; }
    public void setChoiceRadiusUpperLim(double v) { this.choiceRadiusUpperLim = v; }

    public double getAlphaLowerLim() { return alphaLowerLim; }
    public void setAlphaLowerLim(double v) { this.alphaLowerLim = v; }

    public double getAlphaUpperLim() { return alphaUpperLim; }
    public void setAlphaUpperLim(double v) { this.alphaUpperLim = v; }

    public double getDistractorDistanceLowerLim() { return distractorDistanceLowerLim; }
    public void setDistractorDistanceLowerLim(double v) { this.distractorDistanceLowerLim = v; }

    public double getDistractorDistanceUpperLim() { return distractorDistanceUpperLim; }
    public void setDistractorDistanceUpperLim(double v) { this.distractorDistanceUpperLim = v; }

    public double getDistractorScaleLowerLim() { return distractorScaleLowerLim; }
    public void setDistractorScaleLowerLim(double v) { this.distractorScaleLowerLim = v; }

    public double getDistractorScaleUpperLim() { return distractorScaleUpperLim; }
    public void setDistractorScaleUpperLim(double v) { this.distractorScaleUpperLim = v; }

    public int getDistractorPresentationDelay() { return distractorPresentationDelay; }
    public void setDistractorPresentationDelay(int v) { this.distractorPresentationDelay = v; }
}
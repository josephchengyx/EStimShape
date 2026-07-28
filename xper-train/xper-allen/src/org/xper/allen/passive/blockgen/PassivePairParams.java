package org.xper.allen.passive.blockgen;

import com.thoughtworks.xstream.XStream;

/**
 * Generation parameters for {@link PassivePairBlockGen}. Field initializers are
 * the canonical defaults; serialized into the PassivePairParams table as a
 * per-session record.
 */
public class PassivePairParams {

    private int    numTrials = 100;
    private double width     = 30.0;
    private double height    = 30.0;

    private static final XStream xstream = new XStream();

    static {
        xstream.alias("PassivePairParams", PassivePairParams.class);
    }

    public PassivePairParams() {
    }

    public String toXml() {
        return xstream.toXML(this);
    }

    public static PassivePairParams fromXml(String xml) {
        return (PassivePairParams) xstream.fromXML(xml);
    }

    public int getNumTrials() { return numTrials; }
    public void setNumTrials(int numTrials) { this.numTrials = numTrials; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
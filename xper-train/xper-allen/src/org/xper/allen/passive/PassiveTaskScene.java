package org.xper.allen.passive;

import org.xper.drawing.TaskScene;

public interface PassiveTaskScene extends TaskScene{
    public void setSample(PassiveExperimentTask task);
    public void setMatch(PassiveExperimentTask task);
}

package org.xper.allen.passive.experiment;

import org.xper.experiment.ExperimentTask;

public class PassiveExperimentTask extends ExperimentTask {
    String sampleSpec;
    String matchSpec;
    long sampleSpecId;
    long matchSpecId;

    public String getSampleSpec() {
        return sampleSpec;
    }

    public void setSampleSpec(String sampleSpec) {
        this.sampleSpec = sampleSpec;
    }

    public String getMatchSpec() {
        return matchSpec;
    }

    public void setMatchSpec(String matchSpec) {
        this.matchSpec = matchSpec;
    }

    public long getSampleSpecId() {
        return sampleSpecId;
    }

    public void setSampleSpecId(long sampleSpecId) {
        this.sampleSpecId = sampleSpecId;
    }

    public long getMatchSpecId() {
        return matchSpecId;
    }

    public void setMatchSpecId(long matchSpecId) {
        this.matchSpecId = matchSpecId;
    }
}

package org.xper.allen.nafc.experiment.mock;

import org.xper.allen.nafc.experiment.NAFCDatabaseTaskDataSource;
import org.xper.allen.nafc.experiment.NAFCExperimentTask;
import org.xper.experiment.ExperimentTask;

public class NAFCMockDatabaseTaskDataSource extends NAFCDatabaseTaskDataSource {

    @Override
    public NAFCExperimentTask getNextTask() {
        return new NAFCMockExperimentTask();
    }

    @Override
    public void ungetTask(ExperimentTask t) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}

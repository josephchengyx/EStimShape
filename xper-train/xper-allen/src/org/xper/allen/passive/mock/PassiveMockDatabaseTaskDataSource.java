package org.xper.allen.passive.mock;

import org.xper.allen.passive.PassiveDatabaseTaskDataSource;
import org.xper.allen.passive.PassiveExperimentTask;
import org.xper.experiment.ExperimentTask;

public class PassiveMockDatabaseTaskDataSource extends PassiveDatabaseTaskDataSource {
    @Override
    public PassiveExperimentTask getNextTask() {
        return new PassiveMockExperimentTask();
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
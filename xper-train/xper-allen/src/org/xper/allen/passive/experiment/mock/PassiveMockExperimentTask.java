package org.xper.allen.passive.experiment.mock;

import org.xper.allen.passive.experiment.PassiveExperimentTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PassiveMockExperimentTask extends PassiveExperimentTask {

    private static String readFileAsString(String filename) throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(filename)));
    }

    public PassiveMockExperimentTask() {
        String sampleSpec = "";
        String matchSpec = "";
        long cueId = 0;
        long matchId = 1;

        try {
            sampleSpec = readFileAsString(
                    "/home/connorlab/Documents/GitHub/EStimShape/xper-train/xper-allen/src/org/xper/allen/nafc/experiment/mock/NAFCMockCueSpec.txt");
            matchSpec = readFileAsString(
                    "/home/connorlab/Documents/GitHub/EStimShape/xper-train/xper-allen/src/org/xper/allen/nafc/experiment/mock/NAFCMockTargetSpec.txt");;
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setSampleSpec(sampleSpec);
        this.setMatchSpec(matchSpec);
        this.setSampleSpecId(cueId);
        this.setMatchSpecId(matchId);
        this.setStimSpec(sampleSpec);
    }
}

package org.xper.allen.nafc.experiment.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import org.xper.allen.nafc.experiment.NAFCExperimentTask;
import org.xper.allen.nafc.experiment.RewardPolicy;
import org.xper.drawing.Coordinates2D;
import org.xper.rfplot.drawing.png.PngSpec;

public class NAFCMockExperimentTask extends NAFCExperimentTask {
    private static final Random RNG = new Random();
    private static final double LEFT_X  = -30.31088913245535;
    private static final double RIGHT_X = 30.31088913245535;

    private static String readFileAsString(String filename) throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(filename)));
    }

    private static String withXCenter(String specXml, double xCenter) {
        PngSpec spec = PngSpec.fromXml(specXml);
        spec.setxCenter(xCenter);
        return spec.toXml();
    }

    public NAFCMockExperimentTask() {
        String cueSpec = "";
        String distractorSpec = "";
        String targetSpec = "";
        long cueId = 0;
        long targetId = 1;
        long distractorId = 2;

        try {
            cueSpec = readFileAsString(
                    "/home/connorlab/Documents/GitHub/EStimShape/xper-train/xper-allen/src/org/xper/allen/nafc/experiment/mock/NAFCMockCueSpec.txt");
            distractorSpec = readFileAsString(
                    "/home/connorlab/Documents/GitHub/EStimShape/xper-train/xper-allen/src/org/xper/allen/nafc/experiment/mock/NAFCMockDistractorSpec.txt");
            targetSpec = readFileAsString(
                    "/home/connorlab/Documents/GitHub/EStimShape/xper-train/xper-allen/src/org/xper/allen/nafc/experiment/mock/NAFCMockTargetSpec.txt");;
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setTargetEyeWinCoords(new Coordinates2D[] {
                new Coordinates2D(LEFT_X, 0),
                new Coordinates2D(RIGHT_X, 0),
        });
        this.setTargetEyeWinSize(new double[] {14.142135623730951, 14.142135623730951});
        this.setSampleSpec(cueSpec);
        this.setSampleSpecId(cueId);
        this.setRewardPolicy(RewardPolicy.LIST);

        if (RNG.nextDouble() < 0.5) {
            this.setChoiceSpec(new String[] {
                    withXCenter(targetSpec, LEFT_X),
                    withXCenter(distractorSpec, RIGHT_X)
            });
            this.setChoiceSpecId(new long[] {targetId, distractorId});
            this.setRewardList(new int[] {0});
        } else {
            this.setChoiceSpec(new String[] {
                    withXCenter(distractorSpec, LEFT_X),
                    withXCenter(targetSpec, RIGHT_X)
            });
            this.setChoiceSpecId(new long[] {distractorId, targetId});
            this.setRewardList(new int[] {1});
        }
    }
}

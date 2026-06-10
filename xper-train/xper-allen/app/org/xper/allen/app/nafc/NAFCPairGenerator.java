package org.xper.allen.app.nafc;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.allen.nafc.blockgen.NAFCPairBlockGen;
import org.xper.util.FileUtil;

public class NAFCPairGenerator {
    public static void main(String[] args) {
        // args 0      number of trials
        // args 1      number of choices per trial
        // args 2-3    imageSize width x height
        // args 4      eyeWinSize
        // args 5-6    radius limits for choice
        // args 7-8    alpha for distractors (lower-upper limit)
        // args 9-10   extra distance distractors are from their default location (lower-upper limit)
        // args 11-12  distractor size scale (lower-upper limit)

        int numTrials = Integer.parseInt(args[0]);
        int numChoices = Integer.parseInt(args[1]);
        double width = Double.parseDouble(args[2]);
        double height = Double.parseDouble(args[3]);
        double eyeWinSize = Double.parseDouble(args[4]);
        double choiceRadiusLowerLim = Double.parseDouble(args[5]);
        double choiceRadiusUpperLim = Double.parseDouble(args[6]);
        double alphaLowerLim = Double.parseDouble(args[7]);
        double alphaUpperLim = Double.parseDouble(args[8]);
        double distractorDistanceLowerLim = Double.parseDouble(args[9]);
        double distractorDistanceUpperLim = Double.parseDouble(args[10]);
        double distractorScaleLowerLim = Double.parseDouble(args[11]);
        double distractorScaleUpperLim = Double.parseDouble(args[12]);

        JavaConfigApplicationContext context = new JavaConfigApplicationContext(
                FileUtil.loadConfigClass("experiment.config_class"));

        NAFCPairBlockGen gen = context.getBean(NAFCPairBlockGen.class);

        try {
            gen.toString();
            gen.generate(numTrials, numChoices,
                    width, height, eyeWinSize,
                    choiceRadiusLowerLim, choiceRadiusUpperLim,
                    alphaLowerLim, alphaUpperLim,
                    distractorDistanceLowerLim, distractorDistanceUpperLim,
                    distractorScaleLowerLim, distractorScaleUpperLim);

        }
        catch(Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}

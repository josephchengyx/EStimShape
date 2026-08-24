package org.xper.allen.nafc.blockgen;

import org.xper.Dependency;
import org.xper.allen.nafc.experiment.RewardPolicy;
import org.xper.allen.specs.NAFCStimSpecSpec;
import org.xper.allen.util.AllenDbUtil;
import org.xper.drawing.Coordinates2D;
import org.xper.exception.VariableNotFoundException;
import org.xper.rfplot.drawing.png.ImageDimensions;
import org.xper.rfplot.drawing.png.PngSpec;
import org.xper.time.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.xper.allen.nafc.blockgen.NAFCCoordinateAssigner.inclusiveRandomDouble;

public class NAFCPairBlockGen extends AbstractTrialGenerator {
    @Dependency
    AllenDbUtil dbUtil;
    @Dependency
    TimeUtil globalTimeUtil;
    @Dependency
    String experimentStimLibPath;

    static final String TASK_TYPE = "nafc";
    Random r = new Random();
    long genId = 1;

    /**
     * Mixed-condition generation. Each condition carries its own trial count;
     * trials are expanded, shuffled together, and written in shuffled order so
     * conditions interleave. Task ids are assigned inside the write loop, so
     * shuffled order becomes presentation order.
     */
    public void generate(List<NAFCPairParams> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("No conditions supplied.");
        }

        List<NAFCPairParams> paramsPerTrial = new ArrayList<>();
        for (NAFCPairParams condition : conditions) {
            for (int j = 0; j < condition.getNumTrials(); j++) {
                paramsPerTrial.add(condition);
            }
        }
        if (paramsPerTrial.isEmpty()) {
            throw new IllegalArgumentException("Conditions supplied, but total number of trials is 0.");
        }
        Collections.shuffle(paramsPerTrial);

        List<String> stimList = dbUtil.readStimulusLibrary(TASK_TYPE);
        List<String[]> pairs  = dbUtil.readAssociatePairs(TASK_TYPE);
        Collections.shuffle(pairs);

        if (pairs.isEmpty()) {
            throw new IllegalStateException(
                    "NAFCPairLibrary is empty — seed the session pair library before generating.");
        }

        int maxChoices = 0;
        for (NAFCPairParams condition : conditions) {
            maxChoices = Math.max(maxChoices, condition.getNumChoices());
        }
        if (stimList.size() < maxChoices + 1) {
            throw new IllegalStateException(
                    "NAFCStimLibrary has " + stimList.size() + " entries but up to "
                            + (maxChoices - 1) + " distractors are needed per trial.");
        }

        try {
            /**
             * Gen ID is important for xper to be able to load new tasks on the fly. It will only do so if the generation Id is upticked.
             */
            genId = dbUtil.readReadyGenerationInfo().getGenId() + 1;
        } catch (VariableNotFoundException e) {
            dbUtil.writeReadyGenerationInfo(genId, 0);
        }

        for (int i = 0; i < paramsPerTrial.size(); i++) {
            writeTrial(paramsPerTrial.get(i), i, pairs, stimList);
        }

        dbUtil.updateReadyGenerationInfo(genId, paramsPerTrial.size());
        System.out.println("Done generating " + paramsPerTrial.size() + " NAFC trials across "
                + conditions.size() + " condition(s).");
    }

    /** Single-condition generation. Preserved so NAFCPairGenerator's CLI keeps working. */
    public void generate(int numTrials, int numChoices,
                         double width, double height, double eyeWinSize,
                         double choiceRadiusLowerLim, double choiceRadiusUpperLim,
                         double alphaLowerLim, double alphaUpperLim,
                         double distractorDistanceLowerLim, double distractorDistanceUpperLim,
                         double distractorScaleLowerLim, double distractorScaleUpperLim,
                         int distractorPresentationDelay) {

        NAFCPairParams p = new NAFCPairParams();
        p.setNumTrials(numTrials);
        p.setNumChoices(numChoices);
        p.setWidth(width);
        p.setHeight(height);
        p.setEyeWinSize(eyeWinSize);
        p.setChoiceRadiusLowerLim(choiceRadiusLowerLim);
        p.setChoiceRadiusUpperLim(choiceRadiusUpperLim);
        p.setAlphaLowerLim(alphaLowerLim);
        p.setAlphaUpperLim(alphaUpperLim);
        p.setDistractorDistanceLowerLim(distractorDistanceLowerLim);
        p.setDistractorDistanceUpperLim(distractorDistanceUpperLim);
        p.setDistractorScaleLowerLim(distractorScaleLowerLim);
        p.setDistractorScaleUpperLim(distractorScaleUpperLim);
        p.setDistractorPresentationDelay(distractorPresentationDelay);

        generate(Collections.singletonList(p));
    }

    /** Writes one trial under the given condition. */
    private void writeTrial(NAFCPairParams p, int trialIndex,
                            List<String[]> pairs, List<String> stimList) {

        RewardPolicy rewardPolicy = RewardPolicy.LIST;
        long[] eStimObjData = {1};

        int numChoices = p.getNumChoices();
        ImageDimensions sampleDimensions = new ImageDimensions(p.getWidth(), p.getHeight());

        //SAMPLE + MATCH
        long sampleId = globalTimeUtil.currentTimeMicros();
        long taskId   = sampleId;
        String[] pair     = pairs.get(trialIndex % pairs.size());
        String samplePath = experimentStimLibPath + "/" + pair[0];
        String matchPath  = experimentStimLibPath + "/" + pair[1];
        PngSpec sampleSpec = new PngSpec(0, 0, sampleDimensions, samplePath);
        dbUtil.writeStimObjData(sampleId, sampleSpec.toXml(), "sample");

        //CHOICE
        int correctChoice = r.nextInt(numChoices);
        int[] rewardList  = {correctChoice};

        //Handling shuffling & removing Match from possible distractors
        List<String> distractorList = new ArrayList<String>();
        for (String stimPath : stimList) {
            distractorList.add(experimentStimLibPath + "/" + stimPath);
        }
        distractorList.remove(samplePath);
        distractorList.remove(matchPath);
        Collections.shuffle(distractorList);

        //EyewinCoords of target and target location identical
        DistancedDistractorsUtil ddUtil = new DistancedDistractorsUtil(
                numChoices,
                p.getChoiceRadiusLowerLim(), p.getChoiceRadiusUpperLim(),
                p.getDistractorDistanceLowerLim(), p.getDistractorDistanceUpperLim());
        ArrayList<Coordinates2D> distractorsEyeWinCoords = new ArrayList<Coordinates2D>();

        //Step through all of the choices. If the index corresponds to the randomly decided correct choice index, write path of match.
        //else, write the path of one of the distractors. The paths of the distractor is found through stepping through shuffled list of distractors
        int distractorIndex = 0;
        long[] choiceId = new long[numChoices];

        ArrayList<Coordinates2D> targetEyeWinCoords = new ArrayList<>();
        for (int j = 0; j < numChoices; j++) {
            choiceId[j] = sampleId + j + 1;

            if (j == correctChoice) {
                //Size
                ImageDimensions matchDimensions = new ImageDimensions(p.getWidth(), p.getHeight());

                //Distance
                Coordinates2D matchEyeWinCoords = ddUtil.getMatchCoords();

                PngSpec choiceSpec = new PngSpec(matchEyeWinCoords.getX(), matchEyeWinCoords.getY(),
                        matchDimensions, matchPath);
                dbUtil.writeStimObjData(choiceId[j], choiceSpec.toXml(), "choice " + j + "; match");

                targetEyeWinCoords.add(matchEyeWinCoords); //to be converted to array later to pass as targetEyeWindow
            }
            else {
                //Alpha
                double randomAlpha = inclusiveRandomDouble(p.getAlphaLowerLim(), p.getAlphaUpperLim());

                //Size
                double randomScale = inclusiveRandomDouble(p.getDistractorScaleLowerLim(),
                        p.getDistractorScaleUpperLim());
                ImageDimensions distractorDimensions =
                        new ImageDimensions(p.getWidth() * randomScale, p.getHeight() * randomScale);

                //Distance
                Coordinates2D distractorEyeWinCoords = ddUtil.popDistractorCoord();
                distractorsEyeWinCoords.add(distractorEyeWinCoords);

                PngSpec choiceSpec = new PngSpec(
                        distractorEyeWinCoords.getX(), distractorEyeWinCoords.getY(),
                        distractorDimensions, distractorList.get(distractorIndex), randomAlpha);
                dbUtil.writeStimObjData(choiceId[j], choiceSpec.toXml(), "choice " + j + "; distractor");
                distractorIndex += 1;

                targetEyeWinCoords.add(distractorEyeWinCoords);
            }
        }

        //stimSpec just needs Ids, not the path of the pngs themselves. Pngs are stored in StimObjData
        Coordinates2D[] targetEyeWinCoordsArray = targetEyeWinCoords.toArray(new Coordinates2D[0]);

        ArrayList<Double> targetEyeWinSize = new ArrayList<Double>();
        for(Coordinates2D choice:targetEyeWinCoordsArray){
            targetEyeWinSize.add(p.getEyeWinSize());
        }
        double[] targetEyeWinSizeArray = new double[targetEyeWinSize.size()];
        for(int j=0; j < targetEyeWinSize.size(); j++) {
            targetEyeWinSizeArray[j] = targetEyeWinSize.get(j);
        }

        NAFCStimSpecSpec stimSpec = new NAFCStimSpecSpec(
                targetEyeWinCoordsArray, targetEyeWinSizeArray, sampleId, choiceId,
                eStimObjData, rewardPolicy, rewardList, p.getDistractorPresentationDelay());

        // Third argument records this trial's condition, so trials can be binned
        // by condition in analysis without inferring it from coordinates.
        dbUtil.writeStimSpec(taskId, stimSpec.toXml(), p.toXml());
        dbUtil.writeTaskToDo(taskId, taskId, -1, genId);
    }

    @Override
    protected void addTrials() {

    }

    public AllenDbUtil getDbUtil() {
        return dbUtil;
    }

    public void setDbUtil(AllenDbUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public TimeUtil getGlobalTimeUtil() {
        return globalTimeUtil;
    }

    public void setGlobalTimeUtil(TimeUtil globalTimeUtil) {
        this.globalTimeUtil = globalTimeUtil;
    }

    public String getExperimentStimLibPath() {
        return experimentStimLibPath;
    }

    public void setExperimentStimLibPath(String experimentStimLibPath) {
        this.experimentStimLibPath = experimentStimLibPath;
    }
}
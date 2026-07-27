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

import java.util.*;

import static org.xper.allen.nafc.blockgen.NAFCCoordinateAssigner.inclusiveRandomDouble;

public class NAFCPairBlockGen extends AbstractTrialGenerator {
    @Dependency
    AllenDbUtil dbUtil;
    @Dependency
    TimeUtil globalTimeUtil;

    Random r = new Random();
    long genId = 1;

    public void generate(int numTrials, int numChoices,
                         double width, double height, double eyeWinSize,
                         double choiceRadiusLowerLim, double choiceRadiusUpperLim,
                         double alphaLowerLim, double alphaUpperLim,
                         double distractorDistanceLowerLim,  double distractorDistanceUpperLim,
                         double distractorScaleLowerLim, double distractorScaleUpperLim,
                         double distractorPresentationDelay
                         ) {

        List<String[]> pairList = dbUtil.readAssociatePairs("nafc");
        List<String[]> pairs = new LinkedList<>(pairList);
        Collections.shuffle(pairs);
        //FIXED-PARAMETERS
        //int numTrials = 100;
        //SAMPLE
        ImageDimensions sampleDimensions = new ImageDimensions(width,height);
        //CHOICES
        RewardPolicy rewardPolicy = RewardPolicy.LIST;
        long[] eStimObjData = {1};

        //GENERATION
        try {
            /**
             * Gen ID is important for xper to be able to load new tasks on the fly. It will only do so if the generation Id is upticked.
             */
            genId = dbUtil.readReadyGenerationInfo().getGenId() + 1;
        } catch (VariableNotFoundException e) {
            dbUtil.writeReadyGenerationInfo(genId, 0);
        }

        for (int i = 0; i < numTrials; i++) {
            //SAMPLE + MATCH
            long sampleId = globalTimeUtil.currentTimeMicros();
            long taskId = sampleId;
            String[] pair     = pairs.get(i % pairs.size());
            String samplePath = pair[0];
            String matchPath  = pair[1];
            PngSpec sampleSpec = new PngSpec(0, 0, sampleDimensions, samplePath);
            dbUtil.writeStimObjData(sampleId, sampleSpec.toXml(), "sample");

            //CHOICE
            int correctChoice = r.nextInt(numChoices);
            int[] rewardList  = {correctChoice};

            //Handling shuffling & removing Match from possible distractors
            List<String> distractorList = new ArrayList<>();
            for (String[] entry: pairList) {
                distractorList.add(entry[0]);
            }
            distractorList.remove(samplePath);
            distractorList.remove(matchPath);
            Collections.shuffle(distractorList);

            //EyewinCoords of target and target location identical
            DistancedDistractorsUtil ddUtil = new DistancedDistractorsUtil(numChoices, choiceRadiusLowerLim, choiceRadiusUpperLim, distractorDistanceLowerLim,  distractorDistanceUpperLim);
            Coordinates2D matchEyeWinCoords = new Coordinates2D();
            ArrayList<Coordinates2D> distractorsEyeWinCoords = new ArrayList<Coordinates2D>();

            //Coordinates2D[] targetEyeWinCoords = new Coordinates2D[]{};
            //targetEyeWinCoords = distancedDistractorsEquidistantRandomChoices(choiceRadiusLowerLim,choiceRadiusUpperLim,numChoices, distractorList);

            //Step through all of the choices. If the index corresponds to the randomly decided correct choice index, write path of match.
            //else, write the path of one of the distractors. The paths of the distractor is found through stepping through shuffled list of distractors
            int distractorIndex = 0;
            long[] choiceId   = new long[numChoices];

            ArrayList<Coordinates2D> targetEyeWinCoords = new ArrayList<Coordinates2D>();
            for (int j = 0; j < numChoices; j++) {
                choiceId[j] = sampleId + j + 1;

                if (j==correctChoice){
                    //Size
                    ImageDimensions matchDimensions = new ImageDimensions(width, height);

                    //Distance
                    matchEyeWinCoords = ddUtil.getMatchCoords();

                    PngSpec choiceSpec = new PngSpec(matchEyeWinCoords.getX(), matchEyeWinCoords.getY(), matchDimensions, matchPath);
                    dbUtil.writeStimObjData(choiceId[j], choiceSpec.toXml(), "choice " + j + "; " + "match");

                    targetEyeWinCoords.add(matchEyeWinCoords); //to be converted to array later to pass as targetEyeWindow
                }
                else{
                    //Alpha
                    double randomAlpha = inclusiveRandomDouble(alphaLowerLim, alphaUpperLim);

                    //Size
                    double randomScale = inclusiveRandomDouble(distractorScaleLowerLim, distractorScaleUpperLim);
                    ImageDimensions distractorDimensions = new ImageDimensions();
                    distractorDimensions = new ImageDimensions(width*randomScale, height*randomScale);

                    //Distance
                    Coordinates2D distractorEyeWinCoords = ddUtil.popDistractorCoord();
                    distractorsEyeWinCoords.add(distractorEyeWinCoords);

                    PngSpec choiceSpec = new PngSpec(distractorEyeWinCoords.getX(), distractorEyeWinCoords.getY(),distractorDimensions, distractorList.get(distractorIndex), randomAlpha);
                    dbUtil.writeStimObjData(choiceId[j], choiceSpec.toXml(), "choice " + j + "; " + "distractor");
                    distractorIndex += 1;

                    targetEyeWinCoords.add(distractorEyeWinCoords);
                }
            }

            //stimSpec just needs Ids, not the path of the pngs themselves. Pngs are stored in StimObjData
            Coordinates2D[] targetEyeWinCoordsArray = targetEyeWinCoords.toArray(new Coordinates2D[0]);

            ArrayList<Double> targetEyeWinSize = new ArrayList<Double>();
            for(Coordinates2D choice:targetEyeWinCoordsArray){
                targetEyeWinSize.add(eyeWinSize);
            }
            double[] targetEyeWinSizeArray = new double[targetEyeWinSize.size()];
            for(int j=0; j < targetEyeWinSize.size(); j++) {
                targetEyeWinSizeArray[j] = targetEyeWinSize.get(j);
            }

            NAFCStimSpecSpec stimSpec = new NAFCStimSpecSpec(targetEyeWinCoordsArray, targetEyeWinSizeArray, sampleId, choiceId,
                    eStimObjData, rewardPolicy, rewardList, distractorPresentationDelay);
            String spec = stimSpec.toXml();

            dbUtil.writeStimSpec(taskId, spec);
            dbUtil.writeTaskToDo(taskId, taskId, -1, genId);
        }
        dbUtil.updateReadyGenerationInfo(genId, numTrials);
        System.out.println("Done generating " + numTrials + " NAFC trials.");
        return;
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
}
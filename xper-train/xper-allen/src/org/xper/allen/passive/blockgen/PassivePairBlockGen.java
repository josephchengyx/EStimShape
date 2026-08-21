package org.xper.allen.passive.blockgen;

import org.xper.Dependency;
import org.xper.allen.nafc.blockgen.AbstractTrialGenerator;
import org.xper.allen.specs.PassiveStimSpecSpec;
import org.xper.allen.util.AllenDbUtil;
import org.xper.exception.VariableNotFoundException;
import org.xper.rfplot.drawing.png.ImageDimensions;
import org.xper.rfplot.drawing.png.PngSpec;
import org.xper.time.TimeUtil;

import java.util.*;

public class PassivePairBlockGen extends AbstractTrialGenerator {
    @Dependency
    AllenDbUtil dbUtil;
    @Dependency
    TimeUtil globalTimeUtil;
    @Dependency
    String experimentStimLibPath;

    static final String TASK_TYPE = "passive";
    long genId = 1;

    public void generate(int numTrials, double width, double height) {
        List<String[]> pairs = dbUtil.readAssociatePairs(TASK_TYPE);
        Collections.shuffle(pairs);
        //FIXED-PARAMETERS
        //int numTrials = 100;
        ImageDimensions imageDimensions = new ImageDimensions(width,height);

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
            String[] pair     = pairs.get(i % pairs.size());
            String samplePath = experimentStimLibPath + "/" + pair[0];
            String matchPath  = experimentStimLibPath + "/" + pair[1];

            //SAMPLE
            long sampleId = globalTimeUtil.currentTimeMicros();
            PngSpec sampleSpec = new PngSpec(0, 0, imageDimensions, samplePath);
            dbUtil.writeStimObjData(sampleId, sampleSpec.toXml(), "sample");

            //MATCH
            long matchId = sampleId + 1;
            PngSpec matchSpec = new PngSpec(0, 0, imageDimensions, matchPath);
            dbUtil.writeStimObjData(matchId, matchSpec.toXml(), "match");

            long taskId = sampleId;

            //stimSpec just needs Ids, not the path of the pngs themselves. Pngs are stored in StimObjData
            PassiveStimSpecSpec stimSpec = new PassiveStimSpecSpec(sampleId, matchId);
            String spec = stimSpec.toXml();

            dbUtil.writeStimSpec(taskId, spec);
            dbUtil.writeTaskToDo(taskId, taskId, -1, genId);
        }
        dbUtil.updateReadyGenerationInfo(genId, numTrials);
        System.out.println("Done generating " + numTrials + " passive viewing trials.");
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

    public String getExperimentStimLibPath() {
        return experimentStimLibPath;
    }

    public void setExperimentStimLibPath(String experimentStimLibPath) {
        this.experimentStimLibPath = experimentStimLibPath;
    }
}

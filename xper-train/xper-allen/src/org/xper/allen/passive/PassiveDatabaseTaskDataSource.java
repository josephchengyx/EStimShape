package org.xper.allen.passive;

import org.apache.log4j.Logger;
import org.xper.Dependency;
import org.xper.allen.util.AllenDbUtil;
import org.xper.db.vo.GenerationInfo;
import org.xper.experiment.DatabaseTaskDataSource;
import org.xper.experiment.ExperimentTask;
import org.xper.util.DbUtil;
import org.xper.util.ThreadHelper;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class PassiveDatabaseTaskDataSource extends DatabaseTaskDataSource {

    static Logger logger = Logger.getLogger(PassiveDatabaseTaskDataSource.class);

    static final int DEFAULT_QUERY_INTERVAL = 1000;

    @Dependency
    AllenDbUtil dbUtil;
    @Dependency
    long queryInterval = DEFAULT_QUERY_INTERVAL;
    @Dependency
    UngetPolicy ungetBehavior;
    @Dependency
    int ungetTaskThreshold;

    AtomicReference<LinkedList<PassiveExperimentTask>> currentGeneration = new AtomicReference<>();
    ThreadHelper threadHelper = new ThreadHelper("PassiveDatabaseTaskDataSource", this);
    long currentGenId = -1;
    long lastDoneTaskId = -1;

    public boolean isRunning() {
        return threadHelper.isRunning();
    }

    @Override
    public PassiveExperimentTask getNextTask() {
        try {
            LinkedList<PassiveExperimentTask> tasks = currentGeneration.get();
            if (tasks == null) return null;
            PassiveExperimentTask task = tasks.removeFirst();
            if (logger.isDebugEnabled()) {
                logger.debug("Get -- Generation: " + task.getGenId() + " task: " + task.getTaskId());
            }
            return task;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void ungetTask(ExperimentTask t) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unget -- Generation: " + t.getGenId() + " task: " + t.getTaskId());
        }
        if (t == null) return;

        LinkedList<PassiveExperimentTask> tasks = currentGeneration.get();
        if (tasks == null) return;

        PassiveExperimentTask cur;
        try {
            cur = tasks.getFirst();
        } catch (NoSuchElementException e) {
            cur = null;
        }

        if (cur == null || cur.getGenId() == t.getGenId()) {
            if (tasks.size() >= ungetTaskThreshold) {
                if (ungetBehavior == UngetPolicy.HEAD) {
                    tasks.addFirst((PassiveExperimentTask) t);
                } else if (ungetBehavior == UngetPolicy.TAIL) {
                    tasks.addLast((PassiveExperimentTask) t);
                } else {
                    int numTasks = tasks.size();
                    Random r = new Random();
                    int randIndex = numTasks > 0 ? r.nextInt(numTasks) : 0;
                    tasks.add(randIndex, (PassiveExperimentTask) t);
                }
            } else {
                logger.debug("Did not unget task — queue size below threshold");
            }
        }
    }

    @Override
    public void run() {
        try {
            threadHelper.started();
            while (!threadHelper.isDone()) {
                if (lastDoneTaskId < 0) {
                    lastDoneTaskId = dbUtil.readTaskDoneCompleteMaxId();
                }
                GenerationInfo info = dbUtil.readReadyGenerationInfo();
                if (info.getGenId() > currentGenId) {
                    LinkedList<PassiveExperimentTask> taskToDo =
                            dbUtil.readPassiveExperimentTasks(info.getGenId(), lastDoneTaskId);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Generation " + info.getGenId() + " size: " + taskToDo.size());
                    }
                    if (taskToDo.size() > 0) {
                        LinkedList<PassiveExperimentTask> unfinished = currentGeneration.get();
                        if (unfinished == null) unfinished = new LinkedList<>();
                        unfinished.addAll(taskToDo);
                        currentGeneration.set(unfinished);
                        currentGenId = info.getGenId();
                    }
                }
                try {
                    Thread.sleep(queryInterval);
                } catch (InterruptedException ignored) {}
            }
        } finally {
            try {
                threadHelper.stopped();
            } catch (Exception e) {
                logger.warn(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void start() { threadHelper.start(); }

    public void stop() {
        if (isRunning()) {
            threadHelper.stop();
            threadHelper.join();
        }
    }

    public DbUtil getDbUtil() { return dbUtil; }
    public void setDbUtil(AllenDbUtil dbUtil) { this.dbUtil = dbUtil; }
    public long getQueryInterval() { return queryInterval; }
    public void setQueryInterval(long queryInterval) { this.queryInterval = queryInterval; }
    public UngetPolicy getUngetBehavior() { return ungetBehavior; }
    public void setUngetBehavior(UngetPolicy ungetBehavior) { this.ungetBehavior = ungetBehavior; }
    public int getUngetTaskThreshold() { return ungetTaskThreshold; }
    public void setUngetTaskThreshold(int ungetTaskThreshold) { this.ungetTaskThreshold = ungetTaskThreshold; }
}
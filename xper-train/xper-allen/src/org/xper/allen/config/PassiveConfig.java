package org.xper.allen.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.*;
import org.springframework.config.java.annotation.valuesource.SystemPropertiesValueSource;
import org.springframework.config.java.plugin.context.AnnotationDrivenConfig;
import org.springframework.config.java.util.DefaultScopes;
import org.xper.acq.mock.SocketSamplingDeviceServer;
import org.xper.allen.app.fixation.PngScene;
import org.xper.allen.drawing.LeftRightScreenMarker;
import org.xper.allen.nafc.experiment.NAFCMarkStimAndEStimTrialDrawingController;
import org.xper.allen.nafc.experiment.NAFCTrialDrawingController;
import org.xper.allen.nafc.experiment.ScreenShotter;
import org.xper.allen.nafc.experiment.juice.NAFCJuiceController;
import org.xper.allen.nafc.message.ChoiceEventListener;
import org.xper.allen.passive.*;
import org.xper.allen.passive.mock.PassiveMockDatabaseTaskDataSource;
import org.xper.allen.util.AllenDbUtil;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.config.*;
import org.xper.console.ExperimentConsole;
import org.xper.console.ExperimentConsoleModel;
import org.xper.console.ExperimentMessageReceiver;
import org.xper.drawing.object.BlankScreen;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.drawing.renderer.PerspectiveRenderer;
import org.xper.exception.DbException;
import org.xper.experiment.DatabaseTaskDataSource.UngetPolicy;
import org.xper.experiment.TaskDataSource;
import org.xper.experiment.listener.ExperimentEventListener;
import org.xper.classic.TrialEventListener;
import org.xper.eye.mapping.MappingAlgorithm;
import org.xper.intan.SlideTrialIntanRecordingController;
import org.xper.juice.mock.NullDynamicJuice;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Configuration(defaultLazy = Lazy.TRUE)
@SystemPropertiesValueSource
@AnnotationDrivenConfig
@Import({ClassicConfig.class, RewardButtonConfig.class, IntanRHSConfig.class})
public class PassiveConfig {

    @Autowired ClassicConfig classicConfig;
    @Autowired BaseConfig baseConfig;
    @Autowired AcqConfig acqConfig;
    @Autowired RewardButtonConfig rewardButtonConfig;
    @Autowired IntanRHSConfig intanConfig;
    @Autowired IntanRHDConfig rhdConfig;

    @ExternalValue("jdbc.driver")
    public String jdbcDriver;

    @ExternalValue("jdbc.url")
    public String jdbcUrl;

    @ExternalValue("jdbc.username")
    public String jdbcUserName;

    @ExternalValue("jdbc.password")
    public String jdbcPassword;

    @ExternalValue("experiment.monkey_window_fullscreen")
    public boolean monkeyWindowFullScreen;

    @ExternalValue("experiment.mark_every_step")
    public boolean markEveryStep;

    @ExternalValue("screenshot.enabled")
    public boolean screenShotEnabled;

    @ExternalValue("screenshot.directory")
    public String screenShotDirectory;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Bean
    public AbstractRenderer experimentGLRenderer() {
        PerspectiveRenderer renderer = new PerspectiveRenderer();
        renderer.setDistance(classicConfig.xperMonkeyScreenDistance());
        renderer.setDepth(classicConfig.xperMonkeyScreenDepth());
        renderer.setHeight(classicConfig.xperMonkeyScreenHeight());
        renderer.setWidth(classicConfig.xperMonkeyScreenWidth());
        renderer.setPupilDistance(classicConfig.xperMonkeyPupilDistance());
        return renderer;
    }

    @Bean
    public AllenDbUtil allenDbUtil() {
        AllenDbUtil dbUtil = new AllenDbUtil();
        dbUtil.setDataSource(dataSource());
        return dbUtil;
    }

    @Bean
    public ExperimentConsole experimentConsole() {
        ExperimentConsole console = new ExperimentConsole();
        console.setPaused(classicConfig.xperExperimentInitialPause());
        console.setConsoleRenderer(classicConfig.consoleRenderer());
        console.setMonkeyScreenDimension(classicConfig.monkeyWindow().getScreenDimension());
        console.setModel(classicConfig.experimentConsoleModel());
        console.setCanvasScaleFactor(3);

        ExperimentMessageReceiver receiver = classicConfig.messageReceiver();
        receiver.addMessageReceiverEventListener(console);

        return console;
    }

    @Bean
    public ExperimentConsoleModel experimentConsoleModel () {
        ExperimentConsoleModel model = new ExperimentConsoleModel();
        model.setMessageReceiver(classicConfig.messageReceiver());
        model.setLocalTimeUtil(baseConfig.localTimeUtil());

        HashMap<String, MappingAlgorithm> eyeMappingAlgorithm = new HashMap<String, MappingAlgorithm>();
        eyeMappingAlgorithm.put(classicConfig.xperLeftIscanId(), classicConfig.leftIscanMappingAlgorithm());
        eyeMappingAlgorithm.put(classicConfig.xperRightIscanId(), classicConfig.rightIscanMappingAlgorithm());
        model.setEyeMappingAlgorithm(eyeMappingAlgorithm);

        model.setExperimentRunnerClient(rewardButtonConfig.experimentRunnerClient());
        model.setChannelMap(classicConfig.iscanChannelMap());
        model.setMessageHandler(classicConfig.messageHandler());

        if (classicConfig.consoleEyeSimulation || acqConfig.acqDriverName.equalsIgnoreCase(acqConfig.DAQ_NONE)) {
            // socket sampling server for eye simulation
            SocketSamplingDeviceServer server = new SocketSamplingDeviceServer();
            server.setHost(classicConfig.consoleHost);
            server.setSamplingDevice(model);
            HashMap<Integer, Double> data = new HashMap<Integer, Double>();
            data.put(classicConfig.xperLeftIscanXChannel(), new Double(0));
            data.put(classicConfig.xperLeftIscanYChannel(), new Double(0));
            data.put(classicConfig.xperRightIscanXChannel(), new Double(0));
            data.put(classicConfig.xperRightIscanYChannel(), new Double(0));
            server.setCurrentChannelData(data);

            model.setSamplingServer(server);
        }


        return model;
    }

    @Bean
    public DataSource dataSource() {
        ComboPooledDataSource source = new ComboPooledDataSource();
        try {
            source.setDriverClass(jdbcDriver);
        } catch (PropertyVetoException e) {
            throw new DbException(e);
        }
        source.setJdbcUrl(jdbcUrl);
        source.setUser(jdbcUserName);
        source.setPassword(jdbcPassword);
        return source;
    }

    @Bean
    public PassiveTaskScene taskScene() {
        PassivePngScene scene = new PassivePngScene();
        scene.setRenderer(experimentGLRenderer());
        scene.setFixation(classicConfig.experimentFixationPoint());
        scene.setMarker(classicConfig.screenMarker());
        scene.setBlankScreen(new BlankScreen());
        scene.setScreenHeight(classicConfig.xperMonkeyScreenHeight());
        scene.setScreenWidth(classicConfig.xperMonkeyScreenWidth());
        scene.setDistance(classicConfig.xperMonkeyScreenDistance());
        scene.setBackgroundColor(classicConfig.xperBackgroundColor());
        return scene;
    }

    @Bean
    public SlideTrialIntanRecordingController intanRecordingController() {
        SlideTrialIntanRecordingController controller = new SlideTrialIntanRecordingController();
        controller.setIntan(intanConfig.intan());
        controller.setRecordingEnabled(intanConfig.intanRecordingEnabled());
        controller.setFileNamingStrategy(rhdConfig.intanFileNamingStrategy());
        return controller;
    }

    @Bean
    public PassiveSlideRunner slideRunner() {
        PassiveSlideRunner runner = new PassiveSlideRunner();
        runner.setPunisher(classicConfig.punisher());
        return runner;
    }

    @Bean
    public TaskDataSource taskDataSource() {
        return databaseTaskDataSource();
    }

//    @Bean
//    public PassiveDatabaseTaskDataSource databaseTaskDataSource() {
//        PassiveDatabaseTaskDataSource source = new PassiveDatabaseTaskDataSource();
//        source.setDbUtil(allenDbUtil());
//        source.setQueryInterval(1000);
//        source.setUngetBehavior(xperUngetPolicy());
//        source.setUngetTaskThreshold(xperUngetTaskThreshold());
//        return source;
//    }

    public PassiveDatabaseTaskDataSource databaseTaskDataSource() {
        return new PassiveMockDatabaseTaskDataSource();
    }

    @Bean
    public int xperUngetTaskThreshold() {
        return 5;
    }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public UngetPolicy xperUngetPolicy() {
        return UngetPolicy.valueOf(baseConfig.systemVariableContainer().get("xper_unget_policy", 0));
    }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public List<ExperimentEventListener> experimentEventListeners() {
        List<ExperimentEventListener> listeners = new LinkedList<>();
        listeners.add(classicConfig.messageDispatcher());
        listeners.add(classicConfig.databaseTaskDataSourceController());
        listeners.add(classicConfig.messageDispatcherController());
        listeners.add(classicConfig.dataAcqController());
        listeners.add(classicConfig.eyeZeroLogger());
        listeners.add(classicConfig.experimentCpuBinder());
        listeners.add(classicConfig.systemVarLogger());
        listeners.add(intanRecordingController());
        return listeners;
    }

    @Bean
    public SlideTrialExperimentState experimentState () {
        SlideTrialExperimentState state = new SlideTrialExperimentState ();
        state.setLocalTimeUtil(baseConfig.localTimeUtil());
        state.setTrialEventListeners(trialEventListeners());
        state.setSlideEventListeners(classicConfig.slideEventListeners());
        state.setEyeController(classicConfig.eyeController());
        state.setExperimentEventListeners(experimentEventListeners());
        state.setTaskDataSource(taskDataSource());
        state.setTaskDoneCache(classicConfig.taskDoneCache());
        state.setGlobalTimeClient(acqConfig.timeClient());
        state.setDrawingController(drawingController());
        state.setInterTrialInterval(classicConfig.xperInterTrialInterval());
        state.setTimeBeforeFixationPointOn(classicConfig.xperTimeBeforeFixationPointOn());
        state.setTimeAllowedForInitialEyeIn(classicConfig.xperTimeAllowedForInitialEyeIn());
        state.setRequiredEyeInHoldTime(classicConfig.xperRequiredEyeInHoldTime());
        state.setSlidePerTrial(classicConfig.xperSlidePerTrial());
        state.setSlideLength(xperSlideLength());
        state.setInterSlideInterval(xperInterSlideInterval());
        state.setDoEmptyTask(classicConfig.xperDoEmptyTask());
        state.setSleepWhileWait(true);
        state.setPause(classicConfig.xperExperimentInitialPause());
        state.setDelayAfterTrialComplete(classicConfig.xperDelayAfterTrialComplete());
        return state;
    }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public List<TrialEventListener> trialEventListeners() {
        List<TrialEventListener> listeners = new LinkedList<>();
        listeners.add(classicConfig.eyeMonitorController());
        listeners.add(classicConfig.trialEventLogger());
        listeners.add(classicConfig.experimentProfiler());
        listeners.add(classicConfig.messageDispatcher());
        listeners.add(classicConfig.trialSyncController());
        listeners.add(classicConfig.dataAcqController());
        listeners.add(classicConfig.jvmManager());
        listeners.add(intanRecordingController());
        if (!acqConfig.acqDriverName.equalsIgnoreCase(acqConfig.DAQ_NONE)) {
            listeners.add(classicConfig.dynamicJuiceUpdater());
        }
        return listeners;
    }

    @Bean
    public PassiveTrialDrawingController drawingController() {
        PassiveMarkStimTrialDrawingController controller;
        controller = new PassiveMarkStimTrialDrawingController();

        controller.setWindow(classicConfig.monkeyWindow());
        controller.setTaskScene(taskScene());
        controller.setFixationOnWithStimuli(classicConfig.xperFixationOnWithStimuli());
        controller.setScreenShotter(screenShotter());
        controller.setLeftRightMarker(screenMarker());
        return controller;
    }

    @Bean
    public LeftRightScreenMarker screenMarker() {
        LeftRightScreenMarker leftRightScreenMarker = new LeftRightScreenMarker();
        leftRightScreenMarker.setSize(classicConfig.xperScreenMarkerSize());
        leftRightScreenMarker.setViewportIndex(classicConfig.xperScreenMarkerViewportIndex());
        return leftRightScreenMarker;
    }

    @Bean
    public ScreenShotter screenShotter(){
        ScreenShotter screenShotter = new ScreenShotter();
        screenShotter.setEnabled(screenShotEnabled);
        screenShotter.setDirectory(screenShotDirectory);
        screenShotter.setScreenWidthPixels(3840);
        screenShotter.setScreenHeightPixels(2160);
        return screenShotter;

    }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public Integer xperSlideLength() {
        return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_slide_length", 0));
    }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public Integer xperInterSlideInterval() {
        return Integer.parseInt(baseConfig.systemVariableContainer().get("xper_inter_slide_interval", 0));
    }
}
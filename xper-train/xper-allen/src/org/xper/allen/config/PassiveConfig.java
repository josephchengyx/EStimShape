package org.xper.allen.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.*;
import org.springframework.config.java.annotation.valuesource.SystemPropertiesValueSource;
import org.springframework.config.java.plugin.context.AnnotationDrivenConfig;
import org.springframework.config.java.util.DefaultScopes;
import org.xper.allen.app.fixation.PngScene;
import org.xper.allen.passive.PassiveSlideRunner;
import org.xper.allen.passive.mock.PassiveMockDatabaseTaskDataSource;
import org.xper.config.AcqConfig;
import org.xper.config.BaseConfig;
import org.xper.config.ClassicConfig;
import org.xper.drawing.object.BlankScreen;
import org.xper.drawing.renderer.AbstractRenderer;
import org.xper.drawing.renderer.PerspectiveRenderer;
import org.xper.experiment.TaskDataSource;
import org.xper.experiment.TaskDoneCache;

@Configuration(defaultLazy = Lazy.TRUE)
@SystemPropertiesValueSource
@AnnotationDrivenConfig
@Import({ClassicConfig.class})
public class PassiveConfig {

    @Autowired ClassicConfig classicConfig;
    @Autowired BaseConfig baseConfig;
    @Autowired AcqConfig acqConfig;

    // ---- Slide runner ----

    @Bean
    public PassiveSlideRunner slideRunner() {
        PassiveSlideRunner runner = new PassiveSlideRunner();
        runner.setPunisher(classicConfig.punisher());
        return runner;
    }

    // ---- Scene ----

    @Bean
    public PngScene taskScene() {
        PngScene scene = new PngScene();
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
    public AbstractRenderer experimentGLRenderer() {
        PerspectiveRenderer r = new PerspectiveRenderer();
        r.setDistance(classicConfig.xperMonkeyScreenDistance());
        r.setDepth(classicConfig.xperMonkeyScreenDepth());
        r.setHeight(classicConfig.xperMonkeyScreenHeight());
        r.setWidth(classicConfig.xperMonkeyScreenWidth());
        r.setPupilDistance(classicConfig.xperMonkeyPupilDistance());
        return r;
    }

    // ---- Task source ----

    @Bean
    public TaskDataSource taskDataSource() {
        return new PassiveMockDatabaseTaskDataSource();
    }

    // Prevent DatabaseTaskDataSourceController from starting a DB polling thread
    @Bean
    public TaskDataSource databaseTaskDataSource() {
        return taskDataSource();
    }

    // ---- No-op cache (no DB writes) ----

    @Bean
    public TaskDoneCache taskDoneCache() {
        return new TaskDoneCache() {
            public void put(org.xper.experiment.ExperimentTask t, long ts, boolean partial) {}
            public void flush() {}
        };
    }

    // ---- Hardcoded slide params ----

//    @Bean(scope = DefaultScopes.PROTOTYPE)
//    public Integer xperSlidePerTrial() { return 2; }

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public Integer xperSlideLength() { return 1000; }        // ms

    @Bean(scope = DefaultScopes.PROTOTYPE)
    public Integer xperInterSlideInterval() { return 1000; } // ms delay
}

package org.xper.allen.app.nafc;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.allen.nafc.blockgen.NAFCPairBlockGen;
import org.xper.allen.nafc.blockgen.NAFCPairParams;
import org.xper.allen.nafc.blockgen.PairTrialParamDbUtil;
import org.xper.exception.XGLException;
import org.xper.time.TimeUtil;
import org.xper.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Form launcher for {@link NAFCPairBlockGen}. Opens showing the parameters used
 * for the most recent generation, or the defaults from {@link NAFCPairParams} if
 * none have been recorded yet, and writes a new record after a successful run.
 */
public class NAFCPairGeneratorGUI {

    private static final String NUM_TRIALS  = "Number of Trials:";
    private static final String NUM_CHOICES = "Number of Choices:";
    private static final String WIDTH       = "Image Width (deg):";
    private static final String HEIGHT      = "Image Height (deg):";
    private static final String EYE_WIN     = "Eye Window Size (deg):";
    private static final String RADIUS_MIN  = "Choice Radius Min (deg):";
    private static final String RADIUS_MAX  = "Choice Radius Max (deg):";
    private static final String ALPHA_MIN   = "Distractor Alpha Min:";
    private static final String ALPHA_MAX   = "Distractor Alpha Max:";
    private static final String DIST_MIN    = "Distractor Distance Min (deg):";
    private static final String DIST_MAX    = "Distractor Distance Max (deg):";
    private static final String SCALE_MIN   = "Distractor Scale Min:";
    private static final String SCALE_MAX   = "Distractor Scale Max:";
    private static final String DELAY       = "Distractor Delay (ms, 0 = simultaneous):";

    /** Declaration order is on-screen row order. */
    private static final String[] LABELS = {
            NUM_TRIALS, NUM_CHOICES, WIDTH, HEIGHT, EYE_WIN,
            RADIUS_MIN, RADIUS_MAX, ALPHA_MIN, ALPHA_MAX,
            DIST_MIN, DIST_MAX, SCALE_MIN, SCALE_MAX, DELAY
    };

    private final Map<String, JTextField> fields = new LinkedHashMap<String, JTextField>();
    private final NAFCPairBlockGen generator;
    private final PairTrialParamDbUtil paramDbUtil;

    public NAFCPairGeneratorGUI(NAFCPairBlockGen generator,
                                PairTrialParamDbUtil paramDbUtil) {
        this.generator = generator;
        this.paramDbUtil = paramDbUtil;

        for (int i = 0; i < LABELS.length; i++) {
            fields.put(LABELS[i], new JTextField(10));
        }
        populateFrom(lastUsedOrDefaults());
    }

    /** Parameters from the most recent generation, or a fresh set of defaults. */
    private NAFCPairParams lastUsedOrDefaults() {
        try {
            String xml = paramDbUtil.readLatestTrialParams();
            if (xml != null && !xml.isEmpty()) {
                return NAFCPairParams.fromXml(xml);
            }
        } catch (Exception e) {
            System.out.println("Could not read last-used parameters, using defaults: "
                    + e.getMessage());
        }
        return new NAFCPairParams();
    }

    private void populateFrom(NAFCPairParams p) {
        set(NUM_TRIALS,  p.getNumTrials());
        set(NUM_CHOICES, p.getNumChoices());
        set(WIDTH,       p.getWidth());
        set(HEIGHT,      p.getHeight());
        set(EYE_WIN,     p.getEyeWinSize());
        set(RADIUS_MIN,  p.getChoiceRadiusLowerLim());
        set(RADIUS_MAX,  p.getChoiceRadiusUpperLim());
        set(ALPHA_MIN,   p.getAlphaLowerLim());
        set(ALPHA_MAX,   p.getAlphaUpperLim());
        set(DIST_MIN,    p.getDistractorDistanceLowerLim());
        set(DIST_MAX,    p.getDistractorDistanceUpperLim());
        set(SCALE_MIN,   p.getDistractorScaleLowerLim());
        set(SCALE_MAX,   p.getDistractorScaleUpperLim());
        set(DELAY,       p.getDistractorPresentationDelay());
    }

    private NAFCPairParams readFromFields() {
        NAFCPairParams p = new NAFCPairParams();
        p.setNumTrials(getInt(NUM_TRIALS));
        p.setNumChoices(getInt(NUM_CHOICES));
        p.setWidth(getDouble(WIDTH));
        p.setHeight(getDouble(HEIGHT));
        p.setEyeWinSize(getDouble(EYE_WIN));
        p.setChoiceRadiusLowerLim(getDouble(RADIUS_MIN));
        p.setChoiceRadiusUpperLim(getDouble(RADIUS_MAX));
        p.setAlphaLowerLim(getDouble(ALPHA_MIN));
        p.setAlphaUpperLim(getDouble(ALPHA_MAX));
        p.setDistractorDistanceLowerLim(getDouble(DIST_MIN));
        p.setDistractorDistanceUpperLim(getDouble(DIST_MAX));
        p.setDistractorScaleLowerLim(getDouble(SCALE_MIN));
        p.setDistractorScaleUpperLim(getDouble(SCALE_MAX));
        p.setDistractorPresentationDelay(getInt(DELAY));
        return p;
    }

    private void set(String label, Object value) {
        fields.get(label).setText(String.valueOf(value));
    }

    private int getInt(String label) {
        return Integer.parseInt(fields.get(label).getText().trim());
    }

    private double getDouble(String label) {
        return Double.parseDouble(fields.get(label).getText().trim());
    }

    public void show() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 4));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
            form.add(new JLabel(entry.getKey()));
            form.add(entry.getValue());
        }

        JButton generateButton = new JButton("Generate Trials");
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateTrials();
            }
        });

        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populateFrom(new NAFCPairParams());
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(resetButton);
        buttons.add(generateButton);

        JFrame frame = new JFrame("NAFC Pair Trial Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.CENTER, form);
        frame.getContentPane().add(BorderLayout.SOUTH, buttons);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void generateTrials() {
        NAFCPairParams p;
        try {
            p = readFromFields();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                    "Could not read a field value: " + nfe.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            generator.generate(
                    p.getNumTrials(), p.getNumChoices(),
                    p.getWidth(), p.getHeight(), p.getEyeWinSize(),
                    p.getChoiceRadiusLowerLim(), p.getChoiceRadiusUpperLim(),
                    p.getAlphaLowerLim(), p.getAlphaUpperLim(),
                    p.getDistractorDistanceLowerLim(), p.getDistractorDistanceUpperLim(),
                    p.getDistractorScaleLowerLim(), p.getDistractorScaleUpperLim(),
                    p.getDistractorPresentationDelay());

            paramDbUtil.writeTrialParams(generator.getGlobalTimeUtil().currentTimeMicros(), p.toXml());
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    "Generation failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new XGLException(e);
        }

        JavaConfigApplicationContext context = new JavaConfigApplicationContext(
                FileUtil.loadConfigClass("experiment.config_class"));

        new NAFCPairGeneratorGUI(
                context.getBean(NAFCPairBlockGen.class),
                context.getBean(PairTrialParamDbUtil.class)).show();
    }
}
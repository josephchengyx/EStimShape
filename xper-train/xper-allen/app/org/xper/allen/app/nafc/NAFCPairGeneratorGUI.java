package org.xper.allen.app.nafc;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.allen.nafc.blockgen.NAFCPairBlockGen;
import org.xper.allen.nafc.blockgen.NAFCPairParams;
import org.xper.allen.nafc.blockgen.PairTrialParamDbUtil;
import org.xper.exception.XGLException;
import org.xper.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Form launcher for {@link NAFCPairBlockGen}.
 *
 * Conditions are queued rather than generated one at a time: the form describes
 * one condition, "Add Condition" appends it to the queue, and "Generate Trials"
 * expands every queued condition, shuffles the trials together, and writes them
 * interleaved. Queueing a single condition reproduces the previous behaviour.
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
    private final List<NAFCPairParams> conditions = new ArrayList<NAFCPairParams>();
    private final DefaultListModel<String> listModel = new DefaultListModel<String>();

    private final NAFCPairBlockGen generator;
    private final PairTrialParamDbUtil paramDbUtil;

    private JList<String> conditionList;
    private JLabel totalLabel;

    public NAFCPairGeneratorGUI(NAFCPairBlockGen generator,
                                    PairTrialParamDbUtil paramDbUtil) {
        this.generator = generator;
        this.paramDbUtil = paramDbUtil;

        for (int i = 0; i < LABELS.length; i++) {
            fields.put(LABELS[i], new JTextField(10));
        }

        List<NAFCPairParams> lastUsed = lastUsedOrDefaults();
        conditions.addAll(lastUsed);
        populateFrom(lastUsed.get(0));
    }

    // ---------- persistence ----------

    /**
     * The condition list from the most recent generation, or a single default
     * condition. Falls back to the old single-condition format so records
     * written before mixed conditions existed still load.
     */
    private List<NAFCPairParams> lastUsedOrDefaults() {
        List<NAFCPairParams> result = new ArrayList<NAFCPairParams>();
        try {
            String xml = paramDbUtil.readLatestTrialParams();
            if (xml != null && !xml.isEmpty()) {
                if (xml.trim().startsWith("<list>")) {
                    result.addAll(NAFCPairParams.listFromXml(xml));
                } else {
                    result.add(NAFCPairParams.fromXml(xml));
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read last used parameters, using defaults: "
                    + e.getMessage());
            result.clear();
        }
        if (result.isEmpty()) {
            result.add(new NAFCPairParams());
        }
        return result;
    }

    // ---------- form <-> params ----------

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

    // ---------- queue ----------

    private String summarize(NAFCPairParams p) {
        return p.getNumTrials() + " trials"
                + "  |  " + p.getNumChoices() + "AFC"
                + "  |  delay " + p.getDistractorPresentationDelay() + " ms"
                + "  |  scale " + p.getDistractorScaleLowerLim() + "-" + p.getDistractorScaleUpperLim()
                + "  |  dist " + p.getDistractorDistanceLowerLim() + "-" + p.getDistractorDistanceUpperLim() + " deg"
                + "  |  alpha " + p.getAlphaLowerLim() + "-" + p.getAlphaUpperLim();
    }

    private void refreshList() {
        listModel.clear();
        int total = 0;
        for (NAFCPairParams p : conditions) {
            listModel.addElement(summarize(p));
            total += p.getNumTrials();
        }
        totalLabel.setText("  " + conditions.size() + " condition(s), "
                + total + " trials total — shuffled together on generate");
    }

    /** Reads the form, showing a dialog and returning null if a field is unparseable. */
    private NAFCPairParams readFromFieldsOrWarn() {
        try {
            return readFromFields();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                    "Could not read a field value: " + nfe.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // ---------- ui ----------

    public void show() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 4));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
            form.add(new JLabel(entry.getKey()));
            form.add(entry.getValue());
        }

        conditionList = new JList<String>(listModel);
        conditionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(conditionList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Queued conditions"));
        listScroll.setPreferredSize(new Dimension(520, 220));

        // Selecting a condition loads it back into the form for editing.
        conditionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int i = conditionList.getSelectedIndex();
                if (i >= 0 && i < conditions.size()) {
                    populateFrom(conditions.get(i));
                }
            }
        });

        totalLabel = new JLabel();

        JButton addButton = new JButton("Add Condition");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NAFCPairParams p = readFromFieldsOrWarn();
                if (p != null) {
                    conditions.add(p);
                    refreshList();
                    conditionList.setSelectedIndex(conditions.size() - 1);
                }
            }
        });

        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = conditionList.getSelectedIndex();
                if (i < 0) {
                    return;
                }
                NAFCPairParams p = readFromFieldsOrWarn();
                if (p != null) {
                    conditions.set(i, p);
                    refreshList();
                    conditionList.setSelectedIndex(i);
                }
            }
        });

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = conditionList.getSelectedIndex();
                if (i >= 0) {
                    conditions.remove(i);
                    refreshList();
                }
            }
        });

        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                conditions.clear();
                refreshList();
            }
        });

        JButton resetButton = new JButton("Reset Fields");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populateFrom(new NAFCPairParams());
            }
        });

        JButton generateButton = new JButton("Generate Trials");
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateTrials();
            }
        });

        JPanel queueButtons = new JPanel();
        queueButtons.add(addButton);
        queueButtons.add(updateButton);
        queueButtons.add(removeButton);
        queueButtons.add(clearButton);

        JPanel runButtons = new JPanel();
        runButtons.add(resetButton);
        runButtons.add(generateButton);

        JPanel right = new JPanel(new BorderLayout());
        right.add(BorderLayout.CENTER, listScroll);
        right.add(BorderLayout.NORTH, totalLabel);
        right.add(BorderLayout.SOUTH, queueButtons);

        JFrame frame = new JFrame("NAFC Pair Trial Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.WEST, form);
        frame.getContentPane().add(BorderLayout.CENTER, right);
        frame.getContentPane().add(BorderLayout.SOUTH, runButtons);

        refreshList();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void generateTrials() {
        // An empty queue falls back to the form, so single-condition use needs no queueing.
        List<NAFCPairParams> toRun;
        if (conditions.isEmpty()) {
            NAFCPairParams p = readFromFieldsOrWarn();
            if (p == null) {
                return;
            }
            toRun = new ArrayList<NAFCPairParams>();
            toRun.add(p);
        } else {
            toRun = new ArrayList<NAFCPairParams>(conditions);
        }

        try {
            generator.generate(toRun);
            paramDbUtil.writeTrialParams(
                    generator.getGlobalTimeUtil().currentTimeMicros(),
                    NAFCPairParams.listToXml(toRun));
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
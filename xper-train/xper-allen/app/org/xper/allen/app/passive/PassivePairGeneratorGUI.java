package org.xper.allen.app.passive;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.allen.passive.blockgen.PassivePairBlockGen;
import org.xper.allen.passive.blockgen.PassivePairParams;
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
 * Form launcher for {@link PassivePairBlockGen}. Reads its config class from
 * experiment.passive.config_class, so no properties-file swapping is needed
 * when moving between the NAFC and passive tasks.
 */
public class PassivePairGeneratorGUI {

    private static final String NUM_TRIALS = "Number of Trials:";
    private static final String WIDTH      = "Image Width (deg):";
    private static final String HEIGHT     = "Image Height (deg):";

    private static final String[] LABELS = { NUM_TRIALS, WIDTH, HEIGHT };

    private final Map<String, JTextField> fields = new LinkedHashMap<String, JTextField>();
    private final PassivePairBlockGen generator;
    private final PairTrialParamDbUtil paramDbUtil;

    public PassivePairGeneratorGUI(PassivePairBlockGen generator,
                                   PairTrialParamDbUtil paramDbUtil) {
        this.generator = generator;
        this.paramDbUtil = paramDbUtil;

        for (int i = 0; i < LABELS.length; i++) {
            fields.put(LABELS[i], new JTextField(10));
        }
        populateFrom(lastUsedOrDefaults());
    }

    private PassivePairParams lastUsedOrDefaults() {
        try {
            String xml = paramDbUtil.readLatestTrialParams();
            if (xml != null && !xml.isEmpty()) {
                return PassivePairParams.fromXml(xml);
            }
        } catch (Exception e) {
            System.out.println("Could not read last-used parameters, using defaults: "
                    + e.getMessage());
        }
        return new PassivePairParams();
    }

    private void populateFrom(PassivePairParams p) {
        set(NUM_TRIALS, p.getNumTrials());
        set(WIDTH,      p.getWidth());
        set(HEIGHT,     p.getHeight());
    }

    private PassivePairParams readFromFields() {
        PassivePairParams p = new PassivePairParams();
        p.setNumTrials(getInt(NUM_TRIALS));
        p.setWidth(getDouble(WIDTH));
        p.setHeight(getDouble(HEIGHT));
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
                populateFrom(new PassivePairParams());
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(resetButton);
        buttons.add(generateButton);

        JFrame frame = new JFrame("Passive Pair Trial Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.CENTER, form);
        frame.getContentPane().add(BorderLayout.SOUTH, buttons);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void generateTrials() {
        PassivePairParams p;
        try {
            p = readFromFields();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                    "Could not read a field value: " + nfe.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            generator.generate(p.getNumTrials(), p.getWidth(), p.getHeight());
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
                FileUtil.loadConfigClass("experiment.passive.config_class"));

        new PassivePairGeneratorGUI(
                context.getBean(PassivePairBlockGen.class),
                context.getBean(PairTrialParamDbUtil.class)).show();
    }
}
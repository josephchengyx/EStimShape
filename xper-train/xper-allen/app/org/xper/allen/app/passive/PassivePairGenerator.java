package org.xper.allen.app.passive;

import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.xper.allen.passive.blockgen.PassivePairBlockGen;
import org.xper.util.FileUtil;

public class PassivePairGenerator {
    public static void main(String[] args) {
        // args 0      number of trials
        // args 1-2    imageSize width x height

        int numTrials = Integer.parseInt(args[0]);
        double width = Double.parseDouble(args[1]);
        double height = Double.parseDouble(args[2]);

        JavaConfigApplicationContext context = new JavaConfigApplicationContext(
                FileUtil.loadConfigClass("experiment.passive.config_class"));

        PassivePairBlockGen gen = context.getBean(PassivePairBlockGen.class);

        try {
            gen.toString();
            gen.generate(numTrials, width, height);

        }
        catch(Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}

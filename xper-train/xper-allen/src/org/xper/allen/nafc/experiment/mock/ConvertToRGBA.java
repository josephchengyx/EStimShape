package org.xper.allen.nafc.experiment.mock;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ConvertToRGBA {
    static Color bgColor = Color.WHITE; // change as needed
    static int tolerance = 30;

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        File[] pngFiles = dir.listFiles(new java.io.FilenameFilter() {
            public boolean accept(File d, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        for (File file : pngFiles) {
            BufferedImage src = ImageIO.read(file);

            BufferedImage rgba;
            if (src.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                rgba = src; // already RGBA, just make background transparent
            } else {
                rgba = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                rgba.getGraphics().drawImage(src, 0, 0, null);
            }

            for (int y = 0; y < rgba.getHeight(); y++) {
                for (int x = 0; x < rgba.getWidth(); x++) {
                    Color c = new Color(rgba.getRGB(x, y), true);
                    if (colorDistance(c, bgColor) < tolerance) {
                        rgba.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0).getRGB());
                    }
                }
            }

            ImageIO.write(rgba, "png", file);
            System.out.println("Converted: " + file.getPath());
        }
    }

    static double colorDistance(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
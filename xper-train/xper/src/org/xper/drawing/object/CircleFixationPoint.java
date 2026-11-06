package org.xper.drawing.object;

import org.lwjgl.opengl.GL11;
import org.xper.drawing.Coordinates2D;

public class CircleFixationPoint extends FixationPoint {

	static final int STEPS = 200;

	void drawVertexes(Coordinates2D posInMm, double sizeInMm) {
		double z = 0;

		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), 1f);

		GL11.glPushMatrix();
		GL11.glTranslated(posInMm.getX(), posInMm.getY(), z);
		if (solid) {
			GL11.glBegin(GL11.GL_POLYGON);
		} else {
			GL11.glBegin(GL11.GL_LINE_LOOP);
		}
		for (int i = 0; i < STEPS; i ++) {
			double angle = i * 2 * Math.PI / STEPS;
			GL11.glVertex3d(sizeInMm*Math.cos(angle)/2., sizeInMm*Math.sin(angle)/2., z);
		}
		GL11.glEnd();
		GL11.glPopMatrix();
	}
}
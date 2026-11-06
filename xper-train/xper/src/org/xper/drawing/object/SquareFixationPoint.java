package org.xper.drawing.object;

import org.lwjgl.opengl.GL11;
import org.xper.Dependency;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.Drawable;
import org.xper.drawing.RGBColor;
import org.xper.drawing.renderer.AbstractRenderer;

public class SquareFixationPoint extends FixationPoint {

	void drawVertexes(Coordinates2D posInMm, double sizeInMm) {
		double z = 0;

		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), 1f);

		GL11.glPushMatrix();
		GL11.glTranslated(posInMm.getX(), posInMm.getY(), z);
		if (solid) {
			GL11.glBegin(GL11.GL_QUADS);
		} else {
			GL11.glBegin(GL11.GL_LINE_LOOP);
		}
			GL11.glVertex3d(-sizeInMm/2., -sizeInMm/2., z);
			GL11.glVertex3d(sizeInMm/2., -sizeInMm/2., z);
			GL11.glVertex3d(sizeInMm/2., sizeInMm/2., z);
			GL11.glVertex3d(-sizeInMm/2., sizeInMm/2., z);
		GL11.glEnd();
		GL11.glPopMatrix();
	}
}
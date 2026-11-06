package org.xper.drawing.object;

import org.lwjgl.opengl.GL11;
import org.xper.Dependency;
import org.xper.drawing.Context;
import org.xper.drawing.Coordinates2D;
import org.xper.drawing.Drawable;
import org.xper.drawing.RGBColor;
import org.xper.drawing.renderer.AbstractRenderer;

public abstract class FixationPoint implements Drawable {

	@Dependency
	Coordinates2D fixationPosition;
	@Dependency
	double size; // in degrees
	@Dependency
	RGBColor color;
	@Dependency
	boolean solid = true;

	/**
	 * @param context ignored.
	 */
	public void draw(Context context) {
		AbstractRenderer renderer = context.getRenderer();
		double x = renderer.deg2mm(fixationPosition.getX());
		double y = renderer.deg2mm(fixationPosition.getY());
		double size = renderer.deg2mm(this.size);
		Coordinates2D posInMm = new Coordinates2D(x,y);
		drawVertexes(posInMm, size);
	}

	void drawVertexes(Coordinates2D posInMm, double sizeInMm) {
	}

	public Coordinates2D getFixationPosition() {
		return fixationPosition;
	}

	public void setFixationPosition(Coordinates2D fixationPosition) {
		this.fixationPosition = fixationPosition;
	}

	public RGBColor getColor() {
		return color;
	}

	public void setColor(RGBColor color) {
		this.color = color;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isSolid() {
		return solid;
	}

	public void setSolid(boolean solid) {
		this.solid = solid;
	}
}
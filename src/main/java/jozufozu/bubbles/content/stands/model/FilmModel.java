package jozufozu.bubbles.content.stands.model;

import jozufozu.bubbles.render.FancyRenderedModel;

import jozufozu.bubbles.render.FancyRenderedModel.PositionNormalVertex;
import jozufozu.bubbles.render.FancyRenderedModel.TexturedQuad;

public class FilmModel extends FancyRenderedModel {

    public FilmModel() {
        this.texHeight = 16;
        this.texWidth = 16;

        this.quads = new TexturedQuad[1];

        float xMin = -0.125f;
        float yMin = -0.125f;
        float xMax = 0.125f;
        float yMax = 0.125f;

        PositionNormalVertex vertex000 = new PositionNormalVertex(xMin, yMin, 0f, -1f, -1f, -1f);
        PositionNormalVertex vertex100 = new PositionNormalVertex(xMax, yMin, 0f, 1f, -1f, -1f);
        PositionNormalVertex vertex110 = new PositionNormalVertex(xMax, yMax, 0f, 1f, 1f, -1f);
        PositionNormalVertex vertex010 = new PositionNormalVertex(xMin, yMax, 0f, -1f, 1f, -1f);

        this.quads[0] = new TexturedQuad(new PositionNormalVertex[]{vertex100, vertex000, vertex010, vertex110});
    }
}

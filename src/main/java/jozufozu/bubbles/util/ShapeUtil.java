package jozufozu.bubbles.util;

import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;

public class ShapeUtil {

    public static VoxelShape rotateY(VoxelShape start, int degrees) {
        degrees = Math.floorDiv(degrees % 360, 90) * 90;

        double cos;
        double sin;

        if (degrees == 0) {
            cos = 1;
            sin = 0;
        } else if (degrees == 90) {
            cos = 0;
            sin = 1;
        } else if (degrees == 180) {
            cos = -1;
            sin = 0;
        } else if (degrees == 270) {
            cos = 0;
            sin = -1;
        } else {
            return start;
        }

        ArrayList<VoxelShape> shapes = new ArrayList<>();

        start.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            x1 -= 0.5;
            z1 -= 0.5;
            x2 -= 0.5;
            z2 -= 0.5;

            double x1r = z1 * sin + x1 * cos;
            double z1r = z1 * cos - x1 * sin;
            double x2r = z2 * sin + x2 * cos;
            double z2r = z2 * cos - x2 * sin;

            x1r += 0.5;
            z1r += 0.5;
            x2r += 0.5;
            z2r += 0.5;

            shapes.add(VoxelShapes.box(x1r, y1, z1r, x2r, y2, z2r));
        });

        VoxelShape shape = shapes.get(0);

        for (int i = 1; i < shapes.size(); i++) {
            shape = VoxelShapes.joinUnoptimized(shape, shapes.get(i), IBooleanFunction.OR);
        }

        return shape.optimize();
    }
}

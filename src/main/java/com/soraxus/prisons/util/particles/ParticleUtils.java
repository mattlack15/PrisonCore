package com.soraxus.prisons.util.particles;

import net.ultragrav.utils.Vector3D;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleUtils {
    public static ParticleShape createSquare(World world, Particle particle, Vector3D pos1, Vector3D pos2) {
        ParticleShape shape = new ParticleShape(particle, world);
        square(pos1, pos2, shape);
        return shape;
    }

    public static ParticleShape createSquare(World world, Particle particle, Object data, Vector3D pos1, Vector3D pos2) {
        ParticleShape shape = new ParticleShape(particle, data, world);
        square(pos1, pos2, shape);
        return shape;
    }

    private static void square(Vector3D pos1, Vector3D pos2, ParticleShape shape) {
        shape.addLine(pos1, pos2.setX(pos1.getX()));
        shape.addLine(pos1, pos2.setZ(pos1.getZ()));
        shape.addLine(pos2, pos1.setX(pos2.getX()));
        shape.addLine(pos2, pos1.setZ(pos2.getZ()));
    }

    public static ParticleShape line(World world, Particle particle, Vector3D pos1, Vector3D pos2) {
        ParticleShape shape = new ParticleShape(particle, world);
        shape.addLine(pos1, pos2);
        return shape;
    }
}

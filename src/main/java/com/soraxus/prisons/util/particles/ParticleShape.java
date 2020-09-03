package com.soraxus.prisons.util.particles;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ParticleShape {
    private Particle particle;
    @Setter
    private Object data;
    private double particleDensity;
    private World world;
    private List<Line> lines = new ArrayList<>();

    public ParticleShape(Particle particle, World world) {
        this(particle, 8, world);
    }

    public ParticleShape(Particle particle, Object data, World world) {
        this(particle, data, 8, world);
    }

    public ParticleShape(Particle particle, double particleDensity, World world) {
        this(particle, null, particleDensity, world);
    }

    public ParticleShape(Particle particle, Object data, double particleDensity, World world) {
        this.particle = particle;
        this.data = data;
        this.particleDensity = particleDensity;
        this.world = world;
    }

    public void addLine(Vector3D pos1, Vector3D pos2) {
        this.lines.add(new Line(this, pos1, pos2));
    }

    public void draw() {
        this.lines.forEach(Line::draw);
    }
}

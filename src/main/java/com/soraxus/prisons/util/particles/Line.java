package com.soraxus.prisons.util.particles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Particle;

@Getter
@AllArgsConstructor
public class Line {
    private ParticleShape parent;
    private Vector3D pos1;
    private Vector3D pos2;

    public void draw() {
        double dist = 1 / parent.getParticleDensity();
        Vector3D diff = pos2.subtract(pos1);
        int count = (int) (diff.length() * parent.getParticleDensity());
        diff = diff.normalize().multiply(dist);
        Vector3D curr = pos1;
        for (int i = 0; i < count; i++) {
            if (parent.getParticle().equals(Particle.REDSTONE)) {
                parent.getWorld().spawnParticle(parent.getParticle(), curr.getX(), curr.getY(), curr.getZ(), 1, 255, 255, 255, 1);
            } else if (parent.getData() == null) {
                parent.getWorld().spawnParticle(parent.getParticle(), curr.getX(), curr.getY(), curr.getZ(), 1);
            } else {
                parent.getWorld().spawnParticle(parent.getParticle(), curr.getX(), curr.getY(), curr.getZ(), 1, parent.getData());
            }
            curr = curr.add(diff);
        }
    }
}

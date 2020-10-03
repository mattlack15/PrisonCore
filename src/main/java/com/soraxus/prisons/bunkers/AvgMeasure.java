package com.soraxus.prisons.bunkers;

import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

@Getter
public class AvgMeasure implements GravSerializable {
    private volatile double weightTotal;
    private volatile double avg;

    public AvgMeasure() {
        this(0D, 0D);
    }

    public AvgMeasure(double weightTotal, double avg) {
        this.weightTotal = weightTotal;
        this.avg = avg;
    }

    public synchronized void addEntry(double measure) {
        addEntry(measure, 1D);
    }

    public synchronized void addEntry(double measure, double weight) {
        this.avg += (measure - avg) * weight / (weightTotal += weight);
    }

    public synchronized void setAvg(double avg) {
        this.avg = avg;
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeDouble(weightTotal);
        gravSerializer.writeDouble(avg);
    }

    public static AvgMeasure deserialize(GravSerializer serializer) {
        return new AvgMeasure(serializer.readDouble(), serializer.readDouble());
    }

    //Test
    public static void main(String[] args) {
        AvgMeasure measure = new AvgMeasure();
        measure.addEntry(5);
        measure.addEntry(1);
        measure.addEntry(5);
        measure.addEntry(1);
        measure.addEntry(1, 0.1);
        measure.addEntry(1, 0.1);

        double val = 7;
        System.out.println(val + 0.3);

        System.out.println(measure.getAvg());
    }
}

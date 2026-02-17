package com.monkopedia.kcsg;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class DslJvmStaticCoverageTest {
    @Test
    public void transformAndCsgWrapperStaticsAreInvocableFromJvm() {
        Transform translated = TransformKt.translate(TransformBuilder.INSTANCE.getUnity(), 1.0, 2.0, 3.0);
        Assert.assertEquals(new Vector3d(2.0, 3.0, 4.0), translated.transform(new Vector3d(1.0, 1.0, 1.0)));

        Transform scaledUniform = TransformKt.scale(TransformBuilder.INSTANCE.getUnity(), 2.0);
        Assert.assertEquals(new Vector3d(2.0, 2.0, 2.0), scaledUniform.transform(new Vector3d(1.0, 1.0, 1.0)));

        Transform scaledAxis = TransformKt.scale(TransformBuilder.INSTANCE.getUnity(), 2.0, 3.0, 4.0);
        Assert.assertEquals(new Vector3d(2.0, 3.0, 4.0), scaledAxis.transform(new Vector3d(1.0, 1.0, 1.0)));

        CSG baseCsg = new Cube(2.0).toCSG();
        CSG axisScaledCsg = TransformKt.scale(baseCsg, 2.0, 3.0, 4.0);
        Assert.assertEquals(baseCsg.computeVolume() * 24.0, axisScaledCsg.computeVolume(), 1e-4);

        CSG rotatedCsg = TransformKt.rot(baseCsg, 0.0, 0.0, 90.0);
        Assert.assertEquals(baseCsg.getBounds().getBounds().getZ(), rotatedCsg.getBounds().getBounds().getZ(), 1e-9);
    }

    @Test
    public void collectionWrapperStaticsAreInvocableFromJvm() {
        Collection<CSG> base = Arrays.asList(
            new Cube(1.0).toCSG(),
            new Cube(1.0).toCSG()
        );

        Collection<CSG> translated = CollectionsKt.translate(base, 1.0, 2.0, 3.0);
        Assert.assertEquals(2, translated.size());

        Collection<CSG> scaledAxis = CollectionsKt.scale(base, 2.0, 3.0, 4.0);
        Assert.assertEquals(2, scaledAxis.size());

        Collection<CSG> rotated = CollectionsKt.rot(base, 0.0, 0.0, 90.0);
        Assert.assertEquals(2, rotated.size());
    }
}

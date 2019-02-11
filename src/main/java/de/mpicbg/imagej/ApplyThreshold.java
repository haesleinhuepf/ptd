package de.mpicbg.imagej;

import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.IterableInterval;

import java.util.HashMap;

/**
 * ApplyThreshold
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 02 2019
 */
public class ApplyThreshold {

    private static HashMap<String, Runnable> methods = new HashMap<>();

    private IterableInterval input = null;
    private IterableInterval output = null;

    private static void init() {
        synchronized (methods){
            if (methods.keySet().size() == 0) {
                methods.put("otsu", () -> {
                    output = ops.threshold().otsu(input);
                });
                methods.put("huang", () -> {
                    output = ops.threshold().huang(input);
                });
                methods.put("ij1", () -> {
                    output = ops.threshold().ij1(input);
                });
                methods.put("intermodes", () -> {
                    output = ops.threshold().intermodes(input);
                });
                methods.put("isoData", () -> {
                    output = ops.threshold().isoData(input);
                });
                methods.put("li", () -> {
                    output = ops.threshold().li(input);
                });
                methods.put("maxEntropy", () -> {
                    output = ops.threshold().maxEntropy(input);
                });
                methods.put("mean", () -> {
                    output = ops.threshold().mean(input);
                });
                methods.put("minError", () -> {
                    output = ops.threshold().minError(input);
                });
                methods.put("minimum", () -> {
                    output = ops.threshold().minimum(input);
                });
                methods.put("maxLikelihood", () -> {
                    output = ops.threshold().maxLikelihood(input);
                });
                methods.put("moments", () -> {
                    output = ops.threshold().moments(input);
                });
                methods.put("renyiEntropy", () -> {
                    output = ops.threshold().renyiEntropy(input);
                });
                methods.put("rosin", () -> {
                    output = ops.threshold().rosin(input);
                });
                methods.put("shanbhag", () -> {
                    output = ops.threshold().shanbhag(input);
                });
                methods.put("yen", () -> {
                    output = ops.threshold().yen(input);
                });
                methods.put("triangle", () -> {
                    output = ops.threshold().triangle(input);
                });

            }
        }
    }

    public ApplyThreshold(String method, IterableInterval input, OpService ops) {
        init();
        Runnable r = methods.get(method);
        if (r != null) {
            r.run();
        }
    }

    public static String[] getAvailableThresholdAlgorithms() {
        init();
        String[] output = new String[methods.keySet().size()];
        methods.keySet().toArray(output);
        return output;
    }

    public IterableInterval getOutput() {
        return output;
    }
}

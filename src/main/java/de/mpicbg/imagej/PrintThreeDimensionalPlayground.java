/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package de.mpicbg.imagej;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.io.stl.STLMeshIO;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.DoubleColumn;
import org.scijava.table.IntColumn;
import org.scijava.table.Table;

import java.io.*;
import java.util.Random;

/**
 *
 */
@Plugin(type = Command.class, menuPath = "SpimCat>Presentation>Export STL")
public class PrintThreeDimensionalPlayground<T extends RealType<T>> implements Command {

    @Parameter
    private Dataset currentData;

    @Parameter
    private ImageJ ij;

    @Override
    public void run() {
        final Img<T> image = (Img<T>)currentData.getImgPlus();

        GenericDialogPlus dialog = new GenericDialogPlus("Export STL");
        dialog.addNumericField("Gaussian blur sigma", 2.0, 1);
        dialog.addChoice("Threshold algorithm", ApplyThreshold.getAvailableThresholdAlgorithms(), "ij1");

        dialog.addFileField("Save to: ", "output.stl");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }
        double sigma = dialog.getNextNumber();
        String thresholdAlgorithm = dialog.getNextChoice();
        String filename = dialog.getNextString();

        //String filename = "src/main/resources/hello.stl";

        IJ.showStatus("Blurring");
        IJ.showProgress(0.1);

        // apply a gaussian blur
        RandomAccessibleInterval gaussFiltered = ij.op().filter().gauss(image, sigma, sigma, sigma);

        // show the blurred image
        ij.ui().show(gaussFiltered);

        IJ.showStatus("Thresholding");
        IJ.showProgress(0.2);

        // apply a threshold using selected method
        IterableInterval thresholded = new ApplyThreshold(thresholdAlgorithm, Views.iterable(gaussFiltered), ij.op()).getOutput();
                //ij.op().threshold().otsu(Views.iterable(gaussFiltered));

        ij.ui().show(thresholded);

        IJ.showStatus("Derive mesh");
        IJ.showProgress(0.3);

        Mesh mesh2 = ij.op().geom().marchingCubes((Img)thresholded);

        IJ.showStatus("Saving");
        IJ.showProgress(0.9);

        try {
            ij.io().save(mesh2, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Bye!");
        IJ.showStatus("Export STL done");
        IJ.showProgress(1.0);


        if (true) return;

        ImgLabeling cca = ij.op().labeling().cca((Img) thresholded, ConnectedComponents.StructuringElement.FOUR_CONNECTED);

        LabelRegions<IntegerType> regions = new LabelRegions(cca);

        File file = new File(filename);
        FileOutputStream fileWriter = null;
        try {
            fileWriter = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (LabelRegion region : regions) {
            Mesh mesh = ij.op().geom().marchingCubes(region);


            STLMeshIO writer = new STLMeshIO();
            byte[] array = writer.write(mesh);

            try {
                fileWriter.write(array);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
    }

    private void invertBinaryImage(IterableInterval input) {
        Cursor cursor = input.cursor();

        while(cursor.hasNext()) {
            BitType pixel = (BitType) cursor.next();
            pixel.set(! pixel.get());
        }

    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // ask the user for a file to open
        final File file = new File("C:/structure/data/organoids_ByungHo/input_crop.tif");
                //"src/main/resources/t1-head.tif");

        if (file != null) {
            // load the dataset
            final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());

            // show the image
            ij.ui().show(dataset);

            // invoke the plugin
            ij.command().run(PrintThreeDimensionalPlayground.class, true);
        }
    }

}

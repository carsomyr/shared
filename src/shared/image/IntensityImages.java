/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 2.1 of the License, or (at your option)
 * any later version. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. <br />
 * <br />
 * You should have received a copy of the GNU Lesser General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 */

package shared.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import shared.array.RealArray;
import shared.array.Array.IndexingOrder;
import shared.util.Control;

/**
 * A collection of static methods for reading and writing intensity images represented as {@link RealArray}s.
 * 
 * @author Roy Liu
 */
public class IntensityImages {

    /**
     * A mapping of color names to interpolating color maps.
     */
    final protected static Map<String, int[]> ColorMaps;

    static {

        ColorMaps = new HashMap<String, int[]>();

        ColorMaps.put("gray", createInterpolatingColorMap( //
                0, 0xFF000000, //
                255, 0xFFFFFFFF));

        ColorMaps.put("jet", createInterpolatingColorMap( //
                0, 0xFF000080, //
                31, 0xFF0000FF, //
                95, 0xFF00FFFF, //
                160, 0xFFFFFF00, //
                223, 0xFFFF0000, //
                255, 0xFF800000));
    }

    /**
     * Converts a {@link RealArray} to a {@link BufferedImage}.
     * 
     * @param m
     *            the intensity image.
     * @param cmName
     *            the name of the color map to display with.
     * @param rangeMin
     *            the minimum intensity.
     * @param rangeMax
     *            the maximum intensity.
     * @return an image representation.
     */
    final public static BufferedImage createImage(RealArray m, String cmName, double rangeMin, double rangeMax) {

        Control.checkTrue(m.ndims() == 2, //
                "Number of array dimensions must equal two");

        Control.checkTrue(rangeMax > rangeMin, //
                "Invalid intensity range");

        int[] colorMap = ColorMaps.get(cmName);

        Control.checkTrue(colorMap != null, //
                "Invalid color map name");

        m = m.transpose(1, 0).reverseOrder().uAdd(-rangeMin) //
                .uMul(1.0 / (rangeMax - rangeMin));

        double[] backing = m.values();
        int[] arr = new int[backing.length];

        for (int i = 0, n = backing.length; i < n; i++) {
            arr[i] = colorMap[(int) (backing[i] * 255)];
        }

        BufferedImage bi = new BufferedImage(m.size(0), m.size(1), BufferedImage.TYPE_INT_RGB);
        bi.getRaster().setDataElements(0, 0, m.size(0), m.size(1), arr);

        return bi;
    }

    /**
     * Converts a {@link BufferedImage} to a {@link RealArray}.
     * 
     * @param image
     *            the original image.
     * @return an intensity image representation.
     */
    final public static RealArray createMatrix(BufferedImage image) {

        Raster raster = ensureGrayscale(image).getRaster();

        RealArray res = new RealArray(IndexingOrder.NEAR, image.getWidth(), image.getHeight());

        raster.getSamples(0, 0, image.getWidth(), image.getHeight(), 0, res.values());

        return res.reverseOrder().transpose(1, 0).uMul(1.0d / 256);
    }

    /**
     * Creates a {@link RealArray} from an image file.
     * 
     * @param file
     *            the image file.
     * @return the intensity image.
     * @throws IOException
     *             when the load operation goes awry.
     */
    final public static RealArray createMatrix(File file) throws IOException {
        return createMatrix(ImageIO.read(file));
    }

    /**
     * Creates a {@link RealArray} from a {@code byte} array.
     * 
     * @param data
     *            the binary data.
     * @return the intensity image.
     * @throws IOException
     *             when the load operation goes awry.
     */
    final public static RealArray createMatrix(byte[] data) throws IOException {
        return createMatrix(bytesToJpeg(data));
    }

    /**
     * Converts a {@link BufferedImage} to a {@code byte} array.
     * 
     * @param bi
     *            the image.
     * @return the binary data.
     * @throws IOException
     *             when the encoding operation goes awry.
     */
    final public static byte[] jpegToBytes(BufferedImage bi) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(ensureGrayscale(bi), "jpeg", out);

        return out.toByteArray();
    }

    /**
     * Converts a {@code byte} array to a {@link BufferedImage}.
     * 
     * @param data
     *            the binary data.
     * @return the image.
     * @throws IOException
     *             when the decoding operation goes awry.
     */
    final public static BufferedImage bytesToJpeg(byte[] data) throws IOException {

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BufferedImage bi = ImageIO.read(in);

        return bi;
    }

    /**
     * Ensures that an image is in grayscale, converting it if necessary.
     */
    final protected static BufferedImage ensureGrayscale(BufferedImage src) {

        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), //
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return dst;
    }

    /**
     * Creates a color map by interpolating between start, end, and intermediate colors.
     */
    final protected static int[] createInterpolatingColorMap(int... args) {

        Control.checkTrue(args.length < 4 //
                || args.length % 2 == 0 //
                || args[0] != 0 //
                || args[args.length - 2] != 255, //
                "Invalid arguments");

        int[] indices = new int[args.length / 2];
        int[] values = new int[args.length / 2];

        for (int i = 0, n = indices.length; i < n; i++) {

            indices[i] = args[2 * i];
            values[i] = args[2 * i + 1];
        }

        int[] colorMap = new int[256];

        for (int i = 1, n = indices.length; i < n; i++) {

            int lower = indices[i - 1];
            int upper = indices[i];
            int diff = upper - lower;

            if (diff <= 0) {
                throw new RuntimeException("Invalid arguments");
            }

            int srcARGB = values[i - 1];
            int dstARGB = values[i];

            int aSrc = (srcARGB >>> 24) & 0xFF;
            int rSrc = (srcARGB >>> 16) & 0xFF;
            int gSrc = (srcARGB >>> 8) & 0xFF;
            int bSrc = srcARGB & 0xFF;

            double aIncr = (((dstARGB >>> 24) & 0xFF) - aSrc) / (double) diff;
            double rIncr = (((dstARGB >>> 16) & 0xFF) - rSrc) / (double) diff;
            double gIncr = (((dstARGB >>> 8) & 0xFF) - gSrc) / (double) diff;
            double bIncr = ((dstARGB & 0xFF) - bSrc) / (double) diff;

            for (int j = lower; j <= upper; j++) {

                int a = ((((int) Math.round(aSrc + (j - lower) * aIncr)) & 0xFF) << 24);
                int r = ((((int) Math.round(rSrc + (j - lower) * rIncr)) & 0xFF) << 16);
                int g = ((((int) Math.round(gSrc + (j - lower) * gIncr)) & 0xFF) << 8);
                int b = (((int) Math.round(bSrc + (j - lower) * bIncr)) & 0xFF);

                colorMap[j] = a | r | g | b;
            }
        }

        return colorMap;
    }

    // Dummy constructor.
    IntensityImages() {
    }
}

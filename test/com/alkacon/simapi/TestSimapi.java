/*
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.simapi;

import com.alkacon.simapi.filter.ContrastFilter;
import com.alkacon.simapi.filter.GrayscaleFilter;
import com.alkacon.simapi.filter.ImageMath;
import com.alkacon.simapi.filter.LinearColormap;
import com.alkacon.simapi.filter.LookupFilter;
import com.alkacon.simapi.filter.RotateFilter;
import com.alkacon.simapi.filter.ShadowFilter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the imaging operations.<p>
 */
public class TestSimapi extends VisualTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param params JUnit parameters
     */
    public TestSimapi(String params) {

        super(params);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestSimapi.class.getName());

        suite.addTest(new TestSimapi("testCMYKJpeg"));

        suite.addTest(new TestSimapi("testSpecialScaleSize"));
        suite.addTest(new TestSimapi("testCropPointToSize"));

        suite.addTest(new TestSimapi("testRead"));
        suite.addTest(new TestSimapi("testWriteGif"));
        suite.addTest(new TestSimapi("testWriteJpegAndPng"));
        suite.addTest(new TestSimapi("testWriteJpegQuality"));

        suite.addTest(new TestSimapi("testBadScaleQualityIssue"));
        suite.addTest(new TestSimapi("testBadScaleQualityIssue2"));
        suite.addTest(new TestSimapi("testBadScaleQualityIssue3"));
        suite.addTest(new TestSimapi("testNotSharpEnoughIssue"));

        suite.addTest(new TestSimapi("testCrop"));
        suite.addTest(new TestSimapi("testImageCropping"));

        suite.addTest(new TestSimapi("testFilters"));
        suite.addTest(new TestSimapi("testGIFProcessing"));
        suite.addTest(new TestSimapi("testResizeCrop"));
        suite.addTest(new TestSimapi("testResizeScale"));
        suite.addTest(new TestSimapi("testResizeScaleFill"));
        suite.addTest(new TestSimapi("testResizeScaleFillSmall"));
        suite.addTest(new TestSimapi("testScaleTransparent"));
        suite.addTest(new TestSimapi("testScalingSpeed"));
        suite.addTest(new TestSimapi("testScreenShotScaling"));
        suite.addTest(new TestSimapi("testSetupWizardIssue"));
        suite.addTest(new TestSimapi("testSlowScalingIssue"));

        TestSetup wrapper = new TestSetup(suite);
        return wrapper;
    }

    /**
     * Tests "bad quality" issue encountered when scaling large images to a very small size.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBadScaleQualityIssue() throws Exception {

        Simapi simapi = new Simapi();

        BufferedImage img1 = Simapi.read(getClass().getResource("112_org.jpg"));
        BufferedImage img2 = Simapi.read(getClass().getResource("113_org.jpg"));
        BufferedImage img3 = Simapi.read(getClass().getResource("114_org.jpg"));

        img1 = simapi.resize(img1, 150, 113, Color.RED, Simapi.POS_CENTER);
        img2 = simapi.resize(img2, 150, 113, Color.RED, Simapi.POS_CENTER);
        img3 = simapi.resize(img3, 150, 113, Color.RED, Simapi.POS_CENTER);

        checkImage(new BufferedImage[] {img1, img2, img3}, "Is the quality ok?");
    }

    /**
     * Tests "bad quality" issue encountered when scaling VERY large images to a small size.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBadScaleQualityIssue2() throws Exception {

        RenderSettings settings = new RenderSettings(Simapi.RENDER_QUALITY);
        settings.setCompressionQuality(0.97f);
        Simapi simapi = new Simapi(settings);

        BufferedImage img1 = Simapi.read(getClass().getResource("Messdiener.jpg"));
        BufferedImage img2 = Simapi.read(getClass().getResource("Messdiener_sml.jpg"));

        img1 = simapi.resize(img1, 800, 533, Color.RED, Simapi.POS_CENTER);

        checkImage(new BufferedImage[] {img1, img2}, "Is the quality ok? [Left: Simapi / Right: Prescaled]");

        img1 = Simapi.read(getClass().getResource("img_0005.jpg"));
        img1 = simapi.resize(img1, 800, 534, Color.RED, Simapi.POS_CENTER);

        File baseDir = new File(getClass().getResource("img_0005.jpg").getPath()).getParentFile();
        simapi.write(img1, new File(baseDir, "w_img_0005.jpg"), Simapi.TYPE_JPEG);
        img1 = Simapi.read(getClass().getResource("w_img_0005.jpg"));

        checkImage(new BufferedImage[] {img1}, "Was it witten as JPEG in a good quality?");
    }

    /**
     * Tests "bad quality" issue encountered when scaling large images to a very small size.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBadScaleQualityIssue3() throws Exception {

        RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY_BICUBIC);
        Simapi simapi = new Simapi(rs);

        BufferedImage img1 = Simapi.read(getClass().getResource("funke_case.jpg"));
        img1 = simapi.resize(img1, 1024, 768, Color.RED, Simapi.POS_CENTER);

        File baseDir = new File(getClass().getResource("funke_case.jpg").getPath()).getParentFile();
        simapi.write(img1, new File(baseDir, "w_funke_case.jpg"), Simapi.TYPE_JPEG);
        img1 = Simapi.read(getClass().getResource("w_funke_case.jpg"));

        checkImage(new BufferedImage[] {img1}, "Is the quality ok?");
    }

    /**
     * Tests reading / scaling / writing a CMYK JPEG image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCMYKJpeg() throws Exception {

        File input;
        File destination;
        RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY);
        rs.setCompressionQuality(0.5f);
        Simapi simapi = new Simapi(rs);
        BufferedImage source, scaled, written;

        // first a simple "can we read this" test
        input = new File(getClass().getResource("cms-tueten-cmyk.jpg").getPath());
        source = Simapi.read(input);
        checkImage(new BufferedImage[] {source}, "Was the CMYK image correctly read?");

        // now see if we can scale the CMYK and write to disk as RGB
        input = new File(getClass().getResource("blume-CMYK.jpg").getPath());
        source = Simapi.read(input);
        scaled = simapi.resize(source, 640, 480, Color.WHITE, Simapi.POS_CENTER, true);
        destination = new File(input.getParentFile(), "blume-CMYK-saved.jpg");
        simapi.write(scaled, destination, Simapi.TYPE_JPEG);
        written = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {written},
            "Has the CMYK image been written to disk as JPEG in a scaled version?");

        // iterate to ensure all images in question work
        input = new File(getClass().getResource("CMYK-p1.jpg").getPath());
        source = Simapi.read(input);
        scaled = simapi.resize(source, 480, 640, Color.WHITE, Simapi.POS_CENTER, true);
        destination = new File(input.getParentFile(), "CMYK-p1-saved.jpg");
        simapi.write(scaled, destination, Simapi.TYPE_JPEG);
        written = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {written},
            "Has the CMYK image been written to disk as JPEG in a scaled version?");

        // iterate to ensure all images in question work
        input = new File(getClass().getResource("CMYK-p2.jpg").getPath());
        source = Simapi.read(input);
        scaled = simapi.resize(source, 480, 640, Color.WHITE, Simapi.POS_CENTER, true);
        destination = new File(input.getParentFile(), "CMYK-p2-saved.jpg");
        simapi.write(scaled, destination, Simapi.TYPE_JPEG);
        written = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {written},
            "Has the CMYK image been written to disk as JPEG in a scaled version?");

        // iterate to ensure all images in question work
        input = new File(getClass().getResource("CMYK-p3.jpg").getPath());
        source = Simapi.read(input);
        scaled = simapi.resize(source, 480, 640, Color.WHITE, Simapi.POS_CENTER, true);
        destination = new File(input.getParentFile(), "CMYK-p3-saved.jpg");
        simapi.write(scaled, destination, Simapi.TYPE_JPEG);
        written = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {written},
            "Has the CMYK image been written to disk as JPEG in a scaled version?");

        // iterate to ensure all images in question work
        input = new File(getClass().getResource("CMYK-p4.jpg").getPath());
        source = Simapi.read(input);
        scaled = simapi.resize(source, 480, 640, Color.WHITE, Simapi.POS_CENTER, true);
        destination = new File(input.getParentFile(), "CMYK-p4-saved.jpg");
        simapi.write(scaled, destination, Simapi.TYPE_JPEG);
        written = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {written},
            "Has the CMYK image been written to disk as JPEG in a scaled version?");
    }

    /**
     * Tests cropping an image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCrop() throws Exception {

        Simapi simapi = new Simapi();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = Simapi.read(input);

        result1 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_LEFT);
        result2 = simapi.crop(img1, 250, 250, Simapi.POS_CENTER);
        result3 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped MIDDLE at left, center right");

        result1 = simapi.crop(img1, 250, 250, Simapi.POS_UP_LEFT);
        result2 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_UP);
        result3 = simapi.crop(img1, 250, 250, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped UP at left, center, right");

        result1 = simapi.crop(img1, 250, 250, Simapi.POS_DOWN_LEFT);
        result2 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_DOWN);
        result3 = simapi.crop(img1, 250, 250, Simapi.POS_DOWN_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped DOWN at left, center, right");
    }

    /**
     * Tests cropping an image with a given middle point.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCropPointToSize() throws Exception {

        Simapi simapi = new Simapi();
        BufferedImage img1, result1;

        img1 = Simapi.read(getClass().getResource("Messdiener_sml.jpg"));
        result1 = simapi.cropPointToSize(img1, 360, 234, false, 100, 200);
        checkImage(new BufferedImage[] {drawCross(img1, 360, 234), result1}, "Has it been cropped around the point?");

        result1 = simapi.cropPointToSize(img1, 360, 234, true, 100, 200);
        checkImage(
            new BufferedImage[] {drawCross(img1, 360, 234), result1},
            "Has it been cropped around the point and downscaled?");

        result1 = simapi.cropPointToSize(img1, 461, 506, false, 200, 100);
        checkImage(
            new BufferedImage[] {drawCross(img1, 461, 506), result1},
            "Has it been cropped around the point with offset?");

        img1 = Simapi.read(getClass().getResource("CMYK-p2.jpg"));
        result1 = simapi.cropPointToSize(img1, 93, 154, true, 500, 150);
        checkImage(
            new BufferedImage[] {drawCross(img1, 93, 154), result1},
            "Has it been cropped around the point and upscaled?");

        img1 = Simapi.read(getClass().getResource("CMYK-p4.jpg"));
        result1 = simapi.cropPointToSize(img1, 291, 420, true, 200, 200);
        checkImage(
            new BufferedImage[] {drawCross(img1, 291, 420), result1},
            "Has it been cropped around the point and downscaled?");

        img1 = Simapi.read(getClass().getResource("_3_tn.jpg"));
        result1 = simapi.cropPointToSize(img1, 57, 50, true, 200, 200);
        checkImage(
            new BufferedImage[] {drawCross(img1, 57, 50), result1},
            "Has it been cropped around the point and downscaled?");
    }

    /**
     * Tests writing an image as JPEG with different quality settings.<p>
     *
     * @throws Exception if the test fails
     */
    public void testFilters() throws Exception {

        BufferedImage read;
        File input;

        input = new File(getClass().getResource("DSCN0754.JPG").getPath());

        //      // Java standard metadata extraction - no Exif info is read from file
        //      ImageInputStream stream = ImageIO.createImageInputStream(input);
        //      Iterator iter = ImageIO.getImageReaders(stream);
        //      ImageReader reader = (ImageReader)iter.next();
        //      ImageReadParam param = reader.getDefaultReadParam();
        //      reader.setInput(stream, true, true);
        //      IIOMetadata meta = reader.getImageMetadata(0);
        //      if (meta != null) {
        //          String formats[] = meta.getMetadataFormatNames();
        //          System.out.println("format count: " + formats.length);
        //          for (int i=0; i<formats.length; i++) {
        //              System.out.println("format " + i + ": " + formats[i]);
        //              Node metaData = meta.getAsTree(formats[i]);
        //              TransformerFactory tFactory = TransformerFactory.newInstance();
        //              Transformer transformer = tFactory.newTransformer();
        //              DOMSource source = new DOMSource(metaData);
        //              StreamResult sr = new StreamResult(System.out);
        //              transformer.transform(source, sr);
        //          }
        //      } else {
        //          System.out.println("meta is null");
        //      }
        //      BufferedImage img0 = reader.read(0, param);
        //      stream.close();
        //      reader.dispose();
        //

        //      Metadata metadata = JpegMetadataReader.readMetadata(input);
        //      // iterate through metadata directories
        //      Iterator directories = metadata.getDirectoryIterator();
        //      while (directories.hasNext()) {
        //          Directory directory = (Directory)directories.next();
        //          // iterate through tags and print to System.out
        //          Iterator tags = directory.getTagIterator();
        //          while (tags.hasNext()) {
        //              Tag tag = (Tag)tags.next();
        //              // use Tag.toString()
        //              System.out.println(tag);
        //          }
        //      }

        /* TODO: Writing of Exif information
         *
         * Libary for Exif extraction AND writing (but only with Java 1.5):
         * http://mediachest.sourceforge.net/mediautil/
         */

        RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY);
        Simapi simapi = new Simapi(rs);

        read = Simapi.read(input);
        read = simapi.resize(read, 800, 600, false);
        checkImage(new BufferedImage[] {read}, "Has it been read?");
        assertEquals(800, read.getWidth());
        assertEquals(600, read.getHeight());

        GrayscaleFilter grayscaleFilter = new GrayscaleFilter();
        BufferedImage gray = simapi.applyFilter(read, grayscaleFilter);
        checkImage(new BufferedImage[] {gray}, "Is is gray?");

        ContrastFilter contrastFilter = new ContrastFilter();
        contrastFilter.setGain(0.7f);
        contrastFilter.setBias(0.7f);
        BufferedImage contrast = simapi.applyFilter(read, contrastFilter);
        checkImage(new BufferedImage[] {contrast}, "What about the contrast?");

        //      OilFilter oilFilter = new OilFilter();
        //      BufferedImage oil = simapi.applyFilter(read, oilFilter);
        //      checkImage(new BufferedImage[] {oil}, "In Oil?");

        ShadowFilter shadowFilter = new ShadowFilter();
        shadowFilter.setXOffset(10);
        shadowFilter.setYOffset(10);
        shadowFilter.setOpacity(128);
        shadowFilter.setBackgroundColor(Color.RED.getRGB());
        BufferedImage shadow = simapi.applyFilter(read, shadowFilter);
        checkImage(new BufferedImage[] {shadow}, "What about the shadow?");
        assertEquals(820, shadow.getWidth());
        assertEquals(620, shadow.getHeight());

        LinearColormap colormap = new LinearColormap();
        colormap.setColor1(new Color(162, 138, 101).getRGB());
        colormap.setColor1(new Color(156, 113, 46).getRGB());
        colormap.setColor2(new Color(255, 255, 255).getRGB());
        colormap.setColor2(new Color(255, 241, 201).getRGB());
        //      colormap.setColor1(Color.BLACK.getRGB());
        //      colormap.setColor2(Color.YELLOW.getRGB());
        LookupFilter lookupFilter = new LookupFilter();
        lookupFilter.setColormap(colormap);
        BufferedImage lookup = simapi.applyFilter(read, lookupFilter);
        checkImage(new BufferedImage[] {lookup}, "Sepia effect?");

        // read = Simapi.read(getClass().getResource("logo_alkacon_150_t.gif").getPath());

        rs.addImageFilter(grayscaleFilter);
        rs.addImageFilter(shadowFilter);
        rs.setTransparentReplaceColor(Simapi.COLOR_TRANSPARENT);
        shadowFilter.setBackgroundColor(Color.RED.getRGB());
        shadowFilter.setXOffset(5);
        shadowFilter.setYOffset(5);
        shadowFilter.setOpacity(196);
        Rectangle targetSize = simapi.applyFilterDimensions(read.getWidth(), read.getHeight());
        BufferedImage combined = simapi.resize(
            read,
            (int)targetSize.getWidth(),
            (int)targetSize.getHeight(),
            Simapi.COLOR_TRANSPARENT,
            Simapi.POS_CENTER);
        combined = simapi.applyFilters(combined);
        checkImage(new BufferedImage[] {combined}, "Combined grayscale and shadow effects?");
        assertEquals(read.getWidth(), combined.getWidth());
        assertEquals(read.getHeight(), combined.getHeight());
    }

    /**
     * Tests an issue with JDK 6 and GIF image processing.<p>
     *
     * In a JDK 6 a GIF writer has been introduced. While this is
     * great in general, we already have our own implementation. Actually it appears that
     * the JDK default writer has an issue or at least behaves differently when scaling images
     * that contain transparent pixels.
     *
     *  @throws Exception if the test fails
     */
    public void testGIFProcessing() throws Exception {

        Simapi simapi = new Simapi();
        BufferedImage result;

        File input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);

        result = simapi.resize(img1, 75, 75, true);
        File destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved1.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        BufferedImage read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 75x27 pixel and saved as GIF with transparent background ok?");
        assertEquals(75, read.getWidth());
        assertEquals(27, read.getHeight()); // aspect ratio kept intact
    }

    /**
     * Tests image cropping.<p>
     *
     *  @throws Exception if the test fails
     */
    public void testImageCropping() throws Exception {

        File input = new File(getClass().getResource("screen_1024.png").getPath());
        Simapi simapi = new Simapi();
        BufferedImage result, img1;

        img1 = Simapi.read(input);

        result = simapi.crop(img1, 10, 10, 400, 400);
        checkImage(new BufferedImage[] {result}, "Has it been cropped?");

        result = simapi.crop(img1, 0, -100, 1250, 400);
        checkImage(new BufferedImage[] {result}, "Has it been cropped (oversized)?");

        result = simapi.cropToSize(img1, 0, 0, 160, 120, 800, 600);
        checkImage(new BufferedImage[] {result}, "Has it been cropped and resized (enlarged)?");

        result = simapi.cropToSize(img1, -50, -50, 400, 300, 160, 120);
        checkImage(new BufferedImage[] {result}, "Has it been cropped and resized (reduced)?");

        // another test with an image that support transparent colors
        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        img1 = Simapi.read(input);

        result = simapi.cropToSize(img1, 0, 0, 100, 100, 200, 200);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with transparent color intact?");

        result = simapi.cropToSize(img1, 0, 0, 100, 100, 200, 200, Color.RED);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with red bg color?");

        result = simapi.cropToSize(img1, 21, 15, 1050, 23, 1000, 500, Color.GREEN);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with green bg color and transformed?");
    }

    /**
     * Tests "not sharp enough" issue encountered when scaling large images to a very small size.<p>
     *
     * @throws Exception if the test fails
     */
    public void testNotSharpEnoughIssue() throws Exception {

        Simapi simapi = new Simapi();

        RenderSettings settings = new RenderSettings(Simapi.RENDER_QUALITY);
        settings.setCompressionQuality(0.85f);

        BufferedImage img1 = Simapi.read(getClass().getResource("_1_or.jpg"));
        BufferedImage img2 = Simapi.read(getClass().getResource("_2_or.jpg"));
        BufferedImage img3 = Simapi.read(getClass().getResource("_3_or.jpg"));

        BufferedImage img1Tn = Simapi.read(getClass().getResource("_1_tn.jpg"));
        BufferedImage img2Tn = Simapi.read(getClass().getResource("_2_tn.jpg"));
        BufferedImage img3Tn = Simapi.read(getClass().getResource("_3_tn.jpg"));

        img1 = simapi.resize(img1, 75, 113, Color.RED, Simapi.POS_CENTER);
        img2 = simapi.resize(img2, 150, 100, Color.RED, Simapi.POS_CENTER);
        img3 = simapi.resize(img3, 150, 100, Color.RED, Simapi.POS_CENTER);

        checkImage(
            new BufferedImage[] {img1, img1Tn, img2, img2Tn, img3, img3Tn},
            "Are the images sharp enough? (Left: Simapi / Right: Photoshop)");

        BufferedImage imgOri = Simapi.read(getClass().getResource("112_org.jpg"));

        BufferedImage imgA = simapi.resize(imgOri, 600, 450, Color.RED, Simapi.POS_CENTER);
        BufferedImage imgB = simapi.resize(imgOri, 400, 300, Color.RED, Simapi.POS_CENTER);
        BufferedImage imgC = simapi.resize(imgOri, 300, 225, Color.RED, Simapi.POS_CENTER);
        BufferedImage imgD = simapi.resize(imgOri, 200, 150, Color.RED, Simapi.POS_CENTER);
        BufferedImage imgE = simapi.resize(imgOri, 150, 113, Color.RED, Simapi.POS_CENTER);

        checkImage(new BufferedImage[] {imgA, imgB, imgC, imgD, imgE}, "Are the images sharp enough?");
    }

    /**
     * Tests reading an image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRead() throws Exception {

        BufferedImage img1 = Simapi.read(getClass().getResource("alkacon.png"));
        BufferedImage img2 = Simapi.read(getClass().getResource("alkacon_text.jpg"));
        BufferedImage img3 = Simapi.read(getClass().getResource("logo_alkacon_150_t.gif"));

        checkImage(new BufferedImage[] {img1, img2, img3}, "Do you see 3 images?");
    }

    /**
     * Tests cropping and resizing an image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testResizeCrop() throws Exception {

        Simapi simapi = new Simapi();

        File input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        BufferedImage img1 = Simapi.read(input);
        BufferedImage result1 = simapi.resize(img1, 200, 100, Simapi.POS_CENTER);
        BufferedImage result2 = simapi.resize(img1, 200, 100, Simapi.POS_STRAIGHT_LEFT);
        BufferedImage result3 = simapi.resize(img1, 200, 100, Simapi.POS_STRAIGHT_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be resized and cropped at center, left, right");

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = Simapi.read(input);
        result1 = simapi.resize(img1, 400, 200, Simapi.POS_CENTER);
        result2 = simapi.resize(img1, 400, 200, Simapi.POS_STRAIGHT_UP);
        result3 = simapi.resize(img1, 400, 200, Simapi.POS_STRAIGHT_DOWN);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be resized and cropped at center, up, down");
    }

    /**
     * Tests resizing and scaling an image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testResizeScale() throws Exception {

        Simapi simapi = new Simapi();

        BufferedImage result;

        BufferedImage imgBull = Simapi.read(getClass().getResource("bull.jpg"));
        result = simapi.resize(imgBull, 150, 80, Color.RED, Simapi.POS_CENTER, true);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (reduced) to 150x80 pixel with RED background fill?");
        assertEquals(150, result.getWidth());
        assertEquals(80, result.getHeight());

        result = simapi.resize(imgBull, 450, 450, Color.RED, Simapi.POS_CENTER, true);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (enlarged) to 450x450 pixel WITH blowup and red background fill?");
        assertEquals(450, result.getWidth());
        assertEquals(450, result.getHeight());

        result = simapi.resize(imgBull, 450, 450, Color.RED, Simapi.POS_CENTER, false);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (enlarged) to 450x450 pixel WITHOUT blowup and red background fill?");
        assertEquals(450, result.getWidth());
        assertEquals(450, result.getHeight());

        result = simapi.resize(imgBull, 250, 250, Color.RED, Simapi.POS_CENTER, false);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (reduced) to 250x250 pixel WITHOUT blowup and red background fill?");
        assertEquals(250, result.getWidth());
        assertEquals(250, result.getHeight());

        result = simapi.resize(imgBull, 350, 450, false);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled to 350x450 pixel without keeping PROPORTIONS?");
        assertEquals(350, result.getWidth());
        assertEquals(450, result.getHeight());

        result = simapi.resize(imgBull, 350, 450, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (enlarged) to 350x450 pixel with CROP up right?");
        assertEquals(350, result.getWidth());
        assertEquals(450, result.getHeight());

        result = simapi.resize(imgBull, 200, 250, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (reduced) to 200x250 pixel with CROP up right?");
        assertEquals(200, result.getWidth());
        assertEquals(250, result.getHeight());

        BufferedImage img1 = Simapi.read(getClass().getResource("screen1.png"));
        result = simapi.scale(img1, 0.75f);
        checkImage(new BufferedImage[] {img1, result}, "Has it been scaled to 75%?");
        assertEquals(Math.round(img1.getWidth() * 0.75f), result.getWidth());
        assertEquals(Math.round(img1.getHeight() * 0.75f), result.getHeight());

        BufferedImage img2 = Simapi.read(getClass().getResource("alkacon_text.jpg"));
        result = simapi.resize(img2, 100, 50);

        checkImage(new BufferedImage[] {img2, result}, "Has it been resized to 100x50 pixel?");
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());

        BufferedImage img3 = Simapi.read(getClass().getResource("alkacon.png"));
        result = simapi.resize(img3, 400, 300, true);

        checkImage(new BufferedImage[] {img3, result}, "Has it been resized to 400x300 pixel with aspect intact?");
        assertEquals(400, result.getWidth());
        assertEquals(254, result.getHeight()); // aspect ratio height is 255
    }

    /**
     * Tests resizing/scaling and filling an image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testResizeScaleFill() throws Exception {

        Simapi simapi = new Simapi();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = Simapi.read(input);

        result1 = simapi.resize(img1, 250, 250, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = simapi.resize(img1, 350, 250, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = simapi.resize(img1, 250, 250, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        img1 = Simapi.read(input);

        result1 = simapi.resize(img1, 150, 150, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = simapi.resize(img1, 250, 50, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = simapi.resize(img1, 150, 150, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");
    }

    /**
     * Tests resizing/scaling and filling a small image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testResizeScaleFillSmall() throws Exception {

        Simapi simapi = new Simapi();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        img1 = Simapi.read(input);

        result1 = simapi.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = simapi.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = simapi.resize(img1, 300, 300, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        img1 = Simapi.read(input);

        result1 = simapi.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_CENTER);
        result2 = simapi.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_DOWN_LEFT);
        result3 = simapi.resize(img1, 300, 300, Color.GREEN, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed center, down left, up right");
    }

    /**
     * Tests resizing a transparent image.<p>
     *
     * @throws Exception if the test fails
     */
    public void testScaleTransparent() throws Exception {

        Simapi simapi = new Simapi();
        BufferedImage result;

        BufferedImage imgBull = Simapi.read(getClass().getResource("bull.jpg"));
        result = simapi.resize(imgBull, 500, 500, Simapi.COLOR_TRANSPARENT, Simapi.POS_CENTER, true);
        checkImage(
            new BufferedImage[] {imgBull, result},
            "Has it been scaled (reduced) to 500x500 pixel with WHITE background fill?");
        assertEquals(500, result.getWidth());
        assertEquals(500, result.getHeight());

        File input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);

        result = simapi.resize(img1, 75, 75, true);
        File destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved1.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        BufferedImage read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 75x27 pixel and saved as GIF with transparent background ok?");
        assertEquals(75, read.getWidth());
        assertEquals(27, read.getHeight()); // aspect ratio kept intact

        result = simapi.resize(img1, 75, 27, Color.RED, Simapi.POS_CENTER);
        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved1b.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 75x27 pixel and saved as GIF with transparent background replaced by red?");

        result = simapi.resize(img1, 75, 75, false);
        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved2.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 75x75 pixel and saved as GIF with transparent background ok?");
        assertEquals(75, read.getWidth());
        assertEquals(75, read.getHeight());

        result = simapi.resize(img1, 300, 300, true);
        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved3.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 300x108 pixel and saved as GIF with transparent background ok?");
        assertEquals(300, read.getWidth());
        assertEquals(108, read.getHeight()); // aspect ratio kept intact

        result = simapi.resize(img1, 300, 300, Color.RED, Simapi.POS_CENTER);
        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved4.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 300x300 pixel and saved as GIF with RED fill color?");
        assertEquals(300, read.getWidth());
        assertEquals(300, read.getHeight());

        result = simapi.resize(img1, 300, 300, Simapi.COLOR_TRANSPARENT, Simapi.POS_CENTER);
        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.gif");
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 300x300 pixel and saved as GIF with transparent background ok (NO fill color)?");
        assertEquals(300, read.getWidth());
        assertEquals(300, read.getHeight());

        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.png");
        simapi.write(result, destination, Simapi.TYPE_PNG);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 300x300 pixel and saved as PNG with transparent background ok (NO fill color)?");
        assertEquals(300, read.getWidth());
        assertEquals(300, read.getHeight());

        destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.jpg");
        simapi.write(result, destination, Simapi.TYPE_JPEG);
        read = Simapi.read(destination);
        checkImage(
            new BufferedImage[] {img1, read},
            "Has it been scaled to 300x300 pixel and saved as JPEG with transparent background replaced?");
        assertEquals(300, read.getWidth());
        assertEquals(300, read.getHeight());
    }

    /**
     * Tests the speed of the scaling.<p>
     *
     * @throws Exception if the test fails
     */
    public void testScalingSpeed() throws Exception {

        Simapi simapi = new Simapi();

        long startTime = System.currentTimeMillis();

        BufferedImage img1 = Simapi.read(getClass().getResource("112_org.jpg"));

        long loadTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();

        BufferedImage scaled = null;
        int loops = 100;

        for (int i = 0; i < loops; i++) {
            scaled = simapi.resize(img1, 200, 200, Color.RED, Simapi.POS_CENTER);
        }

        long scaleTime = System.currentTimeMillis() - startTime;

        checkImage(new BufferedImage[] {scaled}, "Load time (msec): "
            + loadTime
            + " / Scale time (msec): "
            + (scaleTime / loops)
            + ". Is this ok?");
    }

    /**
     * Tests an issue with JDK 5 or 6 and GIF image processing.<p>
     *
     *  @throws Exception if the test fails
     */
    public void testScreenShotScaling() throws Exception {

        RenderSettings settings = new RenderSettings(Simapi.RENDER_QUALITY_SOFT);
        Simapi simapi = new Simapi(settings);
        BufferedImage result;

        File input = new File(getClass().getResource("screen_1024.png").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);
        result = simapi.resize(img1, 540, 405, true);
        checkImage(new BufferedImage[] {img1, result}, "Has it been scaled to 540x405 pixel in good quality?");

        input = new File(getClass().getResource("screen_1280.png").getPath());
        imgBytes = readFile(input);
        img1 = Simapi.read(imgBytes);
        result = simapi.resize(img1, 540, 432, true);
        checkImage(new BufferedImage[] {img1, result}, "Has it been scaled to 540x432 pixel in good quality?");
    }

    /**
     * Tests an issue the setup wizard would not show image processing capabilities.<p>
     *
     *  @throws Exception if the test fails
     */
    public void testSetupWizardIssue() throws Exception {

        RenderSettings settings = new RenderSettings(Simapi.RENDER_QUALITY);
        settings.setCompressionQuality(0.85f);
        Simapi simapi = new Simapi(settings);

        ImageIO.scanForPlugins();
        Iterator<ImageReader> pngReaders = ImageIO.getImageReadersByFormatName(Simapi.TYPE_PNG);
        if (!pngReaders.hasNext()) {
            throw (new Exception("No Java ImageIO readers for the PNG format are available."));
        }
        Iterator<ImageWriter> pngWriters = ImageIO.getImageWritersByFormatName(Simapi.TYPE_PNG);
        if (!pngWriters.hasNext()) {
            throw (new Exception("No Java ImageIO writers for the PNG format are available."));
        }

        File input1 = new File(getClass().getResource("screen_1024.png").getPath());
        BufferedImage img1 = Simapi.read(input1);
        BufferedImage img3 = simapi.applyFilter(img1, new RotateFilter(ImageMath.PI));

        File destination = new File(input1.getParentFile(), "test_setup.png");
        simapi.write(img3, destination, Simapi.TYPE_PNG);

        File input2 = new File(getClass().getResource("screen_1024.png").getPath());
        BufferedImage img2 = Simapi.read(input2);

        Arrays.equals(simapi.getBytes(img2, Simapi.TYPE_PNG), simapi.getBytes(img3, Simapi.TYPE_PNG));
    }

    /**
     * Tests "slow scaling" issue encountered with some JPEG's.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSlowScalingIssue() throws Exception {

        Simapi simapi = new Simapi();

        long startTime = System.currentTimeMillis();

        BufferedImage img1 = Simapi.read(getClass().getResource("slow_scale01.jpg"));
        BufferedImage img2 = Simapi.read(getClass().getResource("slow_scale02.jpg"));
        BufferedImage img3 = Simapi.read(getClass().getResource("slow_scale03.jpg"));

        long loadTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();

        img1 = simapi.resize(img1, 100, 100, Color.RED, Simapi.POS_CENTER);
        img2 = simapi.resize(img2, 100, 100, Color.RED, Simapi.POS_CENTER);
        img3 = simapi.resize(img3, 100, 100, Color.RED, Simapi.POS_CENTER);

        long scaleTime = System.currentTimeMillis() - startTime;

        checkImage(new BufferedImage[] {img1, img2, img3}, "Load time (msec): "
            + loadTime
            + " / Scale time (msec): "
            + scaleTime
            + ". Is this ok?");

        File baseDir = new File(getClass().getResource("slow_scale01.jpg").getPath()).getParentFile();

        simapi.write(img1, new File(baseDir, "w_slow_scale01.jpg"), Simapi.TYPE_JPEG);
        simapi.write(img2, new File(baseDir, "w_slow_scale02.jpg"), Simapi.TYPE_JPEG);
        simapi.write(img3, new File(baseDir, "w_slow_scale03.jpg"), Simapi.TYPE_JPEG);

        img1 = Simapi.read(getClass().getResource("w_slow_scale01.jpg"));
        img2 = Simapi.read(getClass().getResource("w_slow_scale02.jpg"));
        img3 = Simapi.read(getClass().getResource("w_slow_scale03.jpg"));

        checkImage(new BufferedImage[] {img1, img2, img3}, "Is default low quality OK for thumbnails?");
    }

    /**
     * Tests an issue with certain scale sizes not working.<p>
     *
     *  @throws Exception if the test fails
     */
    public void testSpecialScaleSize() throws Exception {

        File input = new File(getClass().getResource("meerbild.jpg").getPath());
        RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY);
        // need to reduce default max blur size, otherwise test won't run in eclipse
        rs.setMaximumBlurSize(2200 * 1700);
        Simapi simapi = new Simapi(rs);

        BufferedImage img1 = Simapi.read(input);
        assertEquals(2272, img1.getWidth());
        assertEquals(1704, img1.getHeight()); // must have this exact size to reproduce bug
        // to reproduce we want to see only the upper left part of the image, not the complete image
        img1 = simapi.resize(img1, 690, 219, Simapi.POS_UP_LEFT);
        assertEquals(690, img1.getWidth());
        assertEquals(219, img1.getHeight());
        checkImage(new BufferedImage[] {img1}, "Is the 'special scale size' issue solved?");
    }

    /**
     * Tests writing an image as GIF.<p>
     *
     * @throws Exception if the test fails
     */
    public void testWriteGif() throws Exception {

        Simapi simapi = new Simapi();

        ImageIO.scanForPlugins();
        Iterator<ImageWriter> gifWriters = ImageIO.getImageWritersByFormatName(Simapi.TYPE_GIF);
        if (!gifWriters.hasNext()) {
            fail("No Java ImageIO writers for the GIF format are available.");
        }

        BufferedImage result;
        BufferedImage read;
        File input;
        File destination;

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        BufferedImage img1 = Simapi.read(input);
        result = simapi.resize(img1, 300, 150, true, true);

        destination = new File(input.getParentFile(), "saved3.gif");
        System.out.println(destination.getAbsolutePath());
        simapi.write(result, destination, Simapi.TYPE_GIF);
        read = Simapi.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as GIF in a scaled version?");
    }

    /**
     * Tests writing an image as JPEG and PNG.<p>
     *
     * @throws Exception if the test fails
     */
    public void testWriteJpegAndPng() throws Exception {

        Simapi simapi = new Simapi();

        BufferedImage result;
        BufferedImage read;
        File input;
        File destination;

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        BufferedImage img1 = Simapi.read(input);
        result = simapi.resize(img1, 300, 150, true, true);
        destination = new File(input.getParentFile(), "saved1.jpg");
        System.out.println(destination.getAbsolutePath());
        simapi.write(result, destination, Simapi.TYPE_JPEG);
        read = Simapi.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a scaled version?");

        input = new File(getClass().getResource("screen1.png").getPath());
        BufferedImage img2 = Simapi.read(input);
        result = simapi.scale(img2, 0.8f);
        destination = new File(input.getParentFile(), "saved2.png");
        System.out.println(destination.getAbsolutePath());
        simapi.write(result, destination, Simapi.TYPE_PNG);
        read = Simapi.read(destination);
        checkImage(new BufferedImage[] {img2, read}, "Has it been written to disk as PNG in a scaled version?");
    }

    /**
     * Tests writing an image as JPEG with different quality settings.<p>
     *
     * @throws Exception if the test fails
     */
    public void testWriteJpegQuality() throws Exception {

        BufferedImage read;
        File input;
        File destination;

        RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY);
        Simapi simapi = new Simapi(rs);

        input = new File(getClass().getResource("screen1.png").getPath());
        BufferedImage img1 = Simapi.read(input);

        rs.setCompressionQuality(0.3f);
        destination = new File(input.getParentFile(), "saved_q_low.jpg");
        simapi.write(img1, destination, Simapi.TYPE_JPEG);
        read = Simapi.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _low_ quality version?");

        rs.setCompressionQuality(0.95f);
        destination = new File(input.getParentFile(), "saved_q_high.jpg");
        simapi.write(img1, destination, Simapi.TYPE_JPEG);
        read = Simapi.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _high_ quality version?");
    }

    /**
     * Draws a cross at a specific image point.<p>
     *
     * @param image the image
     * @param x horizontal middle point of the cross
     * @param y vertical middle point of the cross
     *
     * @return the image with the cross drawn at x,y
     */
    protected BufferedImage drawCross(BufferedImage image, int x, int y) {

        Graphics2D g = image.createGraphics();

        g.setColor(Color.RED);
        g.drawLine(x - 5, y, x + 5, y);
        g.drawLine(x, y - 5, x, y + 5);

        return image;
    }
}
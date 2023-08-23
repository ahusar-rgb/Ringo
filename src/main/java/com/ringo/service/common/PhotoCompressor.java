package com.ringo.service.common;

import com.ringo.exception.InternalException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

@Component
public class PhotoCompressor {

    private static final float BLUR_QUALITY = 0.1f;
    private static final int BLUR_RADIUS = 20;

    public byte[] compressImage(byte[] photo, String contentType, float quality) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(photo);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        BufferedImage image = ImageIO.read(inputStream);
        Iterator<ImageWriter> writers =  ImageIO.getImageWritersByFormatName(contentType);
        ImageWriter writer = writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        writer.write(null, new IIOImage(image, null, null), param);

        return outputStream.toByteArray();
    }

    public byte[] createLazyPhoto(byte[] photo, String contentType) {
        try {
            byte[] compressed = compressImage(photo, contentType, BLUR_QUALITY);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(compressed));

            BufferedImage blurred = blurImage(image, BLUR_RADIUS);
            return imageToBytes(blurred, contentType);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while creating lazy photo");
        }
    }

    private BufferedImage blurImage(BufferedImage image, int radius) {
        float[] matrix = new float[radius  * radius];
        Arrays.fill(matrix, 1.0f / matrix.length);

        BufferedImageOp op = new ConvolveOp( new Kernel(radius, radius, matrix), ConvolveOp.EDGE_ZERO_FILL, null);
        return op.filter(image, new BufferedImage(image.getWidth() - radius, image.getHeight() - radius, image.getType()));
    }

    private byte[] imageToBytes(BufferedImage image, String contentType) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, contentType, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new InternalException("Error while converting image to bytes");
        }
    }
}

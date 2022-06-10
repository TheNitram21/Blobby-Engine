package de.arnomann.martin.blobby.core.texture;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture implements ITexture {

    protected int id;
    protected int width;
    protected int height;

    protected String filename;

    protected ByteBuffer pixels;

    public Texture(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();

        int[] pixelsRaw;
        pixelsRaw = image.getRGB(0, 0, width, height, null, 0, width);

        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int pixel = pixelsRaw[i * height + j];
                pixels.put((byte) ((pixel >> 16) & 0xFF));  // R
                pixels.put((byte) ((pixel >> 8) & 0xFF));   // G
                pixels.put((byte) (pixel & 0xFF));          // B
                pixels.put((byte) ((pixel >> 24) & 0xFF));  // A
            }
        }

        pixels.flip();

        id = glGenTextures();
        bind();

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        this.pixels = pixels;
    }

    public Texture(String filename) {
        BufferedImage bi;
        filename = filename + ".png";

        try {
            this.filename = filename;
            bi = ImageIO.read(new File(filename));
            width = bi.getWidth();
            height = bi.getHeight();

            int[] pixelsRaw;
            pixelsRaw = bi.getRGB(0, 0, width, height, null, 0, width);

            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);

            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    int pixel = pixelsRaw[i * height + j];
                    pixels.put((byte) ((pixel >> 16) & 0xFF));  // R
                    pixels.put((byte) ((pixel >> 8) & 0xFF));   // G
                    pixels.put((byte) (pixel & 0xFF));          // B
                    pixels.put((byte) ((pixel >> 24) & 0xFF));  // A
                }
            }

            pixels.flip();

            id = glGenTextures();
            bind();

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            this.pixels = pixels;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public ByteBuffer getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[filename=" + filename + "]";
    }
}

package de.arnomann.martin.blobby.core.texture;

public interface ITexture {
    void bind();
    int getWidth();
    int getHeight();
    String getFilename();
}

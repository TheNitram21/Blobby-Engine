package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.core.texture.ITexture;

import java.util.List;

public class Menu {

    private final List<Button> buttons;
    private final ITexture backgroundTexture;

    public Menu(List<Button> buttons, ITexture backgroundTexture) {
        this.buttons = buttons;
        this.backgroundTexture = backgroundTexture;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public ITexture getBackgroundTexture() {
        return backgroundTexture;
    }

}

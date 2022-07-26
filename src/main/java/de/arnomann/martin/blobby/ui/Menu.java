package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.core.texture.ITexture;

import java.util.List;

/**
 * Represents a UI menu.
 */
public class Menu {

    private final List<Button> buttons;
    private final ITexture backgroundTexture;

    /**
     * Creates a new menu.
     * @param buttons a list of buttons in the menu.
     * @param backgroundTexture the background texture of the menu.
     */
    public Menu(List<Button> buttons, ITexture backgroundTexture) {
        this.buttons = buttons;
        this.backgroundTexture = backgroundTexture;
    }

    /**
     * Returns a list of all buttons.
     * @return the buttons.
     */
    public List<Button> getButtons() {
        return buttons;
    }

    /**
     * Returns the background texture of the menu.
     * @return the background texture.
     */
    public ITexture getBackgroundTexture() {
        return backgroundTexture;
    }

}

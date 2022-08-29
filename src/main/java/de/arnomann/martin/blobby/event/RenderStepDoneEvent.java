package de.arnomann.martin.blobby.event;

/** An event. Called after rendering blocks and the player, but before UI. */
public class RenderStepDoneEvent extends Event {

    public final RenderStep step;

    public RenderStepDoneEvent(RenderStep step) {
        this.step = step;
    }

    public enum RenderStep {
        /** Renderer has rendered the background. */
        RENDER_BACKGROUND,
        /** Renderer has rendered entities that are behind the player. */
        RENDER_ENTITIES_BEHIND_PLAYER,
        /** Renderer has rendered the player. */
        RENDER_PLAYER,
        /** Renderer has rendered entities that are in front of the player. */
        RENDER_ENTITIES_IN_FRONT_OF_PLAYER,
        /** Renderer has rendered the light map. */
        RENDER_LIGHT_MAP,
        /** Renderer has rendered the particles. */
        RENDER_PARTICLES,
        /** Renderer has rendered the queued textures. */
        RENDER_QUEUED_TEXTURES,
        /** Renderer has rendered the menu. */
        RENDER_MENU
    }

}

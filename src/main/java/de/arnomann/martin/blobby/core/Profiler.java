package de.arnomann.martin.blobby.core;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A profiler for profiling the time executing some actions takes.
 */
public class Profiler {

    private final JFrame window;

    private final Map<String, JLabel> labels;

    /**
     * Creates a new profiler.
     */
    public Profiler() {
        window = new JFrame("Blobby Engine Profiler");
        window.setResizable(true);
        window.setSize(240, 240);
        window.getContentPane().setBackground(new Color(40, 44, 52));

        window.getContentPane().setLayout(null);

        labels = new HashMap<>();
        addLabel("FrameTime", "Frame Time");
        addLabel("UpdateTime", "Update Time");
        addLabel("RenderTime", "Render Time");

        window.setVisible(true);
    }

    /**
     * Destroyes the profiler.
     */
    public void destroy() {
        window.setVisible(false);
        window.dispose();
    }

    /**
     * Updates the frame time.
     * @param frameTime the updated frame time.
     */
    public void updateFrameTime(float frameTime) {
        labels.get("FrameTime").setText("<html><nobr>Frame Time: <font color=#5eaed1>" +
                String.format(Locale.US, "%.7f", frameTime) + "s</font> (<font color=#5eaed1>" +
                String.format(Locale.US, "%.2f", 1 / frameTime) + " FPS</font>)</nobr></html>");
    }

    /**
     * Updates the update time.
     * @param updateTime the updated update time.
     */
    public void updateUpdateTime(float updateTime) {
        labels.get("UpdateTime").setText("<html><nobr>Update Time: <font color=#5eaed1>" +
                String.format(Locale.US, "%.7f", updateTime) + "s</font></nobr></html>");
    }

    /**
     * Updates the render time.
     * @param renderTime the updated render time.
     */
    public void updateRenderTime(float renderTime) {
        labels.get("RenderTime").setText("<html><nobr>Render Time: <font color=#5eaed1>" +
                String.format(Locale.US, "%.7f", renderTime) + "s</font></nobr></html>");
    }

    /**
     * Updates a custom action time.
     * @param name the internal name of the action.
     * @param time the (updated) time the action took.
     * @param prefix the visual name of the action.
     */
    public void updateTime(String name, float time, String prefix) {
        JLabel label = labels.get(name);
        if(label == null)
            label = addLabel(name, prefix);
        label.setText("<html><nobr>" + prefix + ": <font color=#5eaed1>" +
                String.format(Locale.US, "%.7f", time) + "s</font></nobr></html>");
    }

    private JLabel addLabel(String name, String prefix) {
        JLabel label;
        window.getContentPane().add(label = new JLabel(prefix + ": NULL"))
                .setBounds(5, labels.size() * 20 + 5, 220, 20);
        label.setForeground(new Color(231, 242, 255));
        labels.put(name, label);
        return label;
    }

}

package io.github.glandais.poc;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        Sim sim = new Sim();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drawing Application");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            DrawingCanvas canvas = new DrawingCanvas(screenSize.width, screenSize.height, sim);
            frame.add(canvas);
            frame.setSize(screenSize.width, screenSize.height);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Start the drawing loop
            canvas.start();
        });

//        sim.run(3600.0, 0.1);
    }

}

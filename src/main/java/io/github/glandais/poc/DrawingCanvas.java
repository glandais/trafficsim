package io.github.glandais.poc;

import io.github.glandais.poc.lane.CircleLane;
import io.github.glandais.poc.lane.Lane;
import io.github.glandais.poc.lane.StraigthLane;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class DrawingCanvas extends JPanel implements Runnable {
    private final Sim sim;
    private boolean running = false;
    private final BufferedImage bufferedImage;
    private final Graphics2D bufferedGraphics;

    public DrawingCanvas(int width, int height, Sim sim) {
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.bufferedGraphics = bufferedImage.createGraphics();
        this.bufferedGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.sim = sim;
    }

    public void start() {
        running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        long done = 0;
        long start = System.currentTimeMillis();
        long previousSeconds = 0;

        while (running) {
            long now = System.nanoTime();

            sim.update(20 * (now - lastTime) / 1000000000.0);
            lastTime = now;

            done++;

            long seconds = (System.currentTimeMillis() - start) / 1000;
            if (seconds > previousSeconds) {
                System.out.println(done);
                done = 0;
                previousSeconds = seconds;
            }

            repaint();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(bufferedGraphics);
        g.drawImage(bufferedImage, 0, 0, null);
    }

    private void doDrawing(Graphics2D g2d) {
        g2d.clearRect(0, 0, getWidth(), getHeight());
        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.GREEN);
        List<Lane> lanes = sim.getLanes();
        for (Lane lane : lanes) {
            if (lane instanceof StraigthLane straigthLane) {
                g2d.drawLine(
                        (int) straigthLane.getFrom().x(),
                        getHeight() - (int) straigthLane.getFrom().y(),
                        (int) straigthLane.getTo().x(),
                        getHeight() - (int) straigthLane.getTo().y()
                );
            } else {
                int c = 1 + (int) (lane.getLength() / 10.0);
                double da = lane.getLength() / c;
                for (int i = 0; i < c; i++) {
                    double sa = da * i;
                    double ea = da * (i + 1);
                    Point sp = lane.getCoords(sa);
                    Point ep = lane.getCoords(ea);
                    g2d.drawLine(
                            (int) sp.x(),
                            getHeight() - (int) sp.y(),
                            (int) ep.x(),
                            getHeight() - (int) ep.y()
                    );
                }
            }
        }
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        List<CarPos> carPositions = sim.getCarPositions();
        for (CarPos carPosition : carPositions) {
            g2d.setColor(getColorFromSpeed(carPosition.speed()));
            int x = ((int) (carPosition.pos().x())) - 4;
            int y = (getHeight() - (int) (carPosition.pos().y())) - 4;
            g2d.fillOval(x, y, 8, 8);
        }
    }

    private Color getColorFromSpeed(double speed) {
        double normalizedSpeed = Math.min(speed / 100, 1.0);
        float hue = (float) (240 * (1 - normalizedSpeed)) / 360;
        float saturation = 1.0f; // Full saturation
        float brightness = 1.0f; // Full brightness

        return Color.getHSBColor(hue, saturation, brightness);
    }
}
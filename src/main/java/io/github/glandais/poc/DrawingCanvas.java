package io.github.glandais.poc;

import io.github.glandais.poc.lane.Lane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class DrawingCanvas extends JPanel implements Runnable {
    private final Sim sim;
    private boolean running = false;
    private final BufferedImage bufferedImage;
    private final Graphics2D bufferedGraphics;

    double scale = 0.5;
    int dx = 0;
    int dy = 0;

    int startX = 0;
    int startY = 0;

    public DrawingCanvas(int width, int height, Sim sim) {
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.bufferedGraphics = bufferedImage.createGraphics();
        this.bufferedGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.sim = sim;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dx = dx + (e.getX() - startX);
                dy = dy + (e.getY() - startY);
                startX = e.getX();
                startY = e.getY();
            }
        });
        addMouseWheelListener(new MouseAdapter() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();
                scale += delta;
                scale = Math.max(scale, 0.05);
                scale = Math.min(scale, 20.0);
                revalidate();
                repaint();
            }

        });
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
            List<Point> points = lane.getPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                g2d.drawLine(
                        x(points.get(i).x()),
                        y(points.get(i).y()),
                        x(points.get(i + 1).x()),
                        y(points.get(i + 1).y())
                );
            }
        }
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        List<CarPos> carPositions = sim.getCarPositions();
        for (CarPos carPosition : carPositions) {
            g2d.setColor(getColorFromSpeed(carPosition.speed()));
            int x = x(carPosition.pos().x()) - 4;
            int y = y(carPosition.pos().y()) - 4;
            g2d.fillOval(x, y, 8, 8);
        }
    }

    private int x(double v) {
        return (int) (dx + v * scale);
    }

    private int y(double v) {
        return dy + getHeight() - (int) (v * scale);
    }

    private Color getColorFromSpeed(double speed) {
        double normalizedSpeed = Math.min(speed / 100, 1.0);
        float hue = (float) (240 * (1 - normalizedSpeed)) / 360;
        float saturation = 1.0f; // Full saturation
        float brightness = 1.0f; // Full brightness

        return Color.getHSBColor(hue, saturation, brightness);
    }
}
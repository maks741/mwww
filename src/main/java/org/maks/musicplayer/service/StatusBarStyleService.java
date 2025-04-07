package org.maks.musicplayer.service;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class StatusBarStyleService {

    public String generateGradientBackground(Image thumbnail) {
        Color color = extractPrioritizedColor(thumbnail);
        Color littleBrighter = color.brighter();
        Color littleDarker = color.darker();
        Color darker = littleDarker.darker();

        return """
                -fx-background-color: linear-gradient(to right,
                        black,
                        %s,
                        %s,
                        %s,
                        %s
                );
                """.formatted(toRgb(darker), toRgb(littleDarker), toRgb(color), toRgb(littleBrighter));
    }

    private Color extractPrioritizedColor(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();

        Map<Color, Integer> colorPriorityMap = new HashMap<>();

        int sampleStep = 5;

        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                Color color = pixelReader.getColor(x, y);

                if(unwantedColor(color)) {
                    continue;
                }

                colorPriorityMap.put(color, colorPriorityMap.getOrDefault(color, 0) + 1);
            }
        }

        int maxPriority = Integer.MIN_VALUE;
        Color color = Color.BLACK;
        for (Map.Entry<Color, Integer> entry : colorPriorityMap.entrySet()) {
            if (entry.getValue() > maxPriority) {
                maxPriority = entry.getValue();
                color = entry.getKey();
            }
        }

        return color;
    }

    private boolean unwantedColor(Color color) {
        Color[] unwantedColors = {Color.BLACK, Color.WHITE, Color.YELLOW, Color.RED};
        double similarityDelta = 0.4;

        for (Color unwantedColor : unwantedColors) {
            boolean similarRed = Math.abs(unwantedColor.getRed() - color.getRed()) < similarityDelta;
            boolean similarGreen = Math.abs(unwantedColor.getGreen() - color.getGreen()) < similarityDelta;
            boolean similarBlue = Math.abs(unwantedColor.getBlue() - color.getBlue()) < similarityDelta;

            if (similarRed && similarGreen && similarBlue) {
                return true;
            }
        }

        return false;
    }

    private String toRgb(Color color) {
        return "rgb(%s, %s, %s)".formatted(color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255);
    }
}

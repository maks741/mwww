package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageUtils {

    public static Image cropToSquare(Image image) {
        int imageWidth = (int) Math.floor(image.getWidth());
        int imageHeight = (int) Math.floor(image.getHeight());
        int minDimension = Math.min(imageWidth, imageHeight);

        WritableImage writableImage = new WritableImage(minDimension, minDimension);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = image.getPixelReader();

        if (imageWidth == imageHeight) {
            return image;
        } else if (imageWidth > imageHeight) {
            int offsetX = imageWidth - imageHeight;

            for (int x = offsetX; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    int argb = pixelReader.getArgb(x - offsetX/2, y);
                    pixelWriter.setArgb(x - offsetX, y, argb);
                }
            }
        } else {
            int offsetY = imageHeight - imageWidth;

            for (int x = 0; x < imageWidth; x++) {
                for (int y = offsetY; y < imageHeight; y++) {
                    int argb = pixelReader.getArgb(x, y - offsetY/2);
                    pixelWriter.setArgb(x, y - offsetY, argb);
                }
            }
        }

        return writableImage;
    }

}

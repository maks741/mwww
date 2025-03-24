package org.maks.musicplayer.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public class ImageUtils {

    public static Image cropToSquare(Image image, ImageView target) {
        image = resizeImage(image, target);

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

    public static Image resizeImage(Image image, ImageView target) {
        // without the downscale factor images result in being too blurry
        // however if we make Scalr downscale them to imageDownscaleFactor times the
        // actual target size then they do not become blurry yet JavaFX is displaying
        // them much better than without resizing
        double imageDownscaleFactor = 3;
        int targetWidth = (int) (imageDownscaleFactor * target.getFitWidth());
        int targetHeight = (int) (imageDownscaleFactor * target.getFitHeight());

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        BufferedImage resized = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, targetWidth, targetHeight);
        return SwingFXUtils.toFXImage(resized, null);
    }

}

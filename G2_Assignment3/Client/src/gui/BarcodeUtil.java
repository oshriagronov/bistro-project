package gui;

import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Utility class for generating QR codes using the ZXing library.
 */
public class BarcodeUtil {

    /**
     * Generates a QR code image from the specified text.
     *
     * @param text the content to encode in the QR code
     * @param size the width and height of the generated image in pixels
     * @return a JavaFX {@link Image} containing the QR code
     * @throws RuntimeException if the QR code generation fails
     */
    public static Image createQr(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix =
                    writer.encode(text, BarcodeFormat.QR_CODE, size, size);

            BufferedImage buffered =
                    MatrixToImageWriter.toBufferedImage(matrix);

            return SwingFXUtils.toFXImage(buffered, null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR", e);
        }
    }
}
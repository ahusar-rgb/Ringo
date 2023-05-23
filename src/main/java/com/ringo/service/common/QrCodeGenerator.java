package com.ringo.service.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ringo.exception.InternalException;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class QrCodeGenerator {

    public BufferedImage generateQrCode(String ticketCode) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(ticketCode,
                    BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new InternalException("Error while generating QR code");
        }
    }
}

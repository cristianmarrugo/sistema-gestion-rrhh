package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRService {

    /**
     * Genera un código QR que contiene el PIN del empleado
     *
     * @param pin PIN del empleado (6 dígitos)
     * @return Imagen PNG del QR en bytes
     */
    public byte[] generarQR(String pin) throws WriterException, IOException {
        // Configuración del QR
        int width = 300;
        int height = 300;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                pin,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        // Convertir a imagen PNG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}

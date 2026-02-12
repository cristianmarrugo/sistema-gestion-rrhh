package com.example.demo.controller;

import com.example.demo.model.Asistencia;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.QRService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRController {

    private final QRService qrService;
    private final AsistenciaService asistenciaService;

    /**
     * Generar QR para un empleado
     * GET /api/qr/generar?pin=1234
     * Retorna imagen PNG del QR
     */
    @GetMapping(value = "/generar", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generarQR(@RequestParam String pin) {
        try {
            byte[] qrImage = qrService.generarQR(pin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "qr-" + pin + ".png");

            return new ResponseEntity<>(qrImage, headers, HttpStatus.OK);

        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Escanear QR y marcar asistencia
     * POST /api/qr/escanear
     * Body: {"qrData": "1234"}
     */
    @PostMapping("/escanear")
    public ResponseEntity<Asistencia> escanearQR(@RequestBody QRScanRequest request) {
        try {
            // El QR contiene el PIN del empleado
            String pin = request.getQrData();

            Asistencia asistencia = asistenciaService.marcarAsistencia(pin);

            return ResponseEntity.ok(asistencia);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DTO para recibir el escaneo
    public static class QRScanRequest {
        private String qrData;

        public String getQrData() {
            return qrData;
        }

        public void setQrData(String qrData) {
            this.qrData = qrData;
        }
    }
}

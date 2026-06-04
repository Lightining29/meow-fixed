package com.login.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    public String generateQRCodeBase64(String upiId, String payeeName, double amount, String orderId) throws WriterException, IOException {
        // UPI Payment URI Format (works with all UPI apps: Google Pay, PhonePe, Paytm etc)
        String upiUri = String.format("upi://pay?pa=%s&pn=%s&am=%.2f&tid=%s&tr=%s&tn=Order%%20Payment",
                upiId,
                payeeName.replace(" ", "%20"),
                amount,
                orderId,
                orderId
        );

        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(upiUri, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        byte[] qrImageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(qrImageBytes);
    }

    public String generatePaymentQR(String orderReference, double amount) throws WriterException, IOException {
        return generateQRCodeBase64("9910954720@ybl", "My Store", amount, orderReference);
    }
}
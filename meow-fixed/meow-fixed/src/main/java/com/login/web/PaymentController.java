package com.login.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
public class PaymentController {

    @Autowired
    private QRService qrService;

    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQR(@RequestParam double amount) throws Exception {
        return qrService.generateQR(amount);
    }
}
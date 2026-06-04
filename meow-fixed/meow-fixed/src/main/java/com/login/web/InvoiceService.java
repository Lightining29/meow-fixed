package com.login.web;

import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
@Service
public class InvoiceService {

    public void generate(Order order) throws Exception {

        PdfWriter writer = new PdfWriter("invoice_" + order.getId() + ".pdf");
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.add(new Paragraph("Invoice"));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Amount: ₹" + order.getTotalAmount()));
        document.close();
    }
}

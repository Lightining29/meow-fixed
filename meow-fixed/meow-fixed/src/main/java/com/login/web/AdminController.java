package com.login.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminController {

    @Autowired private PaymentRepository paymentRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private Bookingrepo bookingRepo;
    @Autowired private CartRepository cartRepo;
    @Autowired private MyRepository userRepo;

    @GetMapping("/admin")
    public String admin(Model model) {
        List<Payment> allPayments = (List<Payment>) paymentRepo.findAll();

        long pending  = allPayments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        long approved = allPayments.stream().filter(p -> "APPROVED".equals(p.getStatus())).count();
        long rejected = allPayments.stream().filter(p -> "REJECTED".equals(p.getStatus())).count();
        double totalRevenue = allPayments.stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount).sum();

        model.addAttribute("payments", allPayments);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("rejectedCount", rejected);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("allBookings", (List<Booking>) bookingRepo.findAll());
        return "admin";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Long id) {
        Payment payment = paymentRepo.findById(id).orElseThrow();
        payment.setStatus("APPROVED");
        paymentRepo.save(payment);

        Order order = orderRepo.findById(payment.getOrderId()).orElseThrow();
        order.setStatus("SUCCESS");
        orderRepo.save(order);

        Optional<Login> userOpt = userRepo.findById(order.getUserId().intValue());
        if (userOpt.isPresent()) {
            Login user = userOpt.get();
            List<Cart> cartItems = cartRepo.findByLogin(user);

            boolean alreadyBooked = ((List<Booking>) bookingRepo.findByLogin(user))
                    .stream().anyMatch(bk ->
                        Math.abs(bk.getTotalAmount() - order.getTotalAmount()) < 0.01
                        && "UPI".equals(bk.getPaymentMethod()));

            if (!alreadyBooked) {
                Booking booking = new Booking();
                booking.setLogin(user);
                booking.setPaymentMethod("UPI");
                booking.setAddress(user.getAddress());
                booking.setTotalAmount(order.getTotalAmount());

                List<Product> products = new ArrayList<>();
                for (Cart cart : cartItems) {
                    if (cart.getProduct() != null) products.add(cart.getProduct());
                }
                booking.setProduct(products);
                bookingRepo.save(booking);
                cartRepo.deleteAll(cartItems);
            }
        }
        return "redirect:/admin";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long id) {
        Payment payment = paymentRepo.findById(id).orElseThrow();
        payment.setStatus("REJECTED");
        paymentRepo.save(payment);
        orderRepo.findById(payment.getOrderId()).ifPresent(order -> {
            order.setStatus("FAILED");
            orderRepo.save(order);
        });
        return "redirect:/admin";
    }

    @GetMapping("/admin/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Payment> all = (List<Payment>) paymentRepo.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending",  all.stream().filter(p -> "PENDING".equals(p.getStatus())).count());
        stats.put("approved", all.stream().filter(p -> "APPROVED".equals(p.getStatus())).count());
        stats.put("rejected", all.stream().filter(p -> "REJECTED".equals(p.getStatus())).count());
        stats.put("revenue",  all.stream().filter(p -> "APPROVED".equals(p.getStatus()))
                                          .mapToDouble(Payment::getAmount).sum());
        return ResponseEntity.ok(stats);
    }
}

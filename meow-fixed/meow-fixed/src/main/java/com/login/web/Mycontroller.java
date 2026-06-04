package com.login.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

/*
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                CONTROLLER CLASS                ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
*/

@Controller
public class Mycontroller {
    @Autowired
    public MyRepository a;
    @Autowired
    public Productrepository p;
    @Autowired
    public Categoryrepository c;
    @Autowired
    public CartRepository ca;
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private PaymentRepository paymentRepo;
    @Autowired
    public EmailServiceImpl emailServiceImpl;

    @Autowired
    public Bookingrepo b;

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private QRCodeService qrCodeService;


  @PostMapping("/payment")
    public String createOrder(@RequestParam double amount,
                              @RequestParam Long loginId,
                              Model model) throws Exception {

        Order order = new Order();
        order.setUserId(loginId);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");

        orderRepo.save(order);

        String qrCodeBase64 = qrCodeService.generatePaymentQR(order.getId().toString(), amount);

        model.addAttribute("amount", amount);
        model.addAttribute("orderId", order.getId());
        model.addAttribute("qrCodeBase64", qrCodeBase64);

        return "payment"; // QR page
    }
    @PostMapping("/payment/verify")
    public String verify(@RequestParam String utr,
                         @RequestParam double amount,
                         @RequestParam Long orderId,
                         HttpSession session,
                         Model model) {

        // 1. Save payment record
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setUtr(utr);
        payment.setStatus("PENDING");
        paymentRepo.save(payment);

        // 2. Save Booking — this was missing, causing no data in booking table
        String email = (String) session.getAttribute("email");
        if (email != null) {
            Optional<Login> userOpt = a.findByemail(email);
            if (userOpt.isPresent()) {
                Login user = userOpt.get();
                List<Cart> cartItems = ca.findByLogin(user);

                if (!cartItems.isEmpty()) {
                    Booking booking = new Booking();
                    booking.setLogin(user);
                    booking.setPaymentMethod("UPI");
                    booking.setAddress(user.getAddress());
                    booking.setTotalAmount(amount);

                    List<Product> productList = new ArrayList<>();
                    for (Cart cart : cartItems) {
                        if (cart.getProduct() != null) {
                            productList.add(cart.getProduct());
                        }
                    }
                    booking.setProduct(productList);
                    b.save(booking);  // Save booking to DB

                    // 3. Mark order as PAID
                    orderRepo.findById(orderId).ifPresent(order -> {
                        order.setStatus("PAID");
                        orderRepo.save(order);
                    });

                    // 4. Clear cart after successful booking
                    ca.deleteAll(cartItems);
                }
            }
        }

        model.addAttribute("message", "Payment submitted! Your booking is confirmed.");
        return "success";
    }




    @GetMapping("/search")
    public String searchProducts(@RequestParam("q") String keyword, Model model, HttpSession session) {

        List<Product> results = p.findByFoodContainingIgnoreCase(keyword);

        model.addAttribute("results", results);
        model.addAttribute("query", keyword);

        return "list"; // Thymeleaf template to show results
    }

    @PostMapping("/lg")
    public String loginproduct(
            Model model,
            HttpSession session) {
        String email = (String) session.getAttribute("email");
        Optional<Login> userOpt = a.findByemail(email);
        Login user = userOpt.get();

        // Store session info
        session.setAttribute("email", user.getEmail());
        session.setAttribute("userId", user.getId());

        // Fetch all bookings made by this user
        List<Booking> bookings = b.findByLogin(user);

        // Add to model for Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);

        return "profile"; // profile.html will show booking history
    }

    @PostMapping("/placeorder")
    public String placeOrder(
            @RequestParam String payment,
            HttpSession session,
            Model model) {
        // Step 1: Fetch the user
        String email = (String) session.getAttribute("email");
        Optional<Login> userOpt = a.findByemail(email);
        Login user = userOpt.get();

        // Step 2: Fetch all cart items of this user
        List<Cart> cartItems = ca.findByLogin(user);

        // Step 3: Create new Booking
        Booking booking = new Booking();
        booking.setLogin(user);
        booking.setPaymentMethod(payment);
        booking.setAddress(user.getAddress());

        double total = 0;
        List<Product> productList = new ArrayList<>();

        // Step 4: Calculate total and collect products
        for (Cart cart : cartItems) {
            Product product = cart.getProduct();
            if (product != null) {
                total += product.getPrice() * cart.getQuantity();
                productList.add(product);
            }
        }

        booking.setTotalAmount(total);
        booking.setProduct(productList);

        // Step 5: Save booking
        b.save(booking);

        // Step 6: Clear the user's cart after successful order
        ca.deleteAll(cartItems);

        // Step 7: Pass data to success page
        model.addAttribute("booking", booking);
        return "index"; // Create this Thymeleaf page
    }

    @GetMapping("/car")
    public String booking(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null)
            return "index";

        Optional<Login> userOpt = a.findByemail(email);
        if (userOpt.isEmpty())
            return "index";

        Login user = userOpt.get();
        List<Cart> cartItems = ca.findByLogin(user);
        double total = 0;
        for (Cart item : cartItems) {
            total += item.getProduct().getPrice(); // or item.getProduct().getPrice() * item.getQuantity()
        }

        model.addAttribute("total", total);
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        return "booking";
    }

    @PostMapping("/r/{id}")
    public String d(@PathVariable int id) {
        ca.deleteById(id);
        return "redirect:/index";
    }

    @GetMapping("/filter/price/{price}")
    public String filterProducts(@PathVariable int price, Model model) {
        List<Product> products = p.findByPriceLessThanEqual(price);
        model.addAttribute("pro", products);
        return "index"; // reuse same page
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null)
            return "index";

        Optional<Login> userOpt = a.findByemail(email);
        if (userOpt.isEmpty())
            return "index";

        Login user = userOpt.get();
        List<Cart> cartItems = ca.findByLogin(user);
        double total = 0;
        for (Cart item : cartItems) {
            total += item.getProduct().getPrice(); // or item.getProduct().getPrice() * item.getQuantity()
        }

        model.addAttribute("totalPrice", total);

        model.addAttribute("cartitems", cartItems);
        return "cartview";
    }

    @GetMapping("/foodform")
    public String showFoodForm(Model model) {
        // Fetch all categories
        List<Cat> categories = (List<Cat>) c.findAll();

        // Add to the model so Thymeleaf can access it
        model.addAttribute("categories", categories);

        // Return the form page (must match your .html file name)
        return "food"; // this should be food.html in templates/
    }

    @PostMapping("/category")
    public String category(@RequestParam String category) {
        Cat d = new Cat();
        d.setCategory(category);
        c.save(d);
        return "index";
    }

    @PostMapping("/food")
    public String food(@RequestParam String food, @RequestParam int cat, @RequestParam("image") MultipartFile file,
            @RequestParam int price, @RequestParam String description) throws IOException {
        Cat category = c.findById(cat).orElseThrow();
        Product e = new Product();
        e.setFood(food);
        e.setCat(category);
        e.setDescription(description);
        e.setPrice(price);
        e.setImage(file.getBytes());
        p.save(e);
        return "index";
    }
    /*
     * ╔════════════════════════════════════════════════╗
     * ║ CREATE USER ║
     * ╚════════════════════════════════════════════════╝
     */

    @PostMapping("save")
    public String reg(
            @RequestParam String fname,
            @RequestParam String lname,
            @RequestParam String phno,
            @RequestParam String email,
            @RequestParam String adress,
            @RequestParam String pass,
            @RequestParam("image") MultipartFile file,
            HttpSession session, Model m) throws IOException {

        // 1. Create user object but don't save it yet
        Login user = new Login();
        user.setFname(fname);
        user.setLname(lname);
        user.setEmail(email);
        user.setAddress(adress);
        user.setPassword(pass);
        user.setPhoneno(phno);
        user.setImage(file.getBytes());

        // store user temporarily in session
        session.setAttribute("tempUser", user);
        m.addAttribute("user", user);

        // 2. Generate OTP (4 or 6 digit)
        int otp = (int) (Math.random() * 9000) + 1000;

        // store OTP in session
        session.setAttribute("otp", otp);

        // 3. Send OTP email
        emailServiceImpl.sendEmail(
                email,
                "Your OTP Verification Code",
                "Your OTP is: " + otp);

        // 4. Redirect to OTP page
        return "verify";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam("d1") int d1,
            @RequestParam("d2") int d2,
            @RequestParam("d3") int d3,
            @RequestParam("d4") int d4,
            HttpSession session) {

        String otpStr = "" + d1 + d2 + d3 + d4;
        int otp = Integer.parseInt(otpStr);

        Integer originalOtp = (Integer) session.getAttribute("otp");
        Login user = (Login) session.getAttribute("tempUser");

        if (originalOtp == null || user == null) {
            return "error"; // session expired or direct access
        }

        // OTP matches → save user in DB
        if (otp == originalOtp) {
            a.save(user); // ← **THIS was missing**

            // cleanup
            session.removeAttribute("otp");
            session.removeAttribute("tempUser");

            return "index"; // registration success
        }

        return "wrongotp"; // wrong otp page
    }

    /*
     * ╭────────────────────────────────────────────────╮
     * │ RETURN TO HOME PAGE │
     * ╰────────────────────────────────────────────────╯
     */

    @GetMapping("/")
    public String home(Model model) {
        List<Cat> categories = (List<Cat>) c.findAll(); // each contains its products
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("/index")
    public String hom(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null)
            return "index";

        Optional<Login> userOpt = a.findByemail(email);
        if (userOpt.isEmpty())
            return "index";

        Login user = userOpt.get();
        // Store session info
        session.setAttribute("email", user.getEmail());
        session.setAttribute("userid", user.getId());

        // Fetch all bookings made by this user
        List<Booking> bookings = b.findByLogin(user);

        // Add to model for Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);

        List<Cat> categories = (List<Cat>) c.findAll(); // each contains its products
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("/ca")
    public String cat() {
        return "cat";
    }

    // sorting//
    @GetMapping("/foodlis/{id}")
    public String showProductsByCategor(@PathVariable int id, Model model) {
        Cat category = c.findById(id).orElseThrow();
        List<Product> products = p.findByCat(category);
        List<Cat> categories = (List<Cat>) c.findAll();
        model.addAttribute("products", products);

        return "index"; // same template, filtered list
    }

    @GetMapping("/foodlist/{id}")
    public String showProductsByCategory(@PathVariable int id, Model model) {
        Cat category = c.findById(id).orElseThrow();
        List<Product> products = p.findByCat(category);
        List<Cat> categories = (List<Cat>) c.findAll();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        return "category"; // same template, filtered list
    }

    @GetMapping("/extra/{id}")
    public String showProduct(@PathVariable int id, Model model) {
        Optional<Product> extra = p.findById(id);

        if (extra.isPresent()) {
            model.addAttribute("extra", extra.get());
        } else {
            model.addAttribute("error", "Product not found");
            return "error"; // optional: show a custom error page
        }

        return "extra";
    }

    @GetMapping("/cate")
    public String showCategories(Model model) {
        List<Cat> categories = (List<Cat>) c.findAll();
        model.addAttribute("categories", categories);
        return "category"; // categories.html
    }

    @GetMapping("/food")
    public String food(Model model) {
        List<Cat> categories = (List<Cat>) c.findAll();

        // Add to the model so Thymeleaf can access it
        model.addAttribute("categories", categories);
        return "food";
    }

    @GetMapping("/img/{id}")
    public ResponseEntity<byte[]> getfood(@PathVariable int id) {
        Optional<Product> userOpt = p.findById(id);
        if (userOpt.isPresent() && userOpt.get().getImage() != null) {
            return ResponseEntity.ok()
                    .body(userOpt.get().getImage());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }

    @PostMapping("/add/{productId}")
public String addToCart(@PathVariable int productId, HttpSession session) {

    String userId = (String) session.getAttribute("email");

    Login user = a.findByemail(userId).orElseThrow();
    Product product = p.findById(productId).orElseThrow();

    Cart cart = new Cart();
    cart.setLogin(user);
    cart.setProduct(product);

    int quantity = 1;
    cart.setQuantity(quantity);

    double price = product.getPrice();
    double finalPrice = price;

    // 🔥 APPLY DISCOUNT
    if (product.getDiscount() != null) {
        double percent = product.getDiscount().getPercentage();
        double discountAmount = (price * percent) / 100;
        finalPrice = price - discountAmount;
    }

    double totalPrice = finalPrice * quantity;

    cart.setPrice(price);
    cart.setFinalPrice(finalPrice);
    cart.setTotalPrice(totalPrice);

    ca.save(cart);

    return "redirect:/index";
}
    /*
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ LOGIN USER ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     */

    @PostMapping("/kholo")
    public String login(@RequestParam String fname,
            @RequestParam String pass,
            Model model,
            HttpSession session) {

        Optional<Login> user = a.findByFnameAndPassword(fname, pass);
        if (user.isPresent()) {
            Login loggedInUser = user.get();

            // Store session info
            session.setAttribute("email", loggedInUser.getEmail());
            session.setAttribute("userId", loggedInUser.getId());

            // Fetch all bookings made by this user
            List<Booking> bookings = b.findByLogin(loggedInUser);

            // Add to model for Thymeleaf
            model.addAttribute("user", loggedInUser);
            model.addAttribute("bookings", bookings);

            return "profile"; // profile.html will show booking history
        } else {
            return "invalid"; // invalid.html shows login error
        }
    }

    @GetMapping("/ab")
    public String gString(HttpSession session, Model m) {
        String email = (String) session.getAttribute("useremail");
        Optional<Login> userOpt = a.findByemail(email);
        Login user = userOpt.get();
        // Store session info
        session.setAttribute("email", user.getEmail());
        session.setAttribute("userId", user.getId());

        // Fetch all bookings made by this user
        List<Booking> bookings = b.findByLogin(user);

        // Add to model for Thymeleaf
        m.addAttribute("user", user);
        m.addAttribute("bookings", bookings);
        return "profile";
    }

    @GetMapping("/prof")
    public String getMethodNa(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");

        Optional<Login> userOpt = a.findByemail(email);

        Login user = userOpt.get();
        // Store session info
        session.setAttribute("email", user.getEmail());
        session.setAttribute("userId", user.getId());

        // Fetch all bookings made by this user
        List<Booking> bookings = b.findByLogin(user);

        // Add to model for Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);

        return "profile";
    }

    /*
     * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃ DISPLAY USER IMAGE ┃
     * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
     */

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        Optional<Login> userOpt = a.findById(id);
        if (userOpt.isPresent() && userOpt.get().getImage() != null) {
            return ResponseEntity.ok()
                    .body(userOpt.get().getImage());
        }
        return ResponseEntity.notFound().build();
    }

    /*
     * ��━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━��
     * �� DELETE USER ��
     * ��━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━��
     */

    @PostMapping("/del/{id}")
    public String del(@PathVariable int id) {
        a.deleteById(id);
        return "redirect:/index";
    }

    private ChatClient chatClient; // ✅ Make it final for immutability

    // ✅ Spring will auto-inject the builder
    public Mycontroller(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /*
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 🤖 AI RECOMMENDATIONS ENDPOINTS
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */

    @PostMapping("/ai/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getAIRecommendations(@RequestBody String preference) {
        List<Product> allProducts = (List<Product>) p.findAll();
        List<String> recommendations = aiRecommendationService.getRecommendations(preference, allProducts);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/ai/search-suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String query) {
        List<Product> allProducts = (List<Product>) p.findAll();
        List<String> suggestions = aiRecommendationService.getSearchSuggestions(query, allProducts);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/ai/meal-combo/{productId}")
    @ResponseBody
    public ResponseEntity<String> getMealCombo(@PathVariable int productId) {
        Optional<Product> productOpt = p.findById(productId);
        if (productOpt.isPresent()) {
            String combo = aiRecommendationService.suggestMealCombo(productOpt.get().getFood());
            return ResponseEntity.ok(combo);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ai/description/{productId}")
    @ResponseBody
    public ResponseEntity<String> getAIDescription(@PathVariable int productId) {
        Optional<Product> productOpt = p.findById(productId);
        if (productOpt.isPresent()) {
            String description = aiRecommendationService.generateFoodDescription(productOpt.get().getFood());
            return ResponseEntity.ok(description);
        }
        return ResponseEntity.notFound().build();
    }

    /*
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 💰 DISCOUNT ENDPOINTS
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */

    @PostMapping("/discount/apply")
    @ResponseBody
    public ResponseEntity<DiscountService.DiscountResult> applyDiscount(
            @RequestParam String code, 
            @RequestParam int amount) {
        DiscountService.DiscountResult result = discountService.applyDiscount(code, amount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/discount/active")
    @ResponseBody
    public ResponseEntity<List<Discount>> getActiveDiscounts() {
        List<Discount> discounts = discountService.getActiveDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @PostMapping("/chat-ajax")
    @ResponseBody // 👈 CRITICAL: Tells Spring to serialize the return object (messages list) to
                  // JSON
    public ResponseEntity<List<String>> chatAjax(@RequestBody String query, HttpSession session) {
        // 👆 CRITICAL: Use @RequestBody to read the raw text from the JavaScript
        // fetch() body

        List<String> messages = (List<String>) session.getAttribute("messages");
        if (messages == null) {
            messages = new ArrayList<>();
        }

        // ✅ Handle null or empty queries safely
        if (query == null || query.isBlank()) {
            messages.add("System: Query is empty. Please enter a message.");
            session.setAttribute("messages", messages);
            // Returning 400 Bad Request is good practice for empty input
            return ResponseEntity.badRequest().body(messages);
        }

        String res;
        try {
            // ✅ Interact with model
            res = this.chatClient.prompt(query).call().content();
        } catch (Exception e) {
            res = "Error while fetching response: " + e.getMessage();
        }

        // Add user query and bot response
        messages.add("You: " + query);
        messages.add("Bot: " + res);

        // Save back to session
        session.setAttribute("messages", messages);

        // 🏆 CRITICAL: Return the list of messages as a JSON body with a 200 OK status
        return ResponseEntity.ok(messages);
    }
}
package com.login.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class UserModelListener extends AbstractMongoEventListener<Object> {

    private final SequenceGeneratorService sequenceGenerator;

    @Autowired
    public UserModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof Cat) {
            Cat cat = (Cat) source;
            if (cat.getId() < 1) {
                cat.setId(sequenceGenerator.generateSequence(Cat.SEQUENCE_NAME));
            }
        } else if (source instanceof Product) {
            Product product = (Product) source;
            if (product.getId() < 1) {
                product.setId(sequenceGenerator.generateSequence(Product.SEQUENCE_NAME));
            }
        } else if (source instanceof Booking) {
            Booking booking = (Booking) source;
            if (booking.getId() < 1) {
                booking.setId(sequenceGenerator.generateSequence(Booking.SEQUENCE_NAME));
            }
        } else if (source instanceof Login) {
            Login login = (Login) source;
            if (login.getId() < 1) {
                login.setId(sequenceGenerator.generateSequence(Login.SEQUENCE_NAME));
            }
        } else if (source instanceof Order) {
            Order order = (Order) source;
            if (order.getId() == null || order.getId() < 1) {
                order.setId((long) sequenceGenerator.generateSequence(Order.SEQUENCE_NAME));
            }
        } else if (source instanceof Payment) {
            Payment payment = (Payment) source;
            if (payment.getId() == null || payment.getId() < 1) {
                payment.setId((long) sequenceGenerator.generateSequence(Payment.SEQUENCE_NAME));
            }
        } else if (source instanceof Discount) {
            Discount discount = (Discount) source;
            if (discount.getId() < 1) {
                discount.setId(sequenceGenerator.generateSequence(Discount.SEQUENCE_NAME));
            }
        } else if (source instanceof Cart) {
            Cart cart = (Cart) source;
            if (cart.getId() < 1) {
                cart.setId(sequenceGenerator.generateSequence(Cart.SEQUENCE_NAME));
            }
        }
    }
}

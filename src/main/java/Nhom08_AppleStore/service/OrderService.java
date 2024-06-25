package Nhom08_AppleStore.service;

import Nhom08_AppleStore.model.CartItem;
import Nhom08_AppleStore.model.Order;
import Nhom08_AppleStore.model.OrderDetail;
import Nhom08_AppleStore.model.OrderStatus;
import Nhom08_AppleStore.repository.OrderDetailRepository;
import Nhom08_AppleStore.repository.OrderRepository;
import groovy.lang.GString;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private CartService cartService;
    @Transactional
    public Order createOrder(String customerName, String address,String phoneNumber, String eMail, String note, String payment, List<CartItem> cartItems) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        System.out.println("Khởi tạo đơn hàng cho: " + username);

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setUsername(username);
        order.setAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setEMail(eMail);
        order.setNote(note);
        order.setPayment(payment);
        order.setDate(LocalDateTime.now());

        order = orderRepository.save(order);
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            orderDetailRepository.save(detail);
        }
        cartService.clearCart();
        return order;
    }
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return new Order();
    }

    public String cancelOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            if (order.getStatus() == OrderStatus.DELIVERED) {
                return "Đơn hàng đang được giao không thể huỷ";
            }
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return "Đơn hàng đã được huỷ thành công";
        } else {
            return "Không tìm thấy đơn hàng";
        }
    }
}
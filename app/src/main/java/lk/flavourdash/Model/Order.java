package lk.flavourdash.Model;

import com.google.firebase.firestore.Exclude;
import com.google.type.DateTime;

import java.util.Date;

public class Order {
    private String id;
    private String userId;
    private String orderStatus;
    private Double totalAmount;
    private Date orderTime;
    private String deliveryAddress;
    private Double deliveryAddressLatitude;
    private Double deliveryAddressLongitude;
    private String requests;

    public Order() {
    }

    public Order(String userId, String orderStatus, Double totalAmount, Date orderTime, String deliveryAddress, Double deliveryAddressLatitude, Double deliveryAddressLongitude, String requests) {
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderTime = orderTime;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressLatitude = deliveryAddressLatitude;
        this.deliveryAddressLongitude = deliveryAddressLongitude;
        this.requests = requests;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Double getDeliveryAddressLatitude() {
        return deliveryAddressLatitude;
    }

    public void setDeliveryAddressLatitude(Double deliveryAddressLatitude) {
        this.deliveryAddressLatitude = deliveryAddressLatitude;
    }

    public Double getDeliveryAddressLongitude() {
        return deliveryAddressLongitude;
    }

    public void setDeliveryAddressLongitude(Double deliveryAddressLongitude) {
        this.deliveryAddressLongitude = deliveryAddressLongitude;
    }

    public String getRequests() {
        return requests;
    }

    public void setRequests(String requests) {
        this.requests = requests;
    }
}

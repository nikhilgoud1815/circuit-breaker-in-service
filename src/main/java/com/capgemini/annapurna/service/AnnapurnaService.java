/**
 * 
 */
package com.capgemini.annapurna.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.capgemini.annapurna.restaurant.entity.Address;
import com.capgemini.annapurna.restaurant.entity.Cart;
import com.capgemini.annapurna.restaurant.entity.FoodProducts;
import com.capgemini.annapurna.restaurant.entity.Order;
import com.capgemini.annapurna.restaurant.entity.Restaurant;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author ugawari
 *
 */
@EnableCircuitBreaker
@Service
public class AnnapurnaService {

	@Autowired
	private RestTemplate restTemplate;

	private Restaurant restaurant;
	private Address address;
	private Cart cart;

	private Set<FoodProducts> orderProducts;

	private Integer getUniqueId() {
		UUID idOne = UUID.randomUUID();
		int uid = idOne.hashCode();
		return uid;
	}

	@HystrixCommand(fallbackMethod = "fallBackForFoodItems")
	public String getFoodItemsById(Model model, int restaurantId) {
		ResponseEntity<Restaurant> entity = restTemplate
				.getForEntity("http://annapurna-restaurant/restaurants/" + restaurantId, Restaurant.class);
		restaurant = entity.getBody();
		model.addAttribute("restaurant", entity.getBody());
		return "FoodItems";
	}

	public String fallBackForFoodItems(Model model, @RequestParam int restaurantId) {
		model.addAttribute("message", "wait for a while !");
		return "FoodItems";

	}

	@HystrixCommand(fallbackMethod = "fallBackForGetAllCarts")
	// @RequestMapping("/cart/getAll") // /cart/getAll
	public String getAllCarts(Model model) {
		System.out.println("getAll");
		ResponseEntity<List> carts = restTemplate.getForEntity("http://annapurna-cart/carts", List.class);
		model.addAttribute("carts", carts.getBody());
		return "GetAllCart";
	}

	public String fallBackForGetAllCarts(Model model) {
		model.addAttribute("message", "wait for a while !");
		return "Order";

	}

	@HystrixCommand(fallbackMethod = "fallBackForAddingIntoCart")
	// @RequestMapping("/cart/addCart")
	public String addCart(/* @RequestParam String restaurantName, */ @RequestParam String foodName,
			@RequestParam double price, @RequestParam int quantity/* ,@RequestParam Address address */, Model model) {
		Set<FoodProducts> products = new HashSet<FoodProducts>();
		products.add(new FoodProducts(foodName, price, quantity));
		cart = new Cart(106, restaurant.getName(), products, price, restaurant.getAddress());
		restTemplate.postForEntity("http://annapurna-cart/carts", cart, Cart.class);
		model.addAttribute("cart", cart);
		return "GetAllCart";
	}

	public String fallBackForAddingIntoCart(@RequestParam String foodName, @RequestParam double price,
			@RequestParam int quantity/* ,@RequestParam Address address */, Model model) {
		model.addAttribute("message", "wait for a while !");
		return "Order";

	}

	@HystrixCommand(fallbackMethod = "fallBackForGetOrderById")
	// @RequestMapping("/cart/getById")
	public String getOrderById(@RequestParam("orderId") Integer orderId, Model model) {
		ResponseEntity<Order> order = restTemplate.getForEntity("http://annapurna-order/orders/" + orderId + " ",
				Order.class);
		System.out.println(order.getBody());
		model.addAttribute("message", "heyyyyyyyy !!!!");
		model.addAttribute("Order", order.getBody());
		return "Order";
	}

	public String fallBackForGetOrderById(@RequestParam("orderId") Integer orderId, Model model) {
		model.addAttribute("message", "wait for a while !");
		return "Order";

	}

	@HystrixCommand(fallbackMethod = "fallBackForPlaceOrder")
	public String placeOrder(Address address1, Model model) {
		address = address1;
		model.addAttribute("totalAmount", cart.getTotalAmount());
		return "passMoney";
	}

	public String fallBackForPlaceOrder(@ModelAttribute Address address1, Model model) {
		model.addAttribute("message", "wait for a while !");
		return "Order";

	}

	@HystrixCommand(fallbackMethod = "fallBackForGetPlaceOrder")
	// @RequestMapping("/cart/placeOrder")
	public String getPlaceOrder(/* @RequestBody Cart cart, */Model model) {
		Set<FoodProducts> products = new HashSet<FoodProducts>();
		products.add(new FoodProducts("Brinjal", 234, 12));
		orderProducts = products;
		return "AddressForm";
	}

	public String fallBackForGetPlaceOrder(Model model) {
		model.addAttribute("message", "wait for a while !");
		return "Order";

	}

	
	
	@HystrixCommand(fallbackMethod = "passMoneyForEwallet")
	public String deduct(Double amount, Model model) {

		restTemplate.put("http://annapurna-ewallet/ewallets/" + 1 + "/pay?currentBalance=" + amount, null);
		model.addAttribute("message", "money deducted and Order placed Successfully!");
		String modeOfPayment = "E-Wallet"; // for now
		Double totalAmount = 100.0; // for now
		String restaurantName = "GrandMama's Cafe"; // for now
		Integer id = getUniqueId();
		Order order = new Order(id, modeOfPayment, "pending", cart.getProducts(), cart.getTotalAmount(),
				cart.getRestaurantName(), address, cart.getCartId());
		ResponseEntity<Order> order1 = restTemplate.postForEntity("http://annapurna-order/orders", order, Order.class);
		model.addAttribute("Order", order1.getBody());
		System.out.println(order1.getBody());
		return "passMoney";
	}

	public String passMoneyForEwallet(/* /@RequestParam Integer profileId, */ @RequestParam Double amount,
			Model model) {
		model.addAttribute("message", "wait for a while !");
		return "passMoney";

	}

}

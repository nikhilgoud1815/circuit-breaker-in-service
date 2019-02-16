/**
 * 
 */
package com.capgemini.annapurna.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.capgemini.annapurna.restaurant.entity.Address;
import com.capgemini.annapurna.restaurant.entity.Cart;
import com.capgemini.annapurna.restaurant.entity.FoodItem;
import com.capgemini.annapurna.restaurant.entity.FoodProducts;
import com.capgemini.annapurna.restaurant.entity.Profile;
import com.capgemini.annapurna.restaurant.entity.Restaurant;
import com.capgemini.annapurna.service.AnnapurnaService;

/**
 * @author ugawari
 *
 */
@EnableCircuitBreaker
@Controller
public class AnnapurnaController {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private AnnapurnaService annapurnaService;
	
	/******* Login ******/
	/*
	 * @RequestMapping(value = "/login", method = RequestMethod.GET) public
	 * ModelAndView login() { ModelAndView modelAndView = new ModelAndView();
	 * modelAndView.setViewName("login"); return modelAndView; }
	 */
	
	 @RequestMapping(value = "/login", method = RequestMethod.GET)
	    public ModelAndView login() {
	        ModelAndView modelAndView = new ModelAndView();
	        modelAndView.setViewName("NewFile");
	        return modelAndView;
	    }

	/******** Restaurant ********/
	
	@RequestMapping("/")
	public String getAllRestaurants(Model model) {
		ResponseEntity<List> entity = restTemplate.getForEntity("http://annapurna-restaurant/restaurants", List.class);
		model.addAttribute("list", entity.getBody());
		//return "NewFile";
		return "Home";
	}

	@RequestMapping("/search")
	public String search(Model model, @RequestParam String search) {
		ResponseEntity<Restaurant[]> entity = restTemplate.getForEntity("http://annapurna-restaurant/restaurants",
				Restaurant[].class);
		List<Restaurant> restaurantlist = Arrays.asList(entity.getBody());
		List<Restaurant> searchedList = new ArrayList<>();
		for (Restaurant restaurant : restaurantlist) {
			if (restaurant.getName().equalsIgnoreCase(search))
				searchedList.add(restaurant);
			if (restaurant.getAddress().getCity().equalsIgnoreCase(search))
				searchedList.add(restaurant);
			for (FoodItem foodItem : restaurant.getFoodItems()) {
				if (foodItem.getFoodName().equalsIgnoreCase(search))
					searchedList.add(restaurant);
			}
		}
		model.addAttribute("list", searchedList);
		return "Home";
	}

	private Restaurant restaurant;
	private Address address;
	private Cart cart;

	@RequestMapping("/foodItems")
	public String getFoodItemsById(Model model, @RequestParam int restaurantId) {
		return annapurnaService.getFoodItemsById(model, restaurantId);
	}

	/******** Profile ********/
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signUpPage() {
		return "AccountForm";
	}
	
	@Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/createAccount")
	public String createAccount(@ModelAttribute Profile profile,Model model){
		System.out.println(profile);
		profile.setPassword(bCryptPasswordEncoder.encode(profile.getPassword()));
		profile.setRole("USER");
		restTemplate.postForEntity("http://annapurna-profile/profiless", profile, Profile.class);
		ResponseEntity<List> entity = restTemplate.getForEntity("http://annapurna-restaurant/restaurants", List.class);
		model.addAttribute("list", entity.getBody());
		return "Home";
	}
	
	@RequestMapping("/update")
	public String updateAccount() {
		return "updateForm";
	}
	
	
	/******** Cart ********/
	
	@RequestMapping("/cart/getAll") // /cart/getAll
	public String getAllCarts(Model model) {
		return annapurnaService.getAllCarts(model);
	}
	
	

	@RequestMapping("/cart/addCart")
	public String addCart(/* @RequestParam String restaurantName, */ @RequestParam String foodName,
			@RequestParam double price, @RequestParam int quantity/* ,@RequestParam Address address */, Model model) {
		return annapurnaService.addCart(foodName, price, quantity, model);
	}

	private Integer getUniqueId() {
		UUID idOne = UUID.randomUUID();
		int uid = idOne.hashCode();
		return uid;
	}

	private static Set<FoodProducts> orderProducts;

	/******** Order ********/
	
	@RequestMapping("/cart/getById")
	public String getOrderById(@RequestParam("orderId") Integer orderId, Model model) {
		return annapurnaService.getOrderById(orderId, model);
	}
	
	
	
	
	

	@RequestMapping("/cart/submitAddress")
	public String placeOrder(@ModelAttribute Address address1, Model model) {
		return annapurnaService.placeOrder(address1, model);
	}
	
	@RequestMapping("/cart/placeOrder")
	public String getPlaceOrder(/* @RequestBody Cart cart, */Model model) {
		return annapurnaService.getPlaceOrder(model);
	}
	
	
	
	/******** eWallet ********/

	@RequestMapping("/cart/passMoneyForm")
	public String deduct(/* @RequestParam Integer profileId, */ @RequestParam Double amount, Model model) {
		return annapurnaService.deduct(amount, model);
	}

	@RequestMapping("/AddMoneyLink")
	public String depositForm() {
		return "addMoney";
	}

	@RequestMapping("/AddMoneyForm")
	public String deposit(@RequestParam Integer profileId, @RequestParam Double amount, Model model) {
		restTemplate.put("http://annapurna-ewallet/ewallets/" + profileId + "?currentBalance=" + amount, null);
		model.addAttribute("message", "money added Successfully!");
		return "addMoney";
	}

	@RequestMapping("/PassMoneyLink")
	public String deductAmount() {
		return "passMoney";
	}

	@RequestMapping("/StatementForm")
	public String statementForm() {
		return "statements";
	}

	@RequestMapping("/statement/{profileId}")
	public String statement(@RequestParam Integer profileId, Model model) {
		ResponseEntity<List> entity = restTemplate
				.getForEntity("http://annapurna-ewallet/ewallets/statements/" + profileId, List.class);
		model.addAttribute("statements", entity.getBody());
		return "statements";
	}

}

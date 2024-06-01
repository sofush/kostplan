package com.example.kostplan.service;

import com.example.kostplan.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
	/**
	 * Identifier of the subscription price object which is registered in Stripe's REST API.
	 */
	private static final String PRICE_ID = "price_1PMoaTJg6BITnXXXVoD77vdD";

	private final PersistentStorage storage;

	@Value("${stripe.key.test}")
	private String stripeApiKey;

	@Autowired
	public PaymentService(PersistentStorage storage) {
		this.storage = storage;
	}

	public boolean hasActiveSubscription(String username)
		throws StripeException
	{
		this.instantiateApiKey();

		Customer customer = this.retrieveCustomer(username);
		List<Subscription> subscriptions = customer.getSubscriptions().getData();

		return subscriptions.stream()
			.anyMatch((subscription) -> subscription.getStatus().contentEquals("active"));
	}

	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public Session createPaymentSession(String username)
		throws StripeException
	{
		this.instantiateApiKey();

		SessionCreateParams params =
			SessionCreateParams.builder()
				.setSuccessUrl("http://localhost:8080/week")
				.setCustomer(this.retrieveCustomer(username).getId())
				.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				.setUiMode(SessionCreateParams.UiMode.HOSTED)
				.setCurrency("DKK")
				.addLineItem(
					SessionCreateParams.LineItem.builder()
						.setPrice(PRICE_ID)
						.setQuantity((long) 1)
						.build()
				)
				.build();

		return Session.create(params);
	}

	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public Customer retrieveCustomer(String username)
		throws StripeException, DataAccessException
	{
		this.instantiateApiKey();
		User user = this.storage.findUserByUsername(username);

		CustomerSearchParams params =
			CustomerSearchParams.builder()
				.addExpand("data.subscriptions")
				.setQuery("metadata['username']: '" + username + "'")
				.setLimit((long) 1)
				.build();

		CustomerSearchResult res = Customer.search(params);
		List<Customer> customers = res.getData();

		if (!customers.isEmpty())
			return customers.getFirst();

		CustomerCreateParams customerParams = CustomerCreateParams
			.builder()
			.addExpand("subscriptions")
			.setMetadata(Map.of("username", username))
			.setEmail(user.getEmailAddress())
			.setPhone(user.getPhoneNumber())
			.build();

		return Customer.create(customerParams);
	}

	private void instantiateApiKey() {
		if (Stripe.apiKey == null)
			Stripe.apiKey = this.stripeApiKey;
	}
}

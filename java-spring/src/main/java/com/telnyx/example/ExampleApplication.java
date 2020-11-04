package com.telnyx.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telnyx.sdk.ApiClient;
import com.telnyx.sdk.ApiException;
import com.telnyx.sdk.Configuration;
import com.telnyx.sdk.api.MessagesApi;
import com.telnyx.sdk.api.NumberOrdersApi;
import com.telnyx.sdk.api.NumberSearchApi;
import com.telnyx.sdk.auth.HttpBearerAuth;
import com.telnyx.sdk.model.AvailablePhoneNumber;
import com.telnyx.sdk.model.CreateMessageRequest;
import com.telnyx.sdk.model.CreateNumberOrderRequest;
import com.telnyx.sdk.model.InboundMessageEvent;
import com.telnyx.sdk.model.InboundMessagePayload;
import com.telnyx.sdk.model.ListAvailablePhoneNumbersResponse;
import com.telnyx.sdk.model.MessageResponse;
import com.telnyx.sdk.model.NumberOrder;
import com.telnyx.sdk.model.NumberOrderEvent;
import com.telnyx.sdk.model.NumberOrderResponse;
import com.telnyx.sdk.model.OutboundMessageEvent;
import com.telnyx.sdk.model.OutboundMessagePayload;
import com.telnyx.sdk.model.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@SpringBootApplication
@RestController
public class ExampleApplication {

    Dotenv dotenv = Dotenv.load();
    private final String YOUR_TELNYX_API_KEY = dotenv.get("TELNYX_API_KEY");
    private final String YOUR_TELNYX_MESSAGING_PROFILE_ID = dotenv.get("TELNYX_MESSAGING_PROFILE_ID");
    private static final String MESSAGING_OUTBOUND_PATH = "/messaging/outbound";
    private static final String MESSAGING_INBOUND_PATH = "/messaging/inbound";
    private static final String NUMBER_ORDER_PATH = "/numbers/orders";
    private static final String NUMBERS_PATH = "/numbers";


    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Autowired
    void configureObjectMapper(final ObjectMapper mapper) {
        mapper.registerModule(new JsonNullableModule());
    }
    @GetMapping("/")
    public String hello(){
        return "Hello World";
    }

    @PostMapping(NUMBER_ORDER_PATH)
    public String numberOrder(@RequestBody NumberOrderEvent numberOrderEvent) {
        NumberOrder numberOrder = numberOrderEvent.getData().getPayload();
        numberOrder.getPhoneNumbers().forEach(System.out::println);
        return "";
    }

    @GetMapping(NUMBERS_PATH)
    public List<String> numberSearch(@RequestParam Map<String,String> allParams){
        String countryCode = allParams.get("countryCode");
        String state = allParams.get("state");
        String city = allParams.get("city");

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api.telnyx.com/v2");
        // Configure HTTP bearer authorization: bearerAuth
        HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
        bearerAuth.setBearerToken(YOUR_TELNYX_API_KEY);
        NumberSearchApi apiInstance = new NumberSearchApi(defaultClient);

        try {
            ListAvailablePhoneNumbersResponse availablePhoneNumbers = apiInstance
                    .listAvailablePhoneNumbers()
                    .filterCountryCode(countryCode)
                    .filterAdministrativeArea(state)
                    .filterLocality(city)
                    .execute();

            List<String> phoneNumbers = availablePhoneNumbers
                .getData()
                .stream()
                .map(AvailablePhoneNumber::getPhoneNumber)
                .collect(Collectors.toList());
            return phoneNumbers;
        } catch (Exception e) {
            System.err.println("Exception when calling NumberSearchApi#listAvailablePhoneNumbers");
            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    @PostMapping(NUMBERS_PATH)
    public NumberOrder numberOrder(@RequestBody Map<String,String> allParams){
        String phoneNumber = allParams.get("phoneNumber");
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api.telnyx.com/v2");

        // Configure HTTP bearer authorization: bearerAuth
        HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
        bearerAuth.setBearerToken(YOUR_TELNYX_API_KEY);

        NumberOrdersApi apiInstance = new NumberOrdersApi(defaultClient);
        CreateNumberOrderRequest createNumberOrderRequest = new CreateNumberOrderRequest()
            .addPhoneNumbersItem(new PhoneNumber().phoneNumber(phoneNumber))
            .messagingProfileId(YOUR_TELNYX_MESSAGING_PROFILE_ID);
        try {
            NumberOrderResponse result = apiInstance.createNumberOrder(createNumberOrderRequest);
            return result.getData();
        } catch (ApiException e) {
            System.err.println("Exception when calling NumberOrdersApi#createNumberOrder");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return new NumberOrder();
        }
    }

    @PostMapping(MESSAGING_OUTBOUND_PATH)
    public String outboundMessage(@RequestBody OutboundMessageEvent messageEvent){
        OutboundMessagePayload messagePayload = messageEvent.getData().getPayload();
        UUID messageId = messagePayload.getId();
        System.out.printf("Received message: %s\n", messageId.toString());
        return messageId.toString();
    }

    @PostMapping(MESSAGING_INBOUND_PATH)
    public String inboundMessage(@RequestBody InboundMessageEvent messageEvent){
        InboundMessagePayload messagePayload = messageEvent.getData().getPayload();
        UUID messageId = messagePayload.getId();
        String inboundText = messagePayload.getText().toLowerCase().trim();
        String from = messagePayload.getFrom().getPhoneNumber();
        String to = messagePayload.getTo().get(0).getPhoneNumber();
        String webhookUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(MESSAGING_OUTBOUND_PATH).build().toUriString();

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api.telnyx.com/v2");
        // Configure HTTP bearer authorization: bearerAuth
        HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
        bearerAuth.setBearerToken(YOUR_TELNYX_API_KEY);
        MessagesApi apiInstance = new MessagesApi(defaultClient);

        String text;
        switch (inboundText) {
            case "hello":
                text = "Hello world";
                break;
            case "bye":
                text = "Goodnight Moon";
                break;
            default:
                text = "I can respond to 'hello' or 'bye', try sending one of those words";
                break;
        }


        CreateMessageRequest createMessageRequest = new CreateMessageRequest()
                .to(from)
                .from(to)
                .text(text)
                .webhookUrl(webhookUrl)
                .useProfileWebhooks(false);

        try {
            MessageResponse result = apiInstance.createMessage(createMessageRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MessagesApi#createMessage");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }


        return messageId.toString();
    }
}

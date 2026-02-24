# Canvas Android — SaaS Architecture & Payment Integration Guide
> Adding multi-tier subscriptions + Stripe, Paystack, Paddle, Flutterwave, Airtel Money, M-Pesa, MTN MoMo · 2026-02-24

---

## Table of Contents
1. [SaaS Architecture Overview](#1-saas-architecture-overview)
2. [Subscription Tiers Design](#2-subscription-tiers-design)
3. [Backend Requirements](#3-backend-requirements)
4. [Android Architecture for Payments](#4-android-architecture-for-payments)
5. [New Module Structure](#5-new-module-structure)
6. [Stripe Integration](#6-stripe-integration)
7. [Paystack Integration](#7-paystack-integration)
8. [Paddle Integration](#8-paddle-integration)
9. [Flutterwave Integration](#9-flutterwave-integration)
10. [Airtel Money Integration](#10-airtel-money-integration)
11. [M-Pesa Integration](#11-m-pesa-integration)
12. [MTN MoMo Integration](#12-mtn-momo-integration)
13. [Payment Gateway Routing](#13-payment-gateway-routing)
14. [Subscription State in the App](#14-subscription-state-in-the-app)
15. [Feature Gating](#15-feature-gating)
16. [Webhook Handling (Backend)](#16-webhook-handling-backend)
17. [Free Trial Flow](#17-free-trial-flow)
18. [Offline Grace Period](#18-offline-grace-period)
19. [Hilt DI Wiring](#19-hilt-di-wiring)
20. [Testing Payments](#20-testing-payments)
21. [Compliance & Security](#21-compliance--security)
22. [Implementation Checklist](#22-implementation-checklist)

---

## 1. SaaS Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│  YOUR BACKEND (SaaS API)                                        │
│                                                                  │
│  POST /api/subscriptions/create                                  │
│  GET  /api/subscriptions/status                                  │
│  POST /api/subscriptions/cancel                                  │
│  POST /api/payments/initiate    ← calls payment gateway         │
│  POST /api/payments/verify      ← verifies payment              │
│  POST /webhooks/stripe          ← Stripe webhooks               │
│  POST /webhooks/paystack        ← Paystack webhooks             │
│  POST /webhooks/paddle          ← Paddle webhooks               │
│  POST /webhooks/flutterwave     ← Flutterwave webhooks          │
│  POST /webhooks/mpesa           ← Daraja callback               │
│  POST /webhooks/airtel          ← Airtel callback               │
│  POST /webhooks/mtn             ← MTN MoMo callback             │
└───────────────────────────┬─────────────────────────────────────┘
                            │  HTTPS + your API token (from ApiPrefs)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  ANDROID APP                                                     │
│  SubscriptionRepository → BillingViewModel → PaymentActivity    │
│  SubscriptionManager    → FeatureGate (gates premium features)  │
└─────────────────────────────────────────────────────────────────┘
```

**Key principle**: Payment processing happens **on your backend**, not on the device. The Android app:
1. Shows the paywall / plan selection UI
2. Collects the **minimum** info needed (plan selection, for mobile-money: phone number)
3. Passes everything to **your server** to call the payment gateway
4. Listens for a payment status result (polling or deep link callback)
5. Updates local subscription state

This keeps payment keys off the device and makes PCI compliance manageable.

---

## 2. Subscription Tiers Design

### Recommended tier structure

```kotlin
// libs/saas/src/main/java/com/YOURBRAND/saas/model/SubscriptionTier.kt

enum class SubscriptionTier(
    val id: String,
    val displayName: String,
    val monthlyPriceUsd: Double,
    val yearlyPriceUsd: Double,
    val features: Set<Feature>
) {
    FREE(
        id = "free",
        displayName = "Free",
        monthlyPriceUsd = 0.0,
        yearlyPriceUsd = 0.0,
        features = setOf(Feature.BASIC_COURSES, Feature.ASSIGNMENTS_VIEW)
    ),
    STUDENT_BASIC(
        id = "student_basic",
        displayName = "Student Basic",
        monthlyPriceUsd = 4.99,
        yearlyPriceUsd = 49.99,
        features = setOf(Feature.BASIC_COURSES, Feature.ASSIGNMENTS_VIEW,
                         Feature.SUBMISSIONS, Feature.GRADES, Feature.INBOX)
    ),
    STUDENT_PRO(
        id = "student_pro",
        displayName = "Student Pro",
        monthlyPriceUsd = 9.99,
        yearlyPriceUsd = 99.99,
        features = Feature.entries.toSet()  // All features
    ),
    INSTITUTION(
        id = "institution",
        displayName = "Institution",
        monthlyPriceUsd = 0.0,  // Custom pricing via sales
        yearlyPriceUsd = 0.0,
        features = Feature.entries.toSet()
    )
}

enum class Feature {
    BASIC_COURSES,
    ASSIGNMENTS_VIEW,
    SUBMISSIONS,
    GRADES,
    INBOX,
    OFFLINE_MODE,
    PDF_ANNOTATIONS,
    CALENDAR,
    SMART_SEARCH,
    VIDEO_SUBMISSIONS,
    PARENT_OBSERVER,
    ANALYTICS_DASHBOARD
}
```

---

## 3. Backend Requirements

Your backend must expose these endpoints. Implementation language is your choice (Node.js/Express, Python/FastAPI, Go, etc.).

### Subscription API

```
POST /api/subscriptions/create
Body: { userId, planId, billingCycle: "monthly"|"yearly", paymentMethod: "stripe"|"paystack"|... }
Response: { subscriptionId, status, paymentUrl? }

GET /api/subscriptions/status
Header: Authorization: Bearer <userToken>
Response: { tier, status: "active"|"trialing"|"past_due"|"canceled", expiresAt, features[] }

POST /api/subscriptions/cancel
Body: { subscriptionId, reason? }

POST /api/subscriptions/upgrade
Body: { subscriptionId, newPlanId }
```

### Payment API

```
POST /api/payments/initiate
Body: { amount, currency, gateway: "stripe"|"paystack"|..., metadata: { planId, userId } }
Response: { paymentId, clientSecret? (Stripe), authorizationUrl? (Paystack), checkoutUrl? (Paddle) }

POST /api/payments/verify
Body: { paymentId, reference? }
Response: { status: "success"|"pending"|"failed", subscriptionId? }

GET /api/payments/history
Response: [{ paymentId, amount, currency, gateway, date, status }]
```

---

## 4. Android Architecture for Payments

### New library module: `libs/saas`

```
libs/saas/
├── build.gradle
└── src/main/java/com/YOURBRAND/saas/
    ├── model/
    │   ├── SubscriptionTier.kt
    │   ├── SubscriptionStatus.kt
    │   ├── PaymentMethod.kt
    │   └── PaymentResult.kt
    ├── api/
    │   ├── SubscriptionApi.kt          (Retrofit interface)
    │   └── PaymentApi.kt               (Retrofit interface)
    ├── repository/
    │   ├── SubscriptionRepository.kt   (interface)
    │   └── SubscriptionRepositoryImpl.kt
    ├── gateway/
    │   ├── PaymentGateway.kt           (interface)
    │   ├── StripeGateway.kt
    │   ├── PaystackGateway.kt
    │   ├── PaddleGateway.kt
    │   ├── FlutterwaveGateway.kt
    │   ├── AirtelMoneyGateway.kt
    │   ├── MpesaGateway.kt
    │   └── MtnMomoGateway.kt
    ├── manager/
    │   └── SubscriptionManager.kt      (singleton, cached subscription state)
    ├── gate/
    │   └── FeatureGate.kt              (checks feature access)
    └── di/
        └── SaasModule.kt               (Hilt bindings)
```

### Core interfaces

```kotlin
// SubscriptionRepository.kt
interface SubscriptionRepository {
    suspend fun getSubscriptionStatus(): SubscriptionStatus
    suspend fun createSubscription(planId: String, gateway: PaymentGateway): PaymentResult
    suspend fun cancelSubscription(): Boolean
    fun observeSubscription(): Flow<SubscriptionStatus>
}

// PaymentGateway.kt
interface PaymentGateway {
    val id: String
    val displayName: String
    val supportedCurrencies: List<String>
    val supportedCountries: List<String>
    suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult
    suspend fun verifyPayment(paymentId: String): PaymentVerifyResult
}

// FeatureGate.kt
class FeatureGate @Inject constructor(
    private val subscriptionManager: SubscriptionManager
) {
    fun isEnabled(feature: Feature): Boolean =
        subscriptionManager.currentTier.features.contains(feature)

    fun requireFeature(feature: Feature, onLocked: () -> Unit, onUnlocked: () -> Unit) {
        if (isEnabled(feature)) onUnlocked() else onLocked()
    }
}
```

---

## 5. New Module Structure

### Add to `apps/settings.gradle`
```groovy
include ':saas'
project(':saas').projectDir = new File('../libs/saas')
```

### Add to each app's `build.gradle`
```groovy
dependencies {
    implementation project(path: ':saas')
    // Stripe Android SDK
    implementation 'com.stripe:stripe-android:21.+'
    // Paystack Android SDK
    implementation 'co.paystack.android:paystack:3.1.3'
    // Flutterwave RavePayment
    implementation 'com.flutterwave.rave_android:rave_android:2.1.30'
}
```

---

## 6. Stripe Integration

Stripe is recommended for **international** payments (USD, EUR, GBP, etc.).

### Android Setup

**`build.gradle`**
```groovy
implementation 'com.stripe:stripe-android:21.+'
```

**`AndroidManifest.xml`**
```xml
<!-- Stripe 3D Secure return URL -->
<activity
    android:name=".payment.PaymentResultActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="yourbrand" android:host="payment" />
    </intent-filter>
</activity>
```

### Gateway Implementation

```kotlin
// StripeGateway.kt
class StripeGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "stripe"
    override val displayName = "Credit / Debit Card"
    override val supportedCurrencies = listOf("USD", "EUR", "GBP", "KES", "NGN", "ZAR")
    override val supportedCountries = listOf("*") // Global

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        // Call your backend to create a PaymentIntent
        val response = paymentApi.initiateStripePayment(
            StripePaymentRequest(
                amount = request.amountCents,
                currency = request.currency,
                planId = request.planId,
                userId = request.userId
            )
        )
        // Backend returns clientSecret
        return PaymentInitResult.StripeResult(clientSecret = response.clientSecret)
    }
}
```

### Payment UI (Stripe PaymentSheet)

```kotlin
// StripePaymentFragment.kt
@AndroidEntryPoint
class StripePaymentFragment : Fragment() {

    @Inject lateinit var stripeGateway: StripeGateway

    private lateinit var paymentSheet: PaymentSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Stripe with your PUBLISHABLE key (safe on device — not secret key)
        PaymentConfiguration.init(requireContext(), BuildConfig.STRIPE_PUBLISHABLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    private fun launchStripePayment(clientSecret: String) {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "YourBrand",
            allowsDelayedPaymentMethods = true
        )
        paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
    }

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> viewModel.onPaymentSuccess()
            is PaymentSheetResult.Canceled  -> viewModel.onPaymentCanceled()
            is PaymentSheetResult.Failed    -> viewModel.onPaymentFailed(result.error)
        }
    }
}
```

### Backend — Create PaymentIntent
```javascript
// Node.js example
app.post('/api/payments/initiate-stripe', async (req, res) => {
    const { amount, currency, planId, userId } = req.body;
    const paymentIntent = await stripe.paymentIntents.create({
        amount,       // in smallest currency unit (cents)
        currency,
        metadata: { planId, userId },
        automatic_payment_methods: { enabled: true }
    });
    res.json({ clientSecret: paymentIntent.client_secret });
});
```

### Add to `private-data/student/gradle.properties`
```properties
stripePublishableKey=pk_live_YOUR_STRIPE_PUBLISHABLE_KEY
```

### Add to `build.gradle`
```groovy
buildConfigField "String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableKey\""
```

---

## 7. Paystack Integration

Paystack is optimized for **Nigeria, Ghana, South Africa, Kenya**.

### Android Setup
```groovy
implementation 'co.paystack.android:paystack:3.1.3'
```

### Gateway Implementation

```kotlin
// PaystackGateway.kt
class PaystackGateway @Inject constructor(
    private val paymentApi: PaymentApi,
    @ApplicationContext private val context: Context
) : PaymentGateway {
    override val id = "paystack"
    override val displayName = "Paystack (Card / Bank / USSD)"
    override val supportedCurrencies = listOf("NGN", "GHS", "ZAR", "KES", "USD")
    override val supportedCountries = listOf("NG", "GH", "ZA", "KE")

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        // Backend creates a Paystack transaction, returns authorization URL
        val response = paymentApi.initiatePaystackPayment(
            PaystackPaymentRequest(
                email = request.userEmail,
                amount = request.amountKobo,  // in kobo (NGN cents)
                currency = request.currency,
                planId = request.planId,
                callbackUrl = "yourbrand://payment/paystack"
            )
        )
        return PaymentInitResult.WebRedirect(
            url = response.authorizationUrl,
            reference = response.reference
        )
    }

    override suspend fun verifyPayment(paymentId: String): PaymentVerifyResult {
        val response = paymentApi.verifyPaystackPayment(reference = paymentId)
        return if (response.status == "success")
            PaymentVerifyResult.Success(subscriptionId = response.subscriptionId)
        else
            PaymentVerifyResult.Failed(response.gatewayResponse)
    }
}
```

### Payment UI (WebView for Paystack)

```kotlin
// WebPaymentFragment.kt — handles Paystack, Paddle, Flutterwave
@AndroidEntryPoint
class WebPaymentFragment : Fragment() {

    @Inject lateinit var subscriptionRepository: SubscriptionRepository

    private fun launchWebPayment(authorizationUrl: String, reference: String) {
        // Open a Chrome Custom Tab for the payment page
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(requireContext(), Uri.parse(authorizationUrl))
        // App will receive deep link callback when payment completes
    }

    // Handle deep link callback
    fun onPaymentCallback(uri: Uri) {
        val reference = uri.getQueryParameter("reference") ?: return
        viewModel.verifyPaystackPayment(reference)
    }
}
```

### Backend — Initialize Paystack Transaction
```javascript
app.post('/api/payments/initiate-paystack', async (req, res) => {
    const { email, amount, currency, planId, callbackUrl } = req.body;
    const response = await axios.post(
        'https://api.paystack.co/transaction/initialize',
        { email, amount, currency, metadata: { planId }, callback_url: callbackUrl },
        { headers: { Authorization: `Bearer ${process.env.PAYSTACK_SECRET_KEY}` } }
    );
    res.json({
        authorizationUrl: response.data.data.authorization_url,
        reference: response.data.data.reference
    });
});
```

---

## 8. Paddle Integration

Paddle is best for **SaaS subscription billing** with built-in tax handling and dunning.

### Android — WebView Checkout

Paddle provides a web-based checkout. Open via Chrome Custom Tabs:

```kotlin
// PaddleGateway.kt
class PaddleGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "paddle"
    override val displayName = "Paddle (Card / PayPal)"
    override val supportedCurrencies = listOf("USD", "EUR", "GBP", "AUD", "CAD")
    override val supportedCountries = listOf("*")  // Global, excluding sanctioned countries

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        val response = paymentApi.initiatePaddleCheckout(
            PaddleCheckoutRequest(
                priceId = mapPlanToPaddlePriceId(request.planId),
                userId = request.userId,
                email = request.userEmail,
                successUrl = "yourbrand://payment/paddle/success",
                cancelUrl = "yourbrand://payment/paddle/cancel"
            )
        )
        return PaymentInitResult.WebRedirect(
            url = response.checkoutUrl,
            reference = response.transactionId
        )
    }

    private fun mapPlanToPaddlePriceId(planId: String): String = when (planId) {
        "student_basic_monthly" -> BuildConfig.PADDLE_PRICE_BASIC_MONTHLY
        "student_basic_yearly"  -> BuildConfig.PADDLE_PRICE_BASIC_YEARLY
        "student_pro_monthly"   -> BuildConfig.PADDLE_PRICE_PRO_MONTHLY
        "student_pro_yearly"    -> BuildConfig.PADDLE_PRICE_PRO_YEARLY
        else -> throw IllegalArgumentException("Unknown plan: $planId")
    }
}
```

### Backend — Create Paddle Transaction
```javascript
// Using Paddle Billing API v2
app.post('/api/payments/initiate-paddle', async (req, res) => {
    const { priceId, userId, email, successUrl } = req.body;
    const transaction = await paddle.transactions.create({
        items: [{ price_id: priceId, quantity: 1 }],
        customer: { email },
        custom_data: { userId },
        checkout: { url: successUrl }
    });
    res.json({ checkoutUrl: transaction.checkout.url, transactionId: transaction.id });
});
```

### Add Paddle price IDs to `gradle.properties`
```properties
paddlePriceBasicMonthly=pri_XXXXX
paddlePriceBasicYearly=pri_XXXXX
paddlePriceProMonthly=pri_XXXXX
paddlePriceProYearly=pri_XXXXX
```

---

## 9. Flutterwave Integration

Flutterwave covers **Africa broadly** — Nigeria, Ghana, Kenya, Uganda, Tanzania, Rwanda, Zambia, etc.

### Android Setup
```groovy
implementation 'com.flutterwave.rave_android:rave_android:2.1.30'
```

### Gateway Implementation

```kotlin
// FlutterwaveGateway.kt
class FlutterwaveGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "flutterwave"
    override val displayName = "Flutterwave (Card / Mobile Money / Bank)"
    override val supportedCurrencies = listOf(
        "NGN", "KES", "GHS", "UGX", "TZS", "ZAR", "RWF", "ZMW", "USD", "EUR"
    )
    override val supportedCountries = listOf("NG", "KE", "GH", "UG", "TZ", "ZA", "RW", "ZM")

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        val response = paymentApi.initiateFlutterwavePayment(
            FlutterwavePaymentRequest(
                amount = request.amount,
                currency = request.currency,
                customerEmail = request.userEmail,
                customerName = request.userName,
                txRef = generateTxRef(),
                redirectUrl = "yourbrand://payment/flutterwave",
                meta = mapOf("planId" to request.planId, "userId" to request.userId)
            )
        )
        return PaymentInitResult.WebRedirect(
            url = response.paymentLink,
            reference = response.txRef
        )
    }

    private fun generateTxRef() = "YB-${System.currentTimeMillis()}"
}
```

### Backend — Create Flutterwave Payment Link
```javascript
app.post('/api/payments/initiate-flutterwave', async (req, res) => {
    const { amount, currency, customerEmail, customerName, txRef, redirectUrl, meta } = req.body;
    const response = await axios.post(
        'https://api.flutterwave.com/v3/payments',
        {
            tx_ref: txRef,
            amount,
            currency,
            redirect_url: redirectUrl,
            meta,
            customer: { email: customerEmail, name: customerName },
            customizations: { title: 'YourBrand Subscription', logo: 'https://yourbrand.com/logo.png' }
        },
        { headers: { Authorization: `Bearer ${process.env.FLUTTERWAVE_SECRET_KEY}` } }
    );
    res.json({ paymentLink: response.data.data.link, txRef });
});
```

---

## 10. Airtel Money Integration

Airtel Money covers **East & Central Africa** — Uganda, Kenya, Rwanda, Tanzania, Zambia, Nigeria, Madagascar.

### Flow
Airtel Money uses a **USSD push** flow — the user enters their phone number, receives a USSD prompt on their phone, and enters their PIN. No card details needed.

```kotlin
// AirtelMoneyGateway.kt
class AirtelMoneyGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "airtel"
    override val displayName = "Airtel Money"
    override val supportedCurrencies = listOf("UGX", "KES", "RWF", "TZS", "ZMW", "MWK", "NGN")
    override val supportedCountries = listOf("UG", "KE", "RW", "TZ", "ZM", "MW", "NG")

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        // Phone number required — collected in the UI before calling this
        val response = paymentApi.initiateAirtelPayment(
            AirtelPaymentRequest(
                phoneNumber = request.phoneNumber ?: throw MissingPhoneException(),
                amount = request.amount,
                currency = request.currency,
                reference = "YB-${System.currentTimeMillis()}",
                country = request.country,
                planId = request.planId
            )
        )
        // Airtel returns a transaction ID; user approves via USSD on their phone
        return PaymentInitResult.MobileMoneyPush(
            transactionId = response.transactionId,
            message = "Check your phone for Airtel Money prompt"
        )
    }

    override suspend fun verifyPayment(paymentId: String): PaymentVerifyResult {
        // Poll your backend which checks Airtel transaction status
        val response = paymentApi.verifyAirtelPayment(transactionId = paymentId)
        return when (response.status) {
            "TS" -> PaymentVerifyResult.Success(subscriptionId = response.subscriptionId) // Transaction Successful
            "TF" -> PaymentVerifyResult.Failed("Transaction failed")
            else -> PaymentVerifyResult.Pending
        }
    }
}
```

### Phone Number Collection UI

```kotlin
// MobileMoneyPaymentFragment.kt
@AndroidEntryPoint
class MobileMoneyPaymentFragment : Fragment() {

    fun showPhoneInputDialog(gateway: PaymentGateway, onConfirm: (String) -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${gateway.displayName} Payment")
            .setMessage("Enter your ${gateway.displayName} phone number")
            .setView(R.layout.dialog_phone_input)
            .setPositiveButton("Continue") { dialog, _ ->
                val phoneNumber = (dialog as AlertDialog)
                    .findViewById<TextInputEditText>(R.id.phoneInput)?.text.toString()
                onConfirm(phoneNumber)
            }
            .show()
    }
}
```

### Backend — Airtel Money API
```javascript
// Airtel Money API (Africa's Talking / Airtel direct)
app.post('/api/payments/initiate-airtel', async (req, res) => {
    const { phoneNumber, amount, currency, reference, country } = req.body;

    // Get Airtel access token first
    const tokenResponse = await axios.post(
        'https://openapi.airtel.africa/auth/oauth2/token',
        { client_id: process.env.AIRTEL_CLIENT_ID, client_secret: process.env.AIRTEL_CLIENT_SECRET, grant_type: 'client_credentials' }
    );
    const accessToken = tokenResponse.data.access_token;

    // Initiate collection
    const response = await axios.post(
        `https://openapi.airtel.africa/merchant/v1/payments/`,
        {
            reference,
            subscriber: { country, currency, msisdn: phoneNumber },
            transaction: { amount, country, currency, id: reference }
        },
        {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'X-Country': country,
                'X-Currency': currency
            }
        }
    );
    res.json({ transactionId: response.data.data.transaction.id });
});
```

---

## 11. M-Pesa Integration

M-Pesa (Safaricom Daraja API) is the dominant mobile money platform in **Kenya** and is available in Tanzania, Mozambique, DRC, Ghana, Egypt, Ethiopia, Lesotho.

### STK Push (Lipa Na M-Pesa)

```kotlin
// MpesaGateway.kt
class MpesaGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "mpesa"
    override val displayName = "M-Pesa"
    override val supportedCurrencies = listOf("KES", "TZS", "MZN")
    override val supportedCountries = listOf("KE", "TZ", "MZ", "GH", "EG")

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        val response = paymentApi.initiateMpesaStkPush(
            MpesaStkPushRequest(
                phoneNumber = request.phoneNumber ?: throw MissingPhoneException(),
                amount = request.amount.toInt(),   // M-Pesa requires integer amounts
                accountReference = "YourBrand-${request.planId}",
                transactionDesc = "YourBrand ${request.planId} subscription",
                callbackUrl = "https://yourbackend.com/webhooks/mpesa"
            )
        )
        return PaymentInitResult.MobileMoneyPush(
            transactionId = response.checkoutRequestId,
            message = "Enter your M-Pesa PIN when prompted"
        )
    }

    override suspend fun verifyPayment(paymentId: String): PaymentVerifyResult {
        val response = paymentApi.verifyMpesaPayment(checkoutRequestId = paymentId)
        return when (response.resultCode) {
            "0"  -> PaymentVerifyResult.Success(subscriptionId = response.subscriptionId)
            "1032" -> PaymentVerifyResult.Canceled  // User cancelled
            else -> PaymentVerifyResult.Failed(response.resultDesc)
        }
    }
}
```

### Polling UI for M-Pesa

```kotlin
// MpesaPollingViewModel.kt
@HiltViewModel
class MpesaPollingViewModel @Inject constructor(
    private val mpesaGateway: MpesaGateway
) : ViewModel() {

    private val _state = MutableStateFlow<MpesaState>(MpesaState.WaitingForPin)
    val state: StateFlow<MpesaState> = _state

    fun startPolling(checkoutRequestId: String) {
        viewModelScope.launch {
            repeat(12) { attempt ->   // Poll up to 12 times (60 seconds)
                delay(5_000L)
                val result = mpesaGateway.verifyPayment(checkoutRequestId)
                when (result) {
                    is PaymentVerifyResult.Success -> {
                        _state.value = MpesaState.Success(result.subscriptionId)
                        return@launch
                    }
                    is PaymentVerifyResult.Failed  -> {
                        _state.value = MpesaState.Failed(result.reason)
                        return@launch
                    }
                    is PaymentVerifyResult.Pending -> {
                        if (attempt == 11) _state.value = MpesaState.Timeout
                    }
                    else -> {}
                }
            }
        }
    }
}
```

### Backend — Daraja STK Push
```javascript
app.post('/api/payments/initiate-mpesa', async (req, res) => {
    const { phoneNumber, amount, accountReference, transactionDesc } = req.body;

    // 1. Get access token
    const auth = Buffer.from(`${process.env.MPESA_CONSUMER_KEY}:${process.env.MPESA_CONSUMER_SECRET}`).toString('base64');
    const tokenRes = await axios.get(
        'https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials',
        { headers: { Authorization: `Basic ${auth}` } }
    );
    const accessToken = tokenRes.data.access_token;

    // 2. Generate password: Base64(ShortCode + Passkey + Timestamp)
    const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, 14);
    const password = Buffer.from(`${process.env.MPESA_SHORTCODE}${process.env.MPESA_PASSKEY}${timestamp}`).toString('base64');

    // 3. STK Push
    const response = await axios.post(
        'https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest',
        {
            BusinessShortCode: process.env.MPESA_SHORTCODE,
            Password: password,
            Timestamp: timestamp,
            TransactionType: 'CustomerPayBillOnline',
            Amount: amount,
            PartyA: phoneNumber,   // 254XXXXXXXXX format
            PartyB: process.env.MPESA_SHORTCODE,
            PhoneNumber: phoneNumber,
            CallBackURL: `${process.env.BACKEND_URL}/webhooks/mpesa`,
            AccountReference: accountReference,
            TransactionDesc: transactionDesc
        },
        { headers: { Authorization: `Bearer ${accessToken}` } }
    );

    res.json({ checkoutRequestId: response.data.CheckoutRequestID });
});
```

---

## 12. MTN MoMo Integration

MTN Mobile Money covers **West, Central & East Africa** — Ghana, Uganda, Rwanda, Côte d'Ivoire, Benin, Cameroon, Congo, Zambia.

```kotlin
// MtnMomoGateway.kt
class MtnMomoGateway @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentGateway {
    override val id = "mtn_momo"
    override val displayName = "MTN Mobile Money"
    override val supportedCurrencies = listOf("GHS", "UGX", "RWF", "XOF", "XAF", "ZMW")
    override val supportedCountries = listOf("GH", "UG", "RW", "CI", "BJ", "CM", "CG", "ZM")

    override suspend fun initiatePayment(request: PaymentRequest): PaymentInitResult {
        val response = paymentApi.initiateMtnMomoPayment(
            MtnMomoPaymentRequest(
                externalId = "YB-${System.currentTimeMillis()}",
                amount = request.amount.toString(),
                currency = request.currency,
                partyId = request.phoneNumber ?: throw MissingPhoneException(),
                partyIdType = "MSISDN",
                payerMessage = "YourBrand subscription",
                payeeNote = request.planId
            )
        )
        return PaymentInitResult.MobileMoneyPush(
            transactionId = response.referenceId,
            message = "Approve payment on your MTN MoMo app"
        )
    }

    override suspend fun verifyPayment(paymentId: String): PaymentVerifyResult {
        val response = paymentApi.verifyMtnMomoPayment(referenceId = paymentId)
        return when (response.status) {
            "SUCCESSFUL" -> PaymentVerifyResult.Success(subscriptionId = response.subscriptionId)
            "FAILED"     -> PaymentVerifyResult.Failed(response.reason ?: "Payment failed")
            "PENDING"    -> PaymentVerifyResult.Pending
            else         -> PaymentVerifyResult.Pending
        }
    }
}
```

### Backend — MTN MoMo Collections API
```javascript
const { v4: uuidv4 } = require('uuid');

// Step 1: Create API User (one-time setup in sandbox)
// Step 2: Request to pay
app.post('/api/payments/initiate-mtn', async (req, res) => {
    const { externalId, amount, currency, partyId, partyIdType, payerMessage, payeeNote } = req.body;
    const referenceId = uuidv4();

    // Get MTN MoMo access token
    const tokenRes = await axios.post(
        `${process.env.MTN_BASE_URL}/collection/token/`,
        {},
        {
            headers: {
                Authorization: `Basic ${Buffer.from(`${process.env.MTN_API_USER}:${process.env.MTN_API_KEY}`).toString('base64')}`,
                'Ocp-Apim-Subscription-Key': process.env.MTN_SUBSCRIPTION_KEY
            }
        }
    );
    const accessToken = tokenRes.data.access_token;

    // Request to pay
    await axios.post(
        `${process.env.MTN_BASE_URL}/collection/v1_0/requesttopay`,
        {
            amount,
            currency,
            externalId,
            payer: { partyIdType, partyId },
            payerMessage,
            payeeNote
        },
        {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'X-Reference-Id': referenceId,
                'X-Target-Environment': process.env.MTN_ENVIRONMENT, // sandbox / production
                'Ocp-Apim-Subscription-Key': process.env.MTN_SUBSCRIPTION_KEY,
                'Content-Type': 'application/json'
            }
        }
    );

    res.json({ referenceId });
});
```

---

## 13. Payment Gateway Routing

### Automatic gateway selection by region

```kotlin
// PaymentGatewayRouter.kt
@Singleton
class PaymentGatewayRouter @Inject constructor(
    private val stripeGateway: StripeGateway,
    private val paystackGateway: PaystackGateway,
    private val paddleGateway: PaddleGateway,
    private val flutterwaveGateway: FlutterwaveGateway,
    private val airtelGateway: AirtelMoneyGateway,
    private val mpesaGateway: MpesaGateway,
    private val mtnGateway: MtnMomoGateway
) {
    fun getAvailableGateways(countryCode: String): List<PaymentGateway> = buildList {
        when (countryCode) {
            "KE" -> { add(mpesaGateway); add(airtelGateway); add(flutterwaveGateway); add(stripeGateway) }
            "NG" -> { add(paystackGateway); add(flutterwaveGateway); add(airtelGateway); add(stripeGateway) }
            "GH" -> { add(mtnGateway); add(paystackGateway); add(flutterwaveGateway); add(stripeGateway) }
            "UG" -> { add(mtnGateway); add(airtelGateway); add(flutterwaveGateway); add(stripeGateway) }
            "RW" -> { add(mtnGateway); add(airtelGateway); add(flutterwaveGateway); add(stripeGateway) }
            "TZ" -> { add(mpesaGateway); add(airtelGateway); add(flutterwaveGateway); add(stripeGateway) }
            "ZA" -> { add(paystackGateway); add(stripeGateway); add(flutterwaveGateway) }
            "US", "GB", "EU" -> { add(stripeGateway); add(paddleGateway) }
            else -> { add(stripeGateway); add(paddleGateway); add(flutterwaveGateway) }
        }
    }
}
```

### Payment method selection screen

```kotlin
// PaymentMethodSelectionFragment.kt
@AndroidEntryPoint
class PaymentMethodSelectionFragment : Fragment() {
    @Inject lateinit var router: PaymentGatewayRouter
    private val viewModel: BillingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val countryCode = Locale.getDefault().country
        val gateways = router.getAvailableGateways(countryCode)

        // Build RecyclerView or Compose LazyColumn with gateway options
        // Each item shows gateway name, logo, and accepted payment methods
        adapter.submitList(gateways.map { PaymentMethodItem(it) })
    }
}
```

---

## 14. Subscription State in the App

### SubscriptionManager (Singleton)

```kotlin
// SubscriptionManager.kt
@Singleton
class SubscriptionManager @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val dataStore: DataStore<Preferences>
) {
    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.FREE)
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus

    val currentTier: SubscriptionTier
        get() = _subscriptionStatus.value.tier

    val isActive: Boolean
        get() = _subscriptionStatus.value.isActive

    suspend fun initialize() {
        // Load cached status from DataStore (for offline access)
        val cached = dataStore.data.map { it[SUBSCRIPTION_KEY] }.first()
        if (cached != null) _subscriptionStatus.value = Json.decodeFromString(cached)

        // Refresh from server
        try {
            val fresh = subscriptionRepository.getSubscriptionStatus()
            _subscriptionStatus.value = fresh
            dataStore.edit { it[SUBSCRIPTION_KEY] = Json.encodeToString(fresh) }
        } catch (e: IOException) {
            // Use cached value if offline
        }
    }

    companion object {
        val SUBSCRIPTION_KEY = stringPreferencesKey("subscription_status")
    }
}
```

### Initialise in Application class

```kotlin
@HiltAndroidApp
class YourBrandApplication : Application() {
    @Inject lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            subscriptionManager.initialize()
        }
    }
}
```

---

## 15. Feature Gating

### Checking feature access

```kotlin
// In a Fragment
@AndroidEntryPoint
class AssignmentSubmissionFragment : Fragment() {
    @Inject lateinit var featureGate: FeatureGate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        featureGate.requireFeature(
            feature = Feature.SUBMISSIONS,
            onLocked = { showUpgradePaywall() },
            onUnlocked = { showSubmissionForm() }
        )
    }

    private fun showUpgradePaywall() {
        findNavController().navigate(R.id.action_to_paywall,
            PaywallFragment.args(lockedFeature = Feature.SUBMISSIONS))
    }
}
```

### Paywall screen

```kotlin
// PaywallFragment.kt
@AndroidEntryPoint
class PaywallFragment : Fragment() {

    private val feature: Feature by lazy {
        requireArguments().getSerializable("lockedFeature") as Feature
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.featureTitle.text = feature.displayName
        binding.upgradeButton.setOnClickListener {
            findNavController().navigate(R.id.action_paywall_to_planSelection)
        }
    }
}
```

### Gating in Compose

```kotlin
@Composable
fun FeatureGatedContent(
    feature: Feature,
    featureGate: FeatureGate = hiltViewModel<FeatureGateViewModel>().featureGate,
    lockedContent: @Composable () -> Unit = { UpgradePrompt(feature) },
    unlockedContent: @Composable () -> Unit
) {
    if (featureGate.isEnabled(feature)) unlockedContent() else lockedContent()
}
```

---

## 16. Webhook Handling (Backend)

```javascript
// webhooks/stripe.js
app.post('/webhooks/stripe', express.raw({type: 'application/json'}), async (req, res) => {
    const sig = req.headers['stripe-signature'];
    let event;
    try {
        event = stripe.webhooks.constructEvent(req.body, sig, process.env.STRIPE_WEBHOOK_SECRET);
    } catch (err) {
        return res.status(400).send(`Webhook Error: ${err.message}`);
    }

    switch (event.type) {
        case 'customer.subscription.created':
        case 'customer.subscription.updated':
            await activateSubscription(event.data.object);
            break;
        case 'customer.subscription.deleted':
            await cancelSubscription(event.data.object.metadata.userId);
            break;
        case 'invoice.payment_failed':
            await handlePaymentFailure(event.data.object);
            break;
    }
    res.json({ received: true });
});

// webhooks/mpesa.js
app.post('/webhooks/mpesa', async (req, res) => {
    const { Body: { stkCallback } } = req.body;
    const { CheckoutRequestID, ResultCode, ResultDesc } = stkCallback;

    if (ResultCode === 0) {
        const amount = stkCallback.CallbackMetadata.Item.find(i => i.Name === 'Amount').Value;
        const mpesaRef = stkCallback.CallbackMetadata.Item.find(i => i.Name === 'MpesaReceiptNumber').Value;
        await activateSubscriptionByCheckoutId(CheckoutRequestID, { amount, mpesaRef });
    } else {
        await markPaymentFailed(CheckoutRequestID, ResultDesc);
    }
    res.json({ ResultCode: 0, ResultDesc: 'Success' });
});
```

---

## 17. Free Trial Flow

```kotlin
// In BillingViewModel.kt
fun startFreeTrial(planId: String) {
    viewModelScope.launch {
        val result = subscriptionRepository.createSubscription(
            planId = planId,
            trialDays = 14,
            gateway = null  // No payment method needed for trial
        )
        when (result) {
            is PaymentResult.TrialStarted  -> _state.value = BillingState.TrialActive(result.expiresAt)
            is PaymentResult.Error         -> _state.value = BillingState.Error(result.message)
        }
    }
}
```

### Backend — Trial subscription
```javascript
// For Stripe
const subscription = await stripe.subscriptions.create({
    customer: customerId,
    items: [{ price: priceId }],
    trial_period_days: 14,
    payment_settings: { save_default_payment_method: 'on_subscription' },
    trial_settings: { end_behavior: { missing_payment_method: 'cancel' } }
});
```

---

## 18. Offline Grace Period

Allow users to continue using the app offline for up to 7 days after subscription check:

```kotlin
// SubscriptionManager.kt — grace period logic
val GRACE_PERIOD_DAYS = 7L

fun canAccessFeature(feature: Feature): Boolean {
    val status = _subscriptionStatus.value
    if (status.isActive) return status.tier.features.contains(feature)

    // Offline grace period
    val lastVerified = status.lastVerifiedAt ?: return false
    val gracePeriodEnd = lastVerified.plusDays(GRACE_PERIOD_DAYS)
    val withinGrace = LocalDateTime.now().isBefore(gracePeriodEnd)

    return withinGrace && status.tier.features.contains(feature)
}
```

---

## 19. Hilt DI Wiring

```kotlin
// libs/saas/src/main/java/com/YOURBRAND/saas/di/SaasModule.kt

@Module
@InstallIn(SingletonComponent::class)
object SaasModule {

    @Provides @Singleton
    fun provideSubscriptionApi(retrofit: Retrofit): SubscriptionApi =
        retrofit.create(SubscriptionApi::class.java)

    @Provides @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)

    @Provides @Singleton
    fun provideSubscriptionRepository(
        subscriptionApi: SubscriptionApi,
        paymentApi: PaymentApi
    ): SubscriptionRepository = SubscriptionRepositoryImpl(subscriptionApi, paymentApi)

    @Provides @Singleton
    fun provideSubscriptionManager(
        subscriptionRepository: SubscriptionRepository,
        dataStore: DataStore<Preferences>
    ): SubscriptionManager = SubscriptionManager(subscriptionRepository, dataStore)

    @Provides @Singleton
    fun provideFeatureGate(subscriptionManager: SubscriptionManager): FeatureGate =
        FeatureGate(subscriptionManager)

    @Provides @Singleton
    fun providePaymentGatewayRouter(
        stripeGateway: StripeGateway,
        paystackGateway: PaystackGateway,
        paddleGateway: PaddleGateway,
        flutterwaveGateway: FlutterwaveGateway,
        airtelGateway: AirtelMoneyGateway,
        mpesaGateway: MpesaGateway,
        mtnGateway: MtnMomoGateway
    ): PaymentGatewayRouter = PaymentGatewayRouter(
        stripeGateway, paystackGateway, paddleGateway,
        flutterwaveGateway, airtelGateway, mpesaGateway, mtnGateway
    )
}
```

---

## 20. Testing Payments

### Unit tests

```kotlin
// SubscriptionRepositoryTest.kt
@Test
fun `getSubscriptionStatus returns active when server returns active`() = runTest {
    val mockApi = mockk<SubscriptionApi>()
    coEvery { mockApi.getStatus() } returns SubscriptionStatusResponse(
        tier = "student_pro", status = "active", expiresAt = futureDate
    )
    val repo = SubscriptionRepositoryImpl(mockApi, mockk())
    val status = repo.getSubscriptionStatus()
    assertTrue(status.isActive)
    assertEquals(SubscriptionTier.STUDENT_PRO, status.tier)
}
```

### Test credentials (per gateway)

| Gateway | Test Mode |
|---------|-----------|
| Stripe | Use `pk_test_*` / `sk_test_*` keys; card `4242 4242 4242 4242` |
| Paystack | Use test keys; email `test@paystack.com`; card `4084084084084081` |
| Paddle | Sandbox environment with test credentials |
| Flutterwave | Test mode; card `5531 8866 5214 2950` |
| M-Pesa | Daraja sandbox; test phone `254708374149` |
| Airtel Money | Sandbox environment |
| MTN MoMo | MTN MoMo sandbox; test MSISDN `46733123454` |

### QA Gradle flavor
Add payment test credentials to `qa` productFlavor:
```groovy
qa {
    buildConfigField "String", "STRIPE_TEST_KEY", "\"pk_test_...\""
    buildConfigField "String", "PAYSTACK_TEST_KEY", "\"pk_test_...\""
}
```

---

## 21. Compliance & Security

### PCI DSS
- ✅ **Never** collect raw card numbers in your Android app
- ✅ Use Stripe PaymentSheet (hosted by Stripe — PCI SAQ A compliant)
- ✅ Use Paystack / Flutterwave hosted checkout pages
- ✅ All sensitive payment data flows through your **backend → gateway**

### Private keys
Store all payment gateway **secret keys** server-side only:
```
STRIPE_SECRET_KEY         → backend env var only
PAYSTACK_SECRET_KEY       → backend env var only
FLUTTERWAVE_SECRET_KEY    → backend env var only
MPESA_CONSUMER_SECRET     → backend env var only
MTN_API_KEY               → backend env var only
AIRTEL_CLIENT_SECRET      → backend env var only
```

Only **publishable/public keys** belong in the Android `BuildConfig`.

### Receipt & Invoice
Generate PDF receipts server-side and email to users. Store transaction records in your database.

### GDPR / Data privacy
- Payment data: store only masked card last 4 digits + brand
- Mobile money: store only last 4 digits of phone number
- Full transaction data is held by the gateway — reference by transaction ID only

---

## 22. Implementation Checklist

```
BACKEND
[ ] Set up subscription database tables (users, subscriptions, payments)
[ ] Implement /api/subscriptions/* endpoints
[ ] Implement /api/payments/* endpoints
[ ] Set up Stripe account + webhook endpoint
[ ] Set up Paystack account (for NG/GH/ZA/KE)
[ ] Set up Paddle account (for global SaaS billing)
[ ] Set up Flutterwave account (for Africa)
[ ] Register on Safaricom Daraja (M-Pesa) portal
[ ] Register on Airtel Africa developer portal
[ ] Register on MTN MoMo developer portal
[ ] Implement all webhook handlers
[ ] Set up trial subscription logic

ANDROID
[ ] Create libs/saas module
[ ] Implement SubscriptionTier + Feature enums
[ ] Implement PaymentGateway interface + all 7 implementations
[ ] Implement SubscriptionRepository
[ ] Implement SubscriptionManager (singleton + DataStore caching)
[ ] Implement FeatureGate
[ ] Implement PaymentGatewayRouter (country-based routing)
[ ] Build PaymentMethodSelectionFragment
[ ] Build PaywallFragment
[ ] Build MpesaPollingViewModel + UI
[ ] Build MobileMoneyPaymentFragment (phone input)
[ ] Integrate StripePaymentSheet
[ ] Integrate Paystack/Flutterwave WebView checkout
[ ] Set up deep link callbacks for all gateways
[ ] Add FeatureGate checks to all premium screens
[ ] Add Hilt SaasModule
[ ] Wire SubscriptionManager.initialize() in Application
[ ] Add test credentials to QA flavor
[ ] Write unit tests for SubscriptionRepository
[ ] Write unit tests for FeatureGate
[ ] Manual QA with test credentials for each gateway

STORE & LEGAL
[ ] Update Privacy Policy (subscription data handling)
[ ] Update Terms of Service (subscription terms, refund policy)
[ ] Add subscription info to Play Store listing
[ ] Configure Play Store subscription management (or link to web portal)
```

---

*This guide covers the full payment integration stack. Payment gateway sandbox credentials and API documentation links are available on each provider's developer portal.*

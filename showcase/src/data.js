// All figures and excerpts come from the repository itself (snapshot: Nov 2026).

export const REPO_URL = 'https://github.com/d-ustindwayne/UnyieldingMight';
export const REPO_SLUG = 'd-ustindwayne/UnyieldingMight';

export const rewriteDiff = [
  ['-', 'Java Android app, partially finished'],
  ['-', 'Phone connects straight to SQL Server'],
  ['-', 'DB, mail and payment secrets inside the APK'],
  ['-', 'Fixed phone layout'],
  ['+', 'Kotlin end to end: Compose · Ktor · shared module'],
  ['+', 'REST API with JWT; secrets stay server-side'],
  ['+', 'MySQL 8 schema with constraints + migrations'],
  ['+', 'Adaptive UI: bottom bar → rail → permanent drawer'],
  ['+', '58 tests, CI on every push'],
];

export const modules = [
  {
    id: 'app',
    path: 'app/',
    label: 'Android app',
    stack: ['Jetpack Compose', 'Material 3', 'MVVM', 'Ktor client', 'DataStore', 'Stripe PaymentSheet'],
    notes: [
      'Single activity, type-safe Navigation Compose routes',
      'One ViewModel per screen; small AppContainer for DI',
      'Window size classes drive the navigation shell',
    ],
  },
  {
    id: 'shared',
    path: 'shared/',
    label: 'Shared module',
    stack: ['Pure Kotlin', 'kotlinx.serialization'],
    notes: [
      'Request/response DTOs used by both sides',
      'BMR / TDEE / BMI, MET calorie burner, validation',
      'Rule-based class recommender with explained scores',
    ],
  },
  {
    id: 'server',
    path: 'server/',
    label: 'Ktor API',
    stack: ['Ktor (Netty)', 'JWT + bcrypt', 'HikariCP + JDBC', 'Stripe Java', 'Jakarta Mail'],
    notes: [
      'routes → service → repo layering',
      'PaymentGateway interface: Stripe or mock',
      'Observer pattern: gym events → email notifications',
    ],
  },
  {
    id: 'db',
    path: 'MySQL 8',
    label: 'Database',
    stack: ['MySQL 8 / MariaDB', 'Versioned migrations', 'H2 (MySQL mode) in tests'],
    notes: [
      'Translated from SQL Server and normalised',
      'Demo data generated relative to today',
      'Integrity enforced by constraints, not by the client',
    ],
  },
];

export const problems = [
  {
    title: 'Over-booking under concurrent requests',
    context: 'The original read the count, then wrote it, so two bookings could take the last spot. The check now lives inside one atomic UPDATE.',
    file: 'server/…/repo/Classes.kt',
    code: `fun Tx.reserveSpot(classId: Int, now: Instant = Instant.now()): Boolean = update(
    "UPDATE gym_class SET booked_count = booked_count + 1 " +
    "WHERE id = ? AND status = 'SCHEDULED' " +
    "AND booked_count < max_capacity AND starts_at > ?",
    classId, now,
) == 1`,
  },
  {
    title: 'Never trust the client about money',
    context: 'The app confirms with Stripe, then the server checks the payment with Stripe again. Only the request that wins the state transition fulfils the order, so a retried confirm can’t book twice.',
    file: 'server/…/service/PaymentService.kt',
    code: `GatewayStatus.SUCCEEDED -> {
    // Only the request that wins this transition fulfils.
    if (!db.tx { transitionPayment(paymentId, PENDING, SUCCEEDED) }) {
        return confirm(userId, paymentId)
    }
    try {
        fulfiller.fulfil(payment.copy(status = SUCCEEDED))
    } catch (e: ApiException) {
        refund(payment.copy(status = SUCCEEDED))
        fulfiller.abandon(payment)
    }
}`,
  },
  {
    title: 'Running without third-party keys',
    context: 'Payments sit behind an interface. Without a Stripe key the server swaps in a mock, so the whole flow runs locally and in tests.',
    file: 'server/…/payment/PaymentGateway.kt',
    code: `interface PaymentGateway {
    val mode: PaymentMode
    val publishableKey: String?

    suspend fun createIntent(amountCents: Int, currency: String,
                             description: String, metadata: Map<String, String>): CreatedIntent
    suspend fun status(providerRef: String): GatewayStatus
    suspend fun refund(providerRef: String)
    suspend fun cancel(providerRef: String)
}`,
  },
  {
    title: 'Explainable recommendations',
    context: 'Lives in the shared module, so the logic is unit-tested apart from Android and the server. Every point in a score comes with a reason the user can read.',
    file: 'shared/…/domain/ClassRecommender.kt',
    code: `fun preferredIntensity(profile: Profile): Intensity {
    var level = when (profile.activityLevel) {
        INACTIVE, LIGHT -> 0
        MODERATE -> 1
        HEAVY, EXTREME -> 2
    }
    // Ease off where joint load matters more.
    if (profile.age >= 55) level -= 1
    if (profile.bmi >= 30) level -= 1
    return Intensity.entries[level.coerceIn(0, Intensity.entries.lastIndex)]
}`,
  },
  {
    title: 'One UI, any screen width',
    context: 'Screens didn’t scale in the original. The shell now picks its navigation from the window size class.',
    file: 'app/…/ui/shell/MainShell.kt',
    code: `when (windowSize.widthSizeClass) {
    WindowWidthSizeClass.Expanded -> PermanentNavigationDrawer(/* tablets */) { … }
    WindowWidthSizeClass.Medium   -> ModalNavigationDrawer { Row { NavigationRail { … } } }
    else                          -> Scaffold(bottomBar = { NavigationBar { … } }) { … }
}`,
  },
];

export const metrics = {
  modules: [
    { name: 'app', files: 50, lines: 7391 },
    { name: 'server', files: 38, lines: 4547 },
    { name: 'shared', files: 12, lines: 1057 },
  ],
  tests: 58,
  testKinds: ['shared domain logic', 'API on in-memory H2', 'app units via Ktor MockEngine'],
};

export const screens = [
  { src: 'screens/home.png', label: 'home · recommendations' },
  { src: 'screens/classes.png', label: 'classes' },
  { src: 'screens/class-detail.png', label: 'class detail' },
  { src: 'screens/shop.png', label: 'shop' },
  { src: 'screens/calorie-burner.png', label: 'calorie burner' },
  { src: 'screens/register.png', label: 'registration' },
  { src: 'screens/admin-dashboard.png', label: 'admin' },
  { src: 'screens/tablet-home.png', label: 'tablet · drawer', wide: true },
];

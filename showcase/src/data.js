// Figures and excerpts come from the repository (commit 193c3e2) and the coursework
// documentation in public/. Update them by hand when the code changes.

export const REPO_URL = 'https://github.com/d-ustindwayne/UnyieldingMight';
export const REPO_SLUG = 'd-ustindwayne/UnyieldingMight';
export const DOC_URL = 'SoftwareEngineering_ProjectDocumentation.pdf';

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

/* ---------- 01 process ---------- */

export const process = [
  {
    phase: 'Planning',
    when: 'coursework · group',
    items: [
      'Requirements taken from the fictional client’s business plan',
      'MVC and client-server styles compared in a trade-off table',
      'Class diagram, sequence diagram and ERD in draw.io',
      'Figma prototype for admin, member and non-member flows',
    ],
  },
  {
    phase: 'Sprints',
    when: 'coursework · Scrum',
    items: [
      'Java Android app with XML layouts and activities',
      'Login, registration with email OTP, class booking, admin class editing',
      'JDBC from the phone to a SQL Server hosted by the institute',
    ],
  },
  {
    phase: 'Test and evaluate',
    when: 'coursework',
    items: [
      '12 JUnit tests on models and database calls',
      '3 integration fixes: email check, duplicate bookings, form polish',
      'Maintainability index, cyclomatic complexity, profiler trace',
    ],
  },
  {
    phase: 'Release',
    when: 'June 2026',
    items: [
      'Submitted with the documentation below',
      'Payments and the newsletter were cut to fit the deadline',
    ],
  },
  {
    phase: 'Rewrite',
    when: 'Aug to Oct 2026 · solo',
    items: [
      'Ktor API, shared module and MySQL schema first',
      'Compose app rebuilt on top of the new API',
      'Device testing, then tests, CI and a server Dockerfile',
      'Hardening pass on payments, navigation and auth',
    ],
  },
  {
    phase: 'Next',
    when: 'planned',
    items: ['Host the API and database (Aiven MySQL)', 'Release APK on GitHub Releases'],
    pending: true,
  },
];

/* ---------- 02 requirements ---------- */

export const requirements = [
  { group: 'Customer', name: 'Registration', now: 'Email OTP, address check via QuickEmailVerification, JWT sessions' },
  { group: 'Customer', name: 'Booking without conflicts', now: 'UNIQUE (user, class) plus an atomic capacity update' },
  { group: 'Customer', name: 'Search by class or trainer', now: 'Filters on class name, description and trainer' },
  { group: 'Customer', name: 'Notifications', now: 'Emails for booking, cancellation, finished class and paid order' },
  { group: 'Customer', name: 'Payment for non-members', now: 'Cut in the coursework. Now Stripe PaymentIntents (test mode) with refunds' },
  { group: 'Admin', name: 'Customer management', now: 'User list and role changes' },
  { group: 'Admin', name: 'Booking management', now: 'Attendee list per class; cancelling a class refunds paid bookings' },
  { group: 'Admin', name: 'Class management', now: 'Create, edit, cancel and delete classes' },
  { group: 'Non-functional', name: 'Security', now: 'bcrypt replaces SHA-256; no credentials in the APK' },
  { group: 'Non-functional', name: 'Reliability', now: 'Rules enforced by DB constraints; server re-checks payments' },
  { group: 'Non-functional', name: 'Performance (150 ms)', now: 'Not measured in the rewrite yet. bcrypt runs off the request threads' },
  { group: 'Non-functional', name: 'Usability', now: 'Navigation adapts to phones, foldables and tablets' },
];

/* ---------- 03 architecture ---------- */

export const decision = {
  options: [
    { name: 'MVC', pro: 'Layers test well in isolation', con: 'All logic runs on the device' },
    { name: 'CSM, 2-tier', pro: 'Lightweight client, no extra infrastructure', con: 'Client talks to the database directly' },
    { name: 'CSM, 3-tier', pro: 'Business rules and secrets live on a server', con: 'API and DB must be deployed separately' },
  ],
  then: 'Coursework chose 2-tier CSM, with MVC inside the app.',
  now: 'The rewrite moves to 3-tier. With 2-tier, the database login had to ship inside the APK, so anyone could extract it.',
};

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
      'Integrity enforced by constraints in the schema',
    ],
  },
];

/* ---------- 04 patterns ---------- */

export const patterns = [
  {
    name: 'Builder',
    kind: 'creational',
    then: 'Builder classes for Customer, GymClass, Trainer, Profile, Address and GymBooking.',
    now: 'Kotlin data classes with named and default arguments do the same job with less code.',
  },
  {
    name: 'Observer',
    kind: 'behavioural',
    then: 'Subject and Observer classes, used only for the booking confirmation email.',
    now: 'NotificationCenter publishes GymEvents. EmailObserver sends five kinds of email, and push can subscribe later.',
  },
  {
    name: 'Singleton',
    kind: 'creational',
    then: 'One shared Database instance across all activities.',
    now: 'AppContainer on the phone and a Services container on the server, each created once. A Hikari pool replaces the single connection.',
  },
  {
    name: 'Adapter',
    kind: 'structural',
    then: 'RecyclerView Adapter and ViewHolder for the class cards.',
    now: 'Compose lazy lists replace the list adapter. StripeGateway adapts Stripe’s SDK to the PaymentGateway interface.',
  },
];

/* ---------- 05 problems ---------- */

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
    title: 'Duplicate bookings from repeated taps',
    context: 'Found during coursework integration testing: tapping Book several times stored several rows. The fix then was a SELECT before each insert. The rewrite puts the rule in the schema.',
    file: 'server/…/db/migration/V1__schema.sql',
    code: `CREATE TABLE class_booking (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    class_id INT NOT NULL,
    ...
    CONSTRAINT uq_class_booking UNIQUE (user_id, class_id),`,
  },
  {
    title: 'Server-side payment checks',
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
    title: 'University emails rejected at sign-up',
    context: 'Also from coursework testing: the verification API marked a real university address as unsafe to send. The rule now blocks only invalid or disposable addresses, runs on the server so the key stays private, and lets sign-up continue if the service is down.',
    file: 'server/…/service/EmailVerifier.kt',
    code: `suspend fun isUndeliverable(email: String): Boolean = runCatching {
    val response = http.get("https://api.quickemailverification.com/v1/verify") { … }
    if (!response.status.isSuccess()) return false
    val body = response.body<Response>()
    body.result == "invalid" || body.disposable == "true"
}.getOrDefault(false)`,
  },
  {
    title: 'Running without third-party keys',
    context: 'Payments sit behind an interface. Without a Stripe key the server swaps in a mock, so the whole flow runs locally and in tests. The coursework metrics flagged the lack of interfaces as a risk.',
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

/* ---------- 06 quality ---------- */

export const courseworkMetrics = [
  { label: 'Lines of code', value: '2,020', note: 'Java, MetricsTree' },
  { label: 'Maintainability index', value: '75.00', note: 'good band is 65 to 100' },
  { label: 'Cyclomatic complexity', value: '1.58', note: 'average v(G); 406 total' },
  { label: 'Classes', value: '45', note: '1 abstract, 2 interfaces' },
  { label: 'Tests', value: '12 + 3', note: 'unit tests + integration fixes' },
];

export const lessons = [
  { from: 'Few interfaces made mocking hard', to: 'Interfaces at the edges (PaymentGateway, EmailSender), H2 and MockEngine in tests' },
  { from: 'Low comment count', to: 'Comments explain decisions and edge cases' },
  { from: 'Dense methods, high Halstead volume', to: 'routes, service and repo layers with small functions' },
  { from: 'Integration testing done by hand', to: 'API tests on every push in GitHub Actions' },
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

/* ---------- 07 stack ---------- */

export const stack = [
  { layer: 'Android app', items: ['Kotlin', 'Jetpack Compose', 'Material 3', 'Navigation Compose', 'Ktor client', 'kotlinx.serialization', 'DataStore', 'Coil', 'Stripe PaymentSheet'] },
  { layer: 'API server', items: ['Kotlin', 'Ktor (Netty)', 'JWT', 'bcrypt', 'HikariCP', 'JDBC', 'Jakarta Mail', 'Stripe Java', 'NewsAPI', 'QuickEmailVerification'] },
  { layer: 'Shared', items: ['Pure Kotlin', 'DTOs', 'Fitness maths', 'Recommender', 'Validation'] },
  { layer: 'Database', items: ['MySQL 8', 'MariaDB (local)', 'Versioned SQL migrations'] },
  { layer: 'Testing and CI', items: ['kotlin.test', 'JUnit', 'Ktor test host', 'H2 (MySQL mode)', 'Ktor MockEngine', 'GitHub Actions'] },
  { layer: 'Build and ops', items: ['Gradle Kotlin DSL', 'Version catalog', 'AGP 9', 'Docker', 'Docker Compose'] },
  { layer: 'Coursework tools', items: ['Java', 'Android Studio', 'SQL Server', 'Figma', 'draw.io', 'MetricsTree', 'MetricReloaded'] },
];

/* ---------- 08 screens ---------- */

export const screens = [
  { src: 'screens/home.png', label: 'home · recommendations' },
  { src: 'screens/classes.png', label: 'classes' },
  { src: 'screens/class-detail.png', label: 'class detail' },
  { src: 'screens/shop.png', label: 'shop' },
  { src: 'screens/calorie-burner.png', label: 'calorie burner' },
  { src: 'screens/register.png', label: 'registration' },
  { src: 'screens/admin-dashboard.png', label: 'admin' },
  { src: 'screens/tablet-home.png', label: 'tablet · drawer' },
];

/* ---------- 09 docs ---------- */

export const documentation = {
  title: 'Software Engineering Project Documentation',
  course: '502.633 Software Engineering · Manukau Institute of Technology',
  meta: '103 pages · PDF · 17.5 MB',
  chapters: [
    ['1.0', 'Proposal: requirements, architecture styles, UML, design patterns, quality attributes'],
    ['2.0', 'Development: Scrum process, Figma prototype, pattern implementation, code quality'],
    ['3.0', 'Testing: unit tests, integration tests, quality attribute evaluation'],
    ['4.0', 'Metrics: lines of code, maintainability index, cyclomatic complexity'],
  ],
};

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.unyieldingmight.server.ApplicationKt")
}

// `./gradlew :server:run` runs from the repository root so local.properties is found.
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.logback)
    implementation(libs.hikari)
    implementation(libs.mariadb.jdbc)
    implementation(libs.mysql.jdbc)
    implementation(libs.bcrypt)
    implementation(libs.angus.mail)
    implementation(libs.stripe.java)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.h2)
    testImplementation(libs.kotlinx.coroutines.test)
}

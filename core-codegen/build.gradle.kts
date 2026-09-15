plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Pure Domain AST dependency
    implementation(project(":core-domain"))

    // Kotlin code generation
    implementation(libs.kotlinpoet)

    // Unit Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.google.services)
}

dependencies {
    implementation(projects.shared)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.compose.ui.tooling.preview)
}

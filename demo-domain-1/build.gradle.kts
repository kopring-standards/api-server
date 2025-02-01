plugins {
    id("demo-dependency-manager")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${libs.versions.opentelemetryBom.get()}")
    }
}

dependencies {
    api(project(":demo-common"))
//    implementation(libs.bundles.web)
//    implementation(libs.bundles.service)
//    implementation(libs.bundles.persistence)
//    implementation(libs.bundles.cache)
//    implementation(libs.bundles.storage)
//    implementation(libs.bundles.dataStreaming)
//    implementation(libs.bundles.cloudNative)
//    implementation(libs.bundles.common)
//    developmentOnly(libs.bundles.devTools)
//    implementation(libs.bundles.test)
}

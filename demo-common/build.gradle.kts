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
    api(libs.bundles.web)
    api(libs.bundles.service)
    api(libs.bundles.persistence)
    api(libs.bundles.cache)
    api(libs.bundles.storage)
    api(libs.bundles.dataStreaming)
    // api(libs.bundles.cloudNative)
    api(libs.bundles.common)
    api(libs.bundles.test)
    developmentOnly(libs.bundles.devTools)
}

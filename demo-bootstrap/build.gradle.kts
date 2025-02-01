plugins {
    application
    id("demo-dependency-manager")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${libs.versions.opentelemetryBom.get()}")
    }
}

dependencies {
    api(project(":demo-domain-1"))
    api(project(":demo-domain-2"))
}

tasks {
    ktlint {
        filter {
            exclude { it.file.path.contains("generated/") }
        }
        ignoreFailures.set(false)
    }
}

tasks {
    bootJar {
        enabled = true
    }
}

tasks {
    bootJar {
        enabled = true
    }

    named("bootJar") {
        dependsOn("ktlintFormat")
    }

    getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        mainClass.set("com.demo.bootstrap.DemoBootstrapApplicationKt")
    }

    getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
        imageName.set("${System.getenv("NCP_CONTAINER_REGISTRY")}/piikii")
        environment.set(
            mapOf(
                "BP_JVM_VERSION" to "21",
                "BPE_SPRING_PROFILES_ACTIVE" to "prod",
                "BPE_JAVA_TOOL_OPTIONS" to
                    buildString {
                        append("-XX:+UseG1GC ")
                        append("-XX:+UseContainerSupport ")
                        append("-Xms2g -Xmx2g ")
                        append("-XX:+HeapDumpOnOutOfMemoryError ")
                        append("-XX:HeapDumpPath=/root/heapDump/%Y%m%d_%H%M%S.hprof ")
                        append("-XX:+UseStringDeduplication ")
                        append("-XX:+ExitOnOutOfMemoryError ")
                        append("-Dfile.encoding=UTF-8")
                    },
            ),
        )
        docker {
            publishRegistry {
                url.set(System.getenv("NCP_CONTAINER_REGISTRY"))
                username.set(System.getenv("NCP_API_ACCESS_KEY"))
                password.set(System.getenv("NCP_API_SECRET_KEY"))
            }
        }
        publish.set(true)
    }
}

springBoot {
    buildInfo()
}

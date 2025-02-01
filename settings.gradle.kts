pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "demo"
include("demo-common")
include("demo-domain-1")
include("demo-domain-2")
include("demo-bootstrap")

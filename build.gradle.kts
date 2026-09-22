plugins {
    id("java")
}

group = "io.mikaple"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
}

tasks.test {
    useJUnitPlatform()
}
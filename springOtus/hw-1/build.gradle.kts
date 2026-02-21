plugins {
    java
}

group = "ru.otus"

val springVersion = "7.0.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:$springVersion")
    implementation("org.springframework:spring-beans:$springVersion")
    implementation("org.springframework:spring-core:$springVersion")
    implementation("org.springframework:spring-expression:$springVersion")
    implementation("org.springframework:spring-aop:$springVersion")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.opencsv:opencsv:5.12.0")

    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
    id("io.micronaut.aot") version "4.4.2"
}

version = "0.1"
group = "guild.tracker"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.micronaut:micronaut-bom:3.10.4"))
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.aws:micronaut-aws-lambda-events-serde")
    implementation("io.micronaut.crac:micronaut-crac")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut:micronaut-http-client-jdk")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.micronaut:micronaut-http-client-jdk")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
}



application {
    mainClass = "guild.tracker.ApplicationKt"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
}


//graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("guild.tracker.*")
    }
}



tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    baseImage = "amazonlinux:2023"
    jdkVersion = "21"
    args(
        "-XX:MaximumHeapSizePercent=80",
        "-Dio.netty.allocator.numDirectArenas=0",
        "-Dio.netty.noPreferDirect=true"
    )
}

tasks {
    shadowJar {
        mergeServiceFiles()
        transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java)
        transform(com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
}






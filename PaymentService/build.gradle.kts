plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.grpc:grpc-netty-shaded:1.57.2") // gRPC transport
    implementation("io.grpc:grpc-protobuf:1.57.2") // Protobuf support for gRPC
    implementation("io.grpc:grpc-stub:1.57.2") // gRPC stubs
    implementation("com.google.protobuf:protobuf-java:3.24.3") // Protobuf Java classes
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.3" // Protobuf compiler version
    }
    plugins {
        // Define the gRPC plugin with the correct syntax
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.57.2"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                // Apply the gRPC plugin
                create("grpc")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

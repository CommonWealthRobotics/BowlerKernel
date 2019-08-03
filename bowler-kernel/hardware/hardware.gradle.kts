plugins {
    `java-library`
}

description = "Controlled access to hardware resources."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(
        group = "org.kohsuke",
        name = "github-api",
        version = property("github-api.version") as String
    )
    api(group = "com.neuronrobotics", name = "SimplePacketComsJava", version = "0.9.2")
    api(group = "com.neuronrobotics", name = "SimplePacketComsJava-HID", version = "0.9.3")
    api("org.hid4java:hid4java:0.5.0")
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )

    api(project(":bowler-kernel:util"))

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))

    implementation(project(":bowler-kernel:logging"))

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = property("mockito-kotlin.version") as String
    )
}

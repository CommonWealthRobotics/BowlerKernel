description = "The core CAD module."

dependencies {
    api(project(":bowler-kernel:kinematics"))

    api(
        group = "com.neuronrobotics",
        name = "JavaCad",
        version = property("javacad.version") as String
    ) {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}

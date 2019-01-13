import GitFS_gradle.Verions.arrow_version

plugins {
    `java-library`
}

description = "An interface to a Git-based filesystem."

object Verions {
    const val arrow_version = "0.8.1"
}

dependencies {
    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    api(group = "org.kohsuke", name = "github-api", version = "1.95")

    implementation(project(":BowlerKernel:Util"))
    implementation(project(":BowlerKernel:Settings"))
    implementation(project(":BowlerKernel:Logging"))
    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )
}

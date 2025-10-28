plugins {
    id("atelier-validator.base")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()

    // signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name.set(project.name)
        description.set("Atelier Validator - ${project.name}")
        inceptionYear.set("2025")
        url.set("https://megatilus.dev")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("megatilus")
                name.set("Megatilus")
                url.set("https://github.com/megatilus")
                email.set("megatilus@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/megatilus/atelier-validator")
            connection.set("scm:git:git://github.com/megatilus/atelier-validator.git")
            developerConnection.set("scm:git:ssh://git@github.com/megatilus/atelier-validator.git")
        }
    }
}

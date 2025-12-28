tasks.register("testAll") {
    group = "verification"
    description = "Run all tests from all modules for all platforms"

    dependsOn(
        subprojects.flatMap { project ->
            project.tasks.matching { it.name.endsWith("Test") }
        }
    )
}

tasks.register("compileAllTests") {
    group = "verification"
    description = "Compile all tests from all modules for all platforms"

    dependsOn(
        subprojects.flatMap { project ->
            project.tasks.matching { it.name.startsWith("compileTestKotlin") }
        }
    )
}

tasks.register("checkBeforePublish") {
    group = "verification"
    description = "Compile and run all tests before publishing"

    dependsOn("compileAllTests", "testAll")
}

tasks.register("testAll") {
    group = "verification"
    description = "Exécute tous les tests de tous les modules pour toutes les plateformes"

    subprojects.forEach { p ->
        p.tasks.matching { it.name.endsWith("Test") }.forEach { t ->
            dependsOn(t)
        }
    }
}

tasks.register("compileAllTests") {
    group = "verification"
    description = "Compile tous les tests de tous les modules pour toutes les plateformes"

    subprojects.forEach { p ->
        p.tasks.matching { it.name.startsWith("compileTestKotlin") }.forEach { t ->
            dependsOn(t)
        }
    }
}

tasks.register("checkBeforePublish") {
    group = "verification"
    description = "Compile et exécute tous les tests avant publication"

    dependsOn("compileAllTests")
    finalizedBy("testAll")
}

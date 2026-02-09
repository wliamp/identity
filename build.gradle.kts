import java.lang.System.getenv

val tld: String? = getenv("TLD")
val org: String? = getenv("ORG")
val id: String? = getenv("ID")
val repo: String? = getenv("REPO")
val actor: String? = getenv("ACTOR")
val token: String? = getenv("TOKEN")

plugins {
    java
    `maven-publish`
}

group = "$tld.$org.rest"
version = getenv("TAG") ?: ""

publishing {
    publications {
        create<MavenPublication>("restModel") {
            from(components["java"])
            artifactId = "$id-model"
            pom {
                name.set(artifactId)
                description.set("Reusable REST API Models Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("restApi") {
            from(components["java"])
            artifactId = "$id-api"
            pom {
                name.set(artifactId)
                description.set("Reusable REST APIs Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("restClient") {
            from(components["java"])
            artifactId = "$id-client"
            pom {
                name.set(artifactId)
                description.set("Reusable REST API Clients Specification")
                url.set("https://github.com/$org/$repo")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$org/$repo")
            credentials {
                username = actor
                password = token
            }
        }
    }
}

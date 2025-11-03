import java.lang.System.getenv

plugins {
    id("java")
    id("maven-publish")
}

group = "org.hamsaqua"
version = getenv("VERSION") ?: "SNAPSHOT"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = getenv("ARTIFACT_ID")
            pom {
                name.set(artifactId)
                description.set("Internal API Contracts")
                url.set("https://github.com/hamsaqua/api")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/hamsaqua/api")
            credentials {
                username = getenv("GITHUB_ACTOR") ?: ""
                password = getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

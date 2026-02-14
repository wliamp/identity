import java.lang.System.getenv

rootProject.name = "identity"

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    include(it)
} ?: include(
    "core",
)
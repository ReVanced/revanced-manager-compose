package app.revanced.manager.compose.util.signing

import java.nio.file.Path

data class SigningOptions(
    val cn: String,
    val password: String,
    val keyStoreFilePath: Path
)
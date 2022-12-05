package eu.livesport.kotlintest

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey


@NonNls
private const val BUNDLE = "messages.default"

object Bundle : DynamicBundle(BUNDLE) {

    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return getMessage(key, *params)
    }
}
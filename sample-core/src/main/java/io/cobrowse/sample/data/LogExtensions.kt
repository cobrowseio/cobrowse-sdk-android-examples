package io.cobrowse.sample.data

fun <T : Any> Class<T>.getAndroidLogTag(): String {
    val tag = this.simpleName
    return if (tag.length <= 23) tag else tag.substring(0, 23)
}
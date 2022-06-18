package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.result.ActivityResultLauncher

/**
 * A short-hand utility for launching multiple requests with variable arguments support.
 *
 * @param input Input string
 */
fun ActivityResultLauncher<Array<String>>.launch(vararg input: String) =
    launch(input.toList().toTypedArray())

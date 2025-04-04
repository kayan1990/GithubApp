package com.kayan.githubapp.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun LinkText(
    text: String,
    modifier: Modifier = Modifier,
    textDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit) {

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        textDecoration = textDecoration,
        fontWeight = fontWeight,
        modifier = modifier.noRippleClickable(onClick = onClick)
    )
}
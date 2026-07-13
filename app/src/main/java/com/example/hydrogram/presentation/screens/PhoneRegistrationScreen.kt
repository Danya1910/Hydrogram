package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Separator
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun PhoneRegistrationScreen() {
    Scaffold(
        topBar = {},
    ) { paddingValues ->
        Content(
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    paddingValues: PaddingValues,
) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
            .padding(
                horizontal = 16.dp,
                vertical = 26.dp,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.red_vintage_phone),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(200.dp)
            )
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Телефон",
                fontWeight = FontWeight.Bold,
                fontFamily = SfProText,
                fontSize = 26.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Введите свой номер телефона",
                fontWeight = FontWeight.Normal,
                fontFamily = SfProText,
                fontSize = 20.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(16.dp))
            InputNumberField()
        }

        AcceptButton(
            isAvailable = false,
            onClick = {},
        )
    }
}

@Composable
private fun InputNumberField(

) {
    var number by remember { mutableStateOf("999-999-9999") }
    val maxPhoneLength = 10


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        SeparatorLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "+7",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 25.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(
                        color = Separator,
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = number,
                onValueChange = { it ->
                    if (it.length <= maxPhoneLength && it.all { it.isDigit() }) {
                        number = it
                    }
                },
                textStyle = TextStyle(
                    fontFamily = SfProText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 25.sp,
                    color = Color.Black
                ),
                //visualTransformation = PhoneDashMaskTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.weight(1f)
            )
        }
        SeparatorLine(
            modifier = Modifier
                .padding(top = 3.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun AcceptButton(
    isAvailable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = if (isAvailable) Blue else Separator,
                shape = CircleShape
            )
            .border(
                color = if (isAvailable) Blue else Separator,
                width = 1.dp,
                shape = CircleShape,
            )
    ) {
        Text(
            text = "Применить",
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = if (isAvailable) Color.White else Color.Black,
        )
    }

}

class PhoneDashMaskTransformation(
    private val hintColor: Color = Color.Gray.copy(alpha = 0.4f)
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val rawInput = text.text
        val out = StringBuilder()

        // Массив индексов, где в исходном тексте (оригинальном) ставились бы дефисы
        // Это нужно для правильного расчета позиции курсора
        val targetMask = "000-000-00-00"

        // 1. Форматируем то, что уже ввел пользователь
        for (i in rawInput.indices) {
            out.append(rawInput[i])
            if (i == 2 || i == 5 || i == 7) {
                if (i != rawInput.lastIndex) {
                    out.append("-")
                }
            }
        }
        val realTextLength = out.length

        // 2. Дописываем серую подсказку с дефисами, если текст введен не до конца
        if (rawInput.length < 10) {
            // Если последним символом случайно оказался дефис из-за логики выше, убираем дублирование
            if (out.isNotEmpty() && (rawInput.length == 3 || rawInput.length == 6 || rawInput.length == 8)) {
                out.append("-")
            }

            // Вычисляем, какую часть маски-подсказки нужно дорисовать
            val currentFormattedLength = out.length
            val remainingMask = targetMask.substring(currentFormattedLength)

            // Собираем финальную строку с разделением по цветам
            val annotatedStringBuilder = AnnotatedString.Builder()
            annotatedStringBuilder.append(out.toString().substring(0, currentFormattedLength))
            annotatedStringBuilder.pushStyle(SpanStyle(color = hintColor))
            annotatedStringBuilder.append(remainingMask)
            annotatedStringBuilder.pop()

            return TransformedText(
                annotatedStringBuilder.toAnnotatedString(),
                getOffsetMapping(rawInput)
            )
        }

        return TransformedText(AnnotatedString(out.toString()), getOffsetMapping(rawInput))
    }

    // Рассчитываем, как индексы "чистых" цифр соотносятся с индексами строки, где есть дефисы
    private fun getOffsetMapping(rawInput: String) = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 6) return offset + 1
            if (offset <= 8) return offset + 2
            return offset + 3
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 7) return offset - 1
            if (offset <= 10) return offset - 2
            if (offset <= 13) return offset - 3
            return rawInput.length
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PhoneRegistrationScreenPreview() {
    PhoneRegistrationScreen()
}
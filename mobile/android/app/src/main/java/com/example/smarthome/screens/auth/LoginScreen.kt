package com.example.smarthome.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.R
import com.example.smarthome.ui.theme.UjinDarkGray
import com.example.smarthome.ui.theme.UjinLightGray

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onCodeSent: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = UjinDarkGray
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_ujin),
            contentDescription = "Ujin Logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(180.dp)
                .wrapContentHeight()
                .scale(2f)
)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Введите номер телефона",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Поле ввода телефона
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Префикс +7
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .background(UjinLightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+7", color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            // поле
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(UjinLightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    decorationBox = { innerTextField ->
                        if (phoneNumber.isEmpty()) {
                            Text("999-000-00-00", color = Color.LightGray)
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Галочка согласия
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = isAgreed,
                onCheckedChange = { isAgreed = it },
                colors = CheckboxDefaults.colors(checkedColor = UjinDarkGray)
            )
            Text(
                text = "Нажимая на кнопку «Получить код» вы соглашаетесь с Политикой обработки персональных данных",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка
        Button(
            onClick = onCodeSent,
            enabled = isAgreed && phoneNumber.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UjinDarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Получить код", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Текстовая кнопка назад
        Row(
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = UjinDarkGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Вернуться", color = UjinDarkGray)
        }
    }
}
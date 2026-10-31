package com.example.smarthome.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.R
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinDarkGray
import androidx.compose.ui.draw.scale

@Composable
fun SplashScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ВЕРХНЯЯ ЧАСТЬ: Картинка с логотипом и текстом
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            // 1. Фоновая картинка
            Image(
                painter = painterResource(id = R.drawable.splash_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Логотип и текст поверх картинки
            Column(
                // Отступы из Figma: Top = 74, Left = 25
                modifier = Modifier
                    .padding(top = 74.dp, start = 25.dp, end = 25.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.logo_ujin),
                    contentDescription = "Ujin Logo",
                    modifier = Modifier
                        .width(180.dp)
                        .wrapContentHeight()
                        .scale(2f)
                )

                // Отступ между лого и текстом (подберите на глаз, если в Figma не указано)
                Spacer(modifier = Modifier.height(24.dp))

                // Текст под логотипом
                Text(
                    text = "Технологии\nдля комфортной\nжизни",
                    color = Color.Black,
                    fontSize = 24.sp, // Если в Figma другой размер, поменяйте
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp // Межстрочный интервал
                )
            }
        }

        // НИЖНЯЯ ЧАСТЬ: Кнопки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                // Отступы кнопок из Figma: 16px по бокам (я поставил 20dp для красоты)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // В Figma кнопки обычно ~50-56px
                colors = ButtonDefaults.buttonColors(containerColor = UjinCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Войти", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = UjinDarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Зарегистрироваться", fontSize = 16.sp)
            }
        }
    }
}
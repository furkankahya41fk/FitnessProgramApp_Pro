
package com.furkan.fitnessapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.furkan.fitnessapp.alarm.AlarmReceiver
import com.furkan.fitnessapp.data.Prefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Beslenme", "Uyum", "Kardiyo")
    val icons = listOf(Icons.Default.Restaurant, Icons.Default.Favorite, Icons.Default.Favorite)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fitness Program") },
                actions = {
                    IconButton(onClick = { SettingsDialog() }) {}
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(text = { Text("Ayarlar") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                onClick = { SettingsDialog() }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t) })
                }
            }
            when (selectedTab) {
                0 -> NutritionTab()
                1 -> SyncTab()
                2 -> CardioTab()
            }
        }
    }
}

@Composable
fun SettingsDialog() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    var protein by remember { mutableStateOf(prefs.getInt("protein", 130)) }
    var su by remember { mutableStateOf(prefs.getInt("su", 3000)) }
    var porsiyon by remember { mutableStateOf(prefs.getInt("porsiyon", 100)) }

    AlertDialog(
        onDismissRequest = { },
        title = { Text("Kişisel Ayarlar") },
        text = {
            Column {
                SettingSlider("Günlük Protein g", protein, 60..220) { protein = it }
                SettingSlider("Günlük Su ml", su, 1500..5000) { su = it }
                SettingSlider("Porsiyon %", porsiyon, 50..150) { porsiyon = it }
                Spacer(Modifier.height(8.dp))
                Text("Hatırlatıcılar kurulduktan sonra sistem saatine göre çalışır")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                prefs.putInt("protein", protein)
                prefs.putInt("su", su)
                prefs.putInt("porsiyon", porsiyon)
                // planlı alarmlar
                AlarmReceiver.scheduleAll(context)
            }) { Text("Kaydet") }
        }
    )
}

@Composable
fun SettingSlider(label: String, value: Int, range: IntRange, onChange: (Int)->Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("$label  $value", fontWeight = FontWeight.SemiBold)
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )
    }
}

@Composable
fun SectionHeader(t: String) {
    Text(t, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun CardBlock(title: String, body: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun NutritionTab() {
    val days = listOf(
        DayPlan("Pazartesi Spor", SampleData.ogun1_spor, SampleData.ogun2_spor),
        DayPlan("Salı", SampleData.ogun1_normal, SampleData.ogun2_normal),
        DayPlan("Çarşamba Spor", SampleData.ogun1_spor, SampleData.ogun2_spor),
        DayPlan("Perşembe", SampleData.ogun1_normal, SampleData.ogun2_normal),
        DayPlan("Cuma Spor", SampleData.ogun1_spor, SampleData.ogun2_spor),
        DayPlan("Cumartesi", SampleData.ogun1_normal, SampleData.ogun2_normal),
        DayPlan("Pazar", SampleData.ogun1_normal, SampleData.ogun2_normal),
    )
    LazyColumn(Modifier.padding(12.dp)) {
        item { SectionHeader("Haftalık 2 Öğün Beslenme Programı") }
        items(days) { d ->
            CardBlock(d.gun, "Sabah\n${d.sabah}\n\nAkşam\n${d.aksam}")
        }
        item {
            CardBlock("Günlük Ortalama",
                "Spor Günleri 100g Protein 60g Karb 35g Yağ 1000 kcal 2 öğün\n" +
                "Normal Günler 90g Protein 35g Karb 32g Yağ 850 kcal 2 öğün")
        }
    }
}

@Composable
fun SyncTab() {
    val rows = listOf(
        Uyum("Pazartesi Spor","Göğüs Omuz Triceps", SampleData.ogun1_spor, SampleData.ogun2_spor),
        Uyum("Salı Dinlenme","Hafif kardiyo yürüyüş esneme", SampleData.ogun1_normal, SampleData.ogun2_normal),
        Uyum("Çarşamba Spor","Sırt Biceps", SampleData.ogun1_spor, SampleData.ogun2_spor),
        Uyum("Perşembe Dinlenme","Hafif kardiyo yürüyüş esneme", SampleData.ogun1_normal, SampleData.ogun2_normal),
        Uyum("Cuma Spor","Bacak Karın", SampleData.ogun1_spor, SampleData.ogun2_spor),
        Uyum("Cumartesi Dinlenme","Hafif kardiyo yürüyüş esneme", SampleData.ogun1_normal, SampleData.ogun2_normal),
        Uyum("Pazar Dinlenme","Hafif kardiyo yürüyüş esneme", SampleData.ogun1_normal, SampleData.ogun2_normal),
    )
    LazyColumn(Modifier.padding(12.dp)) {
        item { SectionHeader("Antrenman ve Beslenme Uyum Tablosu") }
        items(rows) { r ->
            CardBlock(r.gun, "Antrenman\n${r.antrenman}\n\nÖncesi\n${r.oncesi}\n\nSonrası\n${r.sonrasi}")
        }
    }
}

@Composable
fun CardioTab() {
    val prog = listOf(
        Triple("Pazartesi Spor","Ağırlık Göğüs Omuz Triceps","Ağırlık sonrası 10 dk LISS hızlı yürüyüş"),
        Triple("Salı Dinlenme","Hafif kardiyo","Sabah aç karnına 30 dk yürüyüş"),
        Triple("Çarşamba Spor","Ağırlık Sırt Biceps","Ağırlık sonrası 10 dk HIIT 30 sn hızlı 60 sn yavaş 10 tekrar"),
        Triple("Perşembe Dinlenme","Hafif kardiyo","20 dk bisiklet LISS tempo"),
        Triple("Cuma Spor","Ağırlık Bacak Karın","Ağırlık sonrası 10 dk LISS bisiklet veya yürüyüş"),
        Triple("Cumartesi Dinlenme","HIIT","15 dk interval kardiyo 30 sn hızlı 60 sn yavaş"),
        Triple("Pazar Dinlenme","Aktif dinlenme","30 dk yürüyüş isteğe bağlı"),
    )
    LazyColumn(Modifier.padding(12.dp)) {
        item { SectionHeader("Yağ Yakımı İçin Kardiyo Programı") }
        items(prog) { (g,a,k) ->
            CardBlock(g, "Antrenman\n${a}\n\nKardiyo\n${k}")
        }
        item {
            CardBlock("Not",
                "HIIT haftada 2 3 kez yapılmalı Kas korunması için HIIT günlerinde protein yüksek tutulmalı " +
                "LISS her gün yapılabilir Dinlenme günlerinde aç karnına LISS daha etkilidir")
        }
    }
}

data class DayPlan(val gun: String, val sabah: String, val aksam: String)
data class Uyum(val gun: String, val antrenman: String, val oncesi: String, val sonrasi: String)

object SampleData {
    val ogun1_spor = \"\"\"3 tam 3 beyaz yumurta haşlama veya omlet
40 70 g yulaf
150 g yoğurt
Bol yeşillik
1 tk zeytinyağı veya 3 4 ceviz
45g Protein 35g Karb 20g Yağ 500 kcal\"\"\"

    val ogun2_spor = \"\"\"150 200 g hindi göğüs veya ton balığı
200 g yoğurt
Bol salata zeytinyağı limon
30 40 g yulaf
1 tk zeytinyağı
55g Protein 25g Karb 15g Yağ 500 kcal\"\"\"

    val ogun1_normal = \"\"\"3 tam 3 beyaz yumurta haşlama veya omlet
40 50 g yulaf
150 g yoğurt
Bol yeşillik
1 tk zeytinyağı veya 3 4 ceviz
40g Protein 25g Karb 18g Yağ 450 kcal\"\"\"

    val ogun2_normal = \"\"\"150 200 g hindi göğüs veya ton balığı
200 g yoğurt
Bol salata zeytinyağı limon
1 tk zeytinyağı
50g Protein 10g Karb 14g Yağ 400 kcal\"\"\"
}

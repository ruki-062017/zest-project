package com.example.imabarimapapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // OSMDroid の設定（キャッシュやユーザーエージェント）
        Configuration.getInstance().userAgentValue = packageName

        // 地図の初期化
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

// 中心と初期ズーム
        map.controller.setCenter(GeoPoint(33.5, 132.8))
        map.controller.setZoom(8.0)

// ズーム制限
        map.setMinZoomLevel(8.0)
        map.setMaxZoomLevel(18.0)

// マルチタッチ有効化（指でのズーム）
        map.setMultiTouchControls(true)

        // ズームレベルの制限
        map.controller.setZoom(13.0) // 初期表示
        map.setMinZoomLevel(9.0)   // これ以上ズームアウトできない
        map.setMaxZoomLevel(18.0)  // これ以上ズームインできない
        // 地図の中心を今治市中央に設定
        map.controller.setCenter(GeoPoint(34.0661, 132.9979)) // 今治市役所付近

// 日本国内にスクロール制限
        val north = 45.8
        val south = 24.0
        val east = 153.0
        val west = 122.0
        map.setScrollableAreaLimitDouble(org.osmdroid.util.BoundingBox(north, east, south, west))
        
        // 観光スポットを読み込んでピンを追加
        loadTouristSpots()
    }

    private fun loadTouristSpots() {
        try {
            // assets/spots.json を読み込む
            val inputStream = assets.open("spots.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val lat = obj.getDouble("lat")
                val lon = obj.getDouble("lon")

                val marker = Marker(map)
                marker.position = GeoPoint(lat, lon)
                marker.title = name
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)
            }

            map.invalidate() // 地図を更新
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

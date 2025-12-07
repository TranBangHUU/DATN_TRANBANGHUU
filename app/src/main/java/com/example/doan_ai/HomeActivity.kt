package com.example.doan_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import android.util.Log


class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var viewPagerHero: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator

    private lateinit var rvFruitScope: RecyclerView
    private lateinit var rvVegetableScope: RecyclerView

    private lateinit var fruitScopeAdapter: PlantScopeAdapter
    private lateinit var vegetableScopeAdapter: PlantScopeAdapter
    private lateinit var btnStartDiagnosis: Button

    private lateinit var userProfileArea: View
    private lateinit var tvUserDisplay: TextView
    private lateinit var btnUserProfileIcon: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private var heroImageCount = 0
    private val MAX_DISPLAY_ITEMS = 4

    private val fallbackPlantList = listOf(
        PlantScope("Táo (Apple)", 4, R.drawable.apple, "fruit"),
        PlantScope("Cà chua (Tomato)", 6, R.drawable.tomato, "vegetable")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ánh xạ Views
        viewPagerHero = findViewById(R.id.viewPagerHero)
        dotsIndicator = findViewById(R.id.dotsIndicator)
        rvFruitScope = findViewById(R.id.rvFruitScope)
        rvVegetableScope = findViewById(R.id.rvVegetableScope)
        btnStartDiagnosis = findViewById(R.id.btnStartDiagnosis)

        // Ánh xạ Views cho Profile
        val tempBtn = findViewById<ImageButton>(R.id.btnUserLogin)
        userProfileArea = tempBtn
        tvUserDisplay = findViewById(R.id.tvUserDisplay)
        btnUserProfileIcon = tempBtn

        setupHeroCarousel()

        loadDiagnosisScopeFromFirestore()

        updateUserDisplay()

        userProfileArea.setOnClickListener {
            showProfileMenu()
        }
        btnStartDiagnosis.setOnClickListener { navigateToDiagnosis() }
    }

    // --- QUẢN LÝ VÒNG ĐỜI VÀ CUỘN TỰ ĐỘNG ---

    override fun onResume() {
        super.onResume()
        updateUserDisplay()
        if (heroImageCount > 1) {
            setupAutoScroll(heroImageCount)
        }
    }

    override fun onPause() {
        super.onPause()
        runnable?.let { handler.removeCallbacks(it) }
    }

    // --- LOGIC HIỂN THỊ TÊN/EMAIL ---

    private fun updateUserDisplay() {
        val user = auth.currentUser
        if (user != null) {
            val email = user.email ?: "Người dùng"
            val displayName = email.substringBefore('@').capitalize()
            tvUserDisplay.text = displayName
        } else {
            tvUserDisplay.text = "Đăng nhập"
        }
    }

    // --- LOGIC POPUP MENU (ĐĂNG XUẤT/ĐĂNG NHẬP) ---

    private fun showProfileMenu() {
        val user = auth.currentUser

        if (user == null) {
            navigateToUserScreen()
            return
        }

        val popupMenu = PopupMenu(this, userProfileArea)
        popupMenu.menuInflater.inflate(R.menu.menu_profile_dropdown, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_logout -> {
                    auth.signOut()
                    updateUserDisplay()
                    Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    return@setOnMenuItemClickListener true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // --- SETUP LOGIC CHÍNH ---

    private fun setupHeroCarousel() {
        val heroImages = listOf(
            R.drawable.img_plant_diagnosis,
            R.drawable.img_plant_diagnosis_1,
            R.drawable.img_plant_diagnosis_2,
            R.drawable.img_plant_diagnosis_3,
            R.drawable.img_plant_diagnosis_4
        )
        heroImageCount = heroImages.size

        viewPagerHero.adapter = HeroImageAdapter(heroImages)
        dotsIndicator.attachTo(viewPagerHero)
    }

    private fun setupAutoScroll(numPages: Int) {
        // ... (Logic tự động cuộn) ...
        runnable?.let { handler.removeCallbacks(it) }
        runnable = object : Runnable {
            override fun run() {
                if (viewPagerHero.currentItem < numPages - 1) {
                    viewPagerHero.currentItem++
                } else {
                    viewPagerHero.currentItem = 0
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable!!, 3000)
    }

    private fun loadDiagnosisScopeFromFirestore() {
        firestore.collection("plant_diagnosis_labels")
            .get()
            .addOnSuccessListener { snapshot ->
                val fullList = parseAndPreparePlantList(snapshot)

                // Phân loại dữ liệu thành 2 danh mục
                val fruitList = fullList.filter { it.type == "fruit" }
                val vegetableList = fullList.filter { it.type == "vegetable" }

                setupAdapters(fruitList, vegetableList, fullList.size)
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Lỗi tải Firestore: $e")
                setupAdapters(fallbackPlantList.filter { it.type == "fruit" },
                    fallbackPlantList.filter { it.type == "vegetable" },
                    fallbackPlantList.size)
            }
    }

    private fun parseAndPreparePlantList(snapshot: QuerySnapshot): List<PlantScope> {
        val list = mutableListOf<PlantScope>()

        for (document in snapshot.documents) {
            val firestoreData = document.toObject(FirestorePlantScope::class.java)
            val plantKey = document.id

            if (firestoreData != null) {
                // Ánh xạ dữ liệu và gộp trường diseaseGroups
                val iconResId = getDrawableIdByName(this, firestoreData.image ?: "ic_default")
                val diseaseCount = firestoreData.sl ?: 0
                val plantName = formatPlantName(plantKey)

                list.add(
                    PlantScope(
                        plantName = plantName,
                        diseaseCount = diseaseCount,
                        iconResId = iconResId,
                        type = firestoreData.type ?: "other",
                        diseaseGroups = firestoreData.diseaseGroups
                    )
                )
            }
        }
        return list
    }

    private fun setupAdapters(
        fruitList: List<PlantScope>,
        vegetableList: List<PlantScope>,
        totalCount: Int
    ) {
        // Cây Ăn Quả
        rvFruitScope.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        fruitScopeAdapter = PlantScopeAdapter(fruitList) { plant -> openGroupDetailScreen(plant) }
        rvFruitScope.adapter = fruitScopeAdapter

        // Cây Rau Màu
        rvVegetableScope.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        vegetableScopeAdapter = PlantScopeAdapter(vegetableList) { plant -> openGroupDetailScreen(plant) }
        rvVegetableScope.adapter = vegetableScopeAdapter

    }

    private fun openGroupDetailScreen(plantScope: PlantScope) {
        // Giả định: Trường diseaseGroups đã được thêm vào FirestorePlantScope và được tải vào PlantScope
        val groups = plantScope.diseaseGroups

        if (groups.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy nhóm bệnh chi tiết.", Toast.LENGTH_SHORT).show()
            return
        }

        // Chuyển đổi List<DiseaseGroup> thành chuỗi JSON để truyền qua Intent
        val groupsJson = Gson().toJson(groups)

        val intent = Intent(this, GroupDetailActivity::class.java).apply {
            putExtra("PLANT_NAME", plantScope.plantName)
            putExtra("DISEASE_GROUPS_JSON", groupsJson)
        }
        startActivity(intent)
    }


    // --- HÀM HỖ TRỢ (Mapping) ---

    private fun getDrawableIdByName(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    private fun formatPlantName(plantKey: String): String {
        return when (plantKey) {
            "PEACH" -> "Đào (Peach)"
            "APPLE" -> "Táo (Apple)"
            "CORN" -> "Ngô (Corn)"
            "GRAPE" -> "Nho (Grape)"
            "TOMATO" -> "Cà chua (Tomato)"
            "POTATO" -> "Khoai tây (Potato)"
            else -> plantKey
        }
    }

    // --- NAVIGATION ---

    private fun navigateToUserScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToDiagnosis() {
        val user = auth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập trước.", Toast.LENGTH_SHORT).show()
        }

    }
}
package org.unizd.rma.subasic

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL



class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutBurgerDetails: View
    private lateinit var textViewBurgerName: TextView
    private lateinit var textViewBurgerPrice: TextView
    private lateinit var textViewBurgerDesc: TextView
    private lateinit var textViewBurgerIngredients: TextView

    private lateinit var buttonBackToList: Button


    private val burgerList = mutableListOf<Burger>()
    private lateinit var adapter: BurgerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewBurgers)
        layoutBurgerDetails = findViewById(R.id.layoutBurgerDetails)
        textViewBurgerName = findViewById(R.id.textViewBurgerName)
        textViewBurgerPrice = findViewById(R.id.textViewBurgerPrice)
        textViewBurgerDesc = findViewById(R.id.textViewBurgerDesc)
        textViewBurgerIngredients = findViewById(R.id.textViewBurgerIngredients)



        buttonBackToList = findViewById(R.id.buttonBackToList)


        adapter = BurgerAdapter(burgerList, object : BurgerAdapter.ItemClickListener {
            override fun onItemClick(position: Int, isExpanded: Boolean) {
                showDetails(burgerList[position], isExpanded)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonBackToList.setOnClickListener {
            showList()
        }

        fetchBurgers()
    }

    private fun showList() {
        recyclerView.visibility = View.VISIBLE
        layoutBurgerDetails.visibility = View.GONE
    }

    private fun showDetails(burger: Burger, isExpanded: Boolean) {
        recyclerView.visibility = View.GONE
        layoutBurgerDetails.visibility = View.VISIBLE

        val burgerInfo = "ID: ${burger.id}\nName: ${burger.name}"

        val burgerDetails = "Price: ${burger.price}\nDescription: ${burger.desc}"


        val ingredients = StringBuilder("Ingredients:\n")
        for (ingredient in burger.ingredients) {
            ingredients.append("- ${ingredient}\n")
        }


        if (isExpanded) {
            textViewBurgerName.visibility = View.VISIBLE
            textViewBurgerPrice.visibility = View.VISIBLE
            textViewBurgerDesc.visibility = View.VISIBLE
            textViewBurgerIngredients.visibility = View.VISIBLE

            textViewBurgerName.text = burgerInfo
            textViewBurgerPrice.text = burgerDetails
            textViewBurgerDesc.text = ingredients

        } else {
            // Inače, sakrij sve detalje
            textViewBurgerName.visibility = View.GONE
            textViewBurgerPrice.visibility = View.GONE
            textViewBurgerDesc.visibility = View.GONE
            textViewBurgerIngredients.visibility = View.GONE
        }
    }

    private fun fetchBurgers() {
        val apiUrl = "https://burgers-hub.p.rapidapi.com/burgers"
        val apiKey = "9898e1321dmshf860a0029e8e995p14970bjsnc48452cd535a"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-RapidAPI-Key", apiKey)

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()

                    val jsonArray = JSONArray(response)

                    burgerList.clear()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val imagesArray = jsonObject.getJSONArray("images")
                        val desc = jsonObject.getString("desc")
                        val ingredientsArray = jsonObject.getJSONArray("ingredients")
                        val price = jsonObject.getDouble("price")
                        val veg = jsonObject.getBoolean("veg")

                        val burgerImages = mutableListOf<Map<String, String>>()
                        for (j in 0 until imagesArray.length()) {
                            val imageObj = imagesArray.getJSONObject(j)
                            val url = imageObj.optString("url") // Provjeri postoji li atribut "url"
                            if (!url.isNullOrEmpty()) {
                                val imageMap = mapOf("url" to url)
                                burgerImages.add(imageMap)
                            }
                        }

                        val burgerIngredients = mutableListOf<String>()
                        for (j in 0 until ingredientsArray.length()) {
                            val ingredientObj = ingredientsArray.getJSONObject(j)
                            val ingredientName = ingredientObj.getString("name")
                            burgerIngredients.add(ingredientName)
                        }

                        burgerList.add(
                            Burger(
                                id = id,
                                name = name,
                                desc = desc,
                                ingredients = burgerIngredients,
                                price = price,
                                veg = veg,
                                isExpanded = false
                            )
                        )
                    }

                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        layoutBurgerDetails.visibility = View.GONE
                    }
                } else {
                    throw Exception("Failed to fetch burgers: HTTP response code $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    data class Burger(
        val id: String,
        val name: String,
        val desc: String,
        val ingredients: MutableList<String>,
        val price: Double,
        val veg: Boolean,
        var isExpanded: Boolean = false
    )
}

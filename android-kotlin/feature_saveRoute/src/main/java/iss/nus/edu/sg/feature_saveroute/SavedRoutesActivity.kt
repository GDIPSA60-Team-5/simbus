package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.databinding.SavedRoutesBinding

class SavedRoutesActivity : AppCompatActivity() {
    private lateinit var binding: SavedRoutesBinding
    private lateinit var adapter: MyCustomAdapter
    private val routes = mutableListOf<Route>()

    private val editRouteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val updatedRoute = data?.getParcelableExtra<Route>("Edit_Route")
            val position = data?.getIntExtra("Position", -1)?:-1
            if (updatedRoute !=null && position>=0){
                routes[position] = updatedRoute
                adapter.notifyDataSetChanged()
            }
        }
    }
    private val addRouteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val newRoute = data?.getParcelableExtra<Route>("New_Route")
                if (newRoute != null) {
                    routes.add(newRoute)
                    adapter.notifyDataSetChanged()
                    binding.activeRoutesNumber.text = routes.size.toString()
                }
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyCustomAdapter(this, routes)
        binding.listview.adapter = adapter
        binding.activeRoutesNumber.text = routes.size.toString()



        binding.addRouteButton.setOnClickListener {
            val intent = Intent(this, AddEditRouteActivity::class.java).apply{
                putExtra("isEdit", false)
            }
            addRouteLauncher.launch(intent)
        }



        binding.listview.setOnItemClickListener { _, _, position, _ ->
            val route = routes[position]
            val intent = Intent(this, AddEditRouteActivity::class.java).apply {
                putExtra("isEdit", true)
                putExtra("Edit_Route", route)
                putExtra("Position", position)

            }
            editRouteLauncher.launch(intent)
        }

    }
}
package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import iss.nus.edu.sg.feature_saveroute.Data.Route
import iss.nus.edu.sg.feature_saveroute.databinding.RouteslistviewBinding

class MyCustomAdapter(
    private val context: Context,
    private val routes: List<Route>
) : BaseAdapter() {

    override fun getCount(): Int = routes.size

    override fun getItem(position: Int): Any = routes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val binding : RouteslistviewBinding

        val rowView: View
        if (convertView == null){
            binding = RouteslistviewBinding.inflate(LayoutInflater.from(context), parent, false)
            rowView = binding.root
            rowView.tag = binding
        }else{
            binding = convertView.tag as RouteslistviewBinding
            rowView = convertView
        }
        val route = routes[position]

        binding.routeNumber.text = "Route: ${getItemId(position+1)}"
        binding.FromText.text = "From: ${route.from}"
        binding.DestinationText.text = "To: ${route.to}"
        binding.BusStop.text = "Bus Stop: ${route.busStop}"
        binding.BusService.text = "Bus Service: ${route.busService}"
        binding.StartTime.text = "Start at: ${route.startTime}"
        binding.ArrivalTime.text = "Arrive at: ${route.arrivalTime}"
        binding.frequency.text = "Frequency: ${formatDays(route.selectedDays?: BooleanArray(7))}"

        return rowView

    }

    private fun formatDays(days: BooleanArray): String{
        val nameOfDay = listOf("Mon", "Tue", "Wed", "Thurs", "Fri", "Sat", "Sun")

        return when{
            days.all{it} -> "Everyday"
            days.slice(0..4).all{it} && !days[5] && !days[6] -> "Mon - Fri"
            else -> nameOfDay.filterIndexed { index, _ -> days[index] }.joinToString(", ")
        }

    }

}

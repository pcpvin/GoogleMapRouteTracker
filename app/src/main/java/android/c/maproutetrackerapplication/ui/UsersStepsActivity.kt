package android.c.maproutetrackerapplication.ui

import android.annotation.SuppressLint
import android.c.maproutetrackerapplication.api.RetrofitClient
import android.c.maproutetrackerapplication.controller.MapsController
import android.c.maproutetrackerapplication.model.google.directions.Directions
import android.c.maproutetrackerapplication.model.google.directions.StepsItem
import android.c.maproutetrackerapplication.model.others.Route
import android.c.maproutetrackerapplication.ui.adapters.UsersStepsAdapter
import android.c.maproutetrackerapplication.utility.Constants
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.samplediections.R
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.users_steps_activity.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersStepsActivity:AppCompatActivity()
{
    val routeItems = ArrayList<StepsItem>()
    var usersStepsAdapter: UsersStepsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.users_steps_activity)

        usersStepsAdapter= UsersStepsAdapter(routeItems,applicationContext)
        //now adding the adapter to recyclerview
        users_steps_recycler.adapter = usersStepsAdapter
        findingRoute()
    }


    private fun findingRoute()
    {
         progresBarMap.visibility = View.VISIBLE
            val directionsCall = RetrofitClient.googleMethods().getDirections(getString(R.string.source), getString(R.string.destination), Constants.GOOGLE_API_KEY)
            directionsCall.enqueue(object : Callback<Directions> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<Directions>, response: Response<Directions>) {
                    val directions = response.body()!!

                    if (directions.status == "OK")
                    {
                        progresBarMap.visibility = View.GONE
                        val legs = directions.routes[0].legs[0]
                        kms_travelled.text = legs.distance.text
                        time_required.text= " ("+legs.duration.text+")"

                        legs.steps!!.forEach { st ->
                            routeItems.add(st)
                        }

                        users_steps_recycler.layoutManager = LinearLayoutManager(this@UsersStepsActivity)
                        usersStepsAdapter?.notifyItemRangeChanged(0, routeItems.size)

                    } else {
                        toast(directions.status)
                        directionsToggleButton.isChecked = false
                    }


                }

                override fun onFailure(call: Call<Directions>, t: Throwable) {
                    toast(t.toString())
                    progressLayout.visibility = View.GONE
                    directionsToggleButton.isChecked = false
                }
            })
        }
}



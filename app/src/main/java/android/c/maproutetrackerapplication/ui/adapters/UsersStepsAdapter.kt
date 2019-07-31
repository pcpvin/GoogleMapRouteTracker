package android.c.maproutetrackerapplication.ui.adapters

import android.annotation.TargetApi
import android.c.maproutetrackerapplication.model.google.directions.StepsItem
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.samplediections.R
import kotlinx.android.synthetic.main.users_steps_list.view.*
import org.jetbrains.anko.image

class UsersStepsAdapter(val items: ArrayList<StepsItem>, val context: Context) : RecyclerView.Adapter<MachinesViewHolder>()
{

    private val TAG = UsersStepsAdapter::class.java.getSimpleName()
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachinesViewHolder {
        return MachinesViewHolder(LayoutInflater.from(context).inflate(R.layout.users_steps_list, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: MachinesViewHolder, position: Int)
    {
        holder.instructionTxt.text = Html.fromHtml(items[position].htmlInstructions)
        holder.directionsImg.setImageDrawable(context.resources.getDrawable(R.drawable.ic_location_end_black_24dp, context.theme))

    }
}


class MachinesViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    val instructionTxt = view.instructions_txt!!
    val directionsImg= view.directions_img!!



}
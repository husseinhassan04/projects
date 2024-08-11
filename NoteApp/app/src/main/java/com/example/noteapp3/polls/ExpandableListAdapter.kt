package com.example.noteapp3.polls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.noteapp3.R
import com.example.noteapp3.models.RetroFitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ExpandableListAdapter(
    private val context: Context,
    private val polls: List<Poll>,
    private val userId: String
) : BaseExpandableListAdapter() {


    private class ViewHolder(view: View) {
        val pieChart: PieChart = view.findViewById(R.id.chart)
        val option1: RadioButton = view.findViewById(R.id.option1)
        val option2: RadioButton = view.findViewById(R.id.option2)
        val option3: RadioButton = view.findViewById(R.id.option3)
        val option4: RadioButton = view.findViewById(R.id.option4)
        val pollOptions: RadioGroup = view.findViewById(R.id.poll_options)
        var isAnimated: Boolean = false
    }

    override fun getGroupCount(): Int {
        return polls.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        // Assuming each poll has only one set of options
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return polls[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        // Assuming each poll has only one set of options
        return polls[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val poll = getGroup(groupPosition) as Poll

        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item_layout, null)
        }

        val parentItemTitle = convertView!!.findViewById<TextView>(R.id.parentItemTitle)
        parentItemTitle.text = poll.text

        val arrowIndicator = convertView.findViewById<ImageView>(R.id.arrowIndicator)
        arrowIndicator.setImageResource(
            if (isExpanded) R.drawable.arrow_collapse else R.drawable.arrow_expand
        )

        // Toggle group expansion/collapse
        convertView.setOnClickListener {
            val expandableListView = parent as ExpandableListView
            if (isExpanded) {
                expandableListView.collapseGroup(groupPosition)
                arrowIndicator.setImageResource(R.drawable.arrow_expand)
            } else {
                expandableListView.expandGroup(groupPosition)
                arrowIndicator.setImageResource(R.drawable.arrow_collapse)
            }
        }

        return convertView
    }


    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val poll = getChild(groupPosition, childPosition) as Poll

        val viewHolder: ViewHolder
        val view: View
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_poll, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Bind poll options to the radio buttons
        viewHolder.option1.text = poll.option1
        viewHolder.option2.text = poll.option2
        viewHolder.option3.text = poll.option3
        viewHolder.option4.text = poll.option4

        // Set radio buttons based on whether userId is in selected lists
        viewHolder.option1.isChecked = poll.selected1.contains(userId)
        viewHolder.option2.isChecked = poll.selected2.contains(userId)
        viewHolder.option3.isChecked = poll.selected3.contains(userId)
        viewHolder.option4.isChecked = poll.selected4.contains(userId)

        // Set radio group listener to handle selection changes
        viewHolder.pollOptions.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.option1 -> handleOptionSelected(poll, 1)
                R.id.option2 -> handleOptionSelected(poll, 2)
                R.id.option3 -> handleOptionSelected(poll, 3)
                R.id.option4 -> handleOptionSelected(poll, 4)
            }
        }

        // Pie Chart setup
        val pollOptionsNb = listOf(
            poll.option1 to poll.selected1.size,
            poll.option2 to poll.selected2.size,
            poll.option3 to poll.selected3.size,
            poll.option4 to poll.selected4.size
        )

        val entries = pollOptionsNb
            .filter { it.second > 0 }
            .map { option -> PieEntry(option.second.toFloat(), option.first) }

        val dataSet = PieDataSet(entries, "Poll Results")
        dataSet.colors = listOf(
            ContextCompat.getColor(context, R.color.color1),
            ContextCompat.getColor(context, R.color.color2),
            ContextCompat.getColor(context, R.color.color3),
            ContextCompat.getColor(context, R.color.color4)
        )

        val pieData = PieData(dataSet)
        viewHolder.pieChart.data = pieData

        // Customize the chart
        val description = Description()
        description.text = "Number of Votes for Each Option"
        viewHolder.pieChart.description = description

        // Animate the chart only if not animated before
        if (!viewHolder.isAnimated) {
            viewHolder.pieChart.animateY(2000)
            viewHolder.isAnimated = true
        }

        // Refresh the chart
        viewHolder.pieChart.invalidate()

        return view
    }



    private fun handleOptionSelected(poll: Poll, optionNumber: Int) {
        // Remove userId from previously selected options
        poll.selected1.remove(userId)
        poll.selected2.remove(userId)
        poll.selected3.remove(userId)
        poll.selected4.remove(userId)

        // Add userId to the selected option
        when (optionNumber) {
            1 -> poll.selected1.add(userId)
            2 -> poll.selected2.add(userId)
            3 -> poll.selected3.add(userId)
            4 -> poll.selected4.add(userId)
        }
        updatePoll(poll)


        notifyDataSetChanged()
    }

    private fun updatePoll(poll: Poll) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.updatePoll(poll.id, poll)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Handle successful update
                    println("Poll updated successfully")
                } else {
                    // Handle unsuccessful response
                    println("Failed to update poll: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure
                println("Error: ${t.message}")
            }
        })
    }
}

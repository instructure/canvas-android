package com.instructure.androidfoosball.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import de.hdodenhof.circleimageview.CircleImageView

class TeamAdapter(private val mContext: Context, private val mRawTeams: List<CustomTeam>, private val userMap: Map<String, User>) : BaseAdapter() {
    private val mAvatarSize = mContext.resources.getDimension(R.dimen.avatar_size_small).toInt()

    private var mTeams = mRawTeams

    override fun getCount() = mTeams.size

    override fun getItem(position: Int) = mTeams[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

    var searchQuery = ""
        set(value) {
            field = value
            mTeams = if (value.isBlank()) {
                mRawTeams
            } else {
                mRawTeams.filter { it.teamName.contains(searchQuery, true) }
            }
            notifyDataSetChanged()
        }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: TeamHolder
        if (view == null) {
            view = View.inflate(mContext, R.layout.adapter_team_dialog, null)
            holder = TeamHolder()
            holder.teamName = view.findViewById(R.id.teamName)
            holder.avatar1 = view.findViewById(R.id.avatar1)
            holder.avatar2 = view.findViewById(R.id.avatar2)
            view.tag = holder
        } else {
            holder = view.tag as TeamHolder
        }
        val team = mTeams[position]
        holder.teamName?.text = team.teamName
        holder.avatar1?.setAvatar(userMap[team.users[0]], mAvatarSize)
        holder.avatar2?.setAvatar(userMap[team.users[1]], mAvatarSize)
        return view!!
    }

    internal class TeamHolder {
        var teamName: TextView? = null
        var avatar1: CircleImageView? = null
        var avatar2: CircleImageView? = null
    }
}

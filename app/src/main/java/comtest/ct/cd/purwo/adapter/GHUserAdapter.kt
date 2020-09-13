package comtest.ct.cd.purwo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import comtest.ct.cd.purwo.R
import comtest.ct.cd.purwo.rest.UserModel
import kotlinx.android.synthetic.main.ghuser_item.view.*


/**
 * Created By Purwo on 12/09/20
 */
class GHUserAdapter(
    var userList: MutableList<UserModel>,
    var userListFiltered: MutableList<UserModel>
) :
    RecyclerView.Adapter<GHUserAdapter.ViewHolder>(),
    Filterable {

    private fun loadIntet() {

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.tv_username
        val avatar: ImageView = itemView.iv_avatar
        val layoutParent: ConstraintLayout = itemView.cl_parent

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ghuser_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ghUser = userListFiltered[position]

        Glide.with(holder.itemView.context)
            .load(ghUser.avatarUrl)
            .apply(
                RequestOptions().override(55, 55)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_broken)
            )
            .into(holder.avatar)

        holder.userName.text = ghUser.login
        holder.layoutParent.setOnClickListener {
            loadIntet()
        }
    }

    override fun getItemCount(): Int {
        return userListFiltered.size
    }

    fun clear() {
        userList.clear()
        userListFiltered.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<UserModel>) {
        userList.addAll(list)
        userListFiltered.addAll(list)
        notifyDataSetChanged()
    }

    fun add(user: UserModel) {
        userList.add(user)
        userListFiltered.add(user)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()

                userListFiltered = if (charString.isEmpty()) {
                    userList
                } else {
                    val filteredList: MutableList<UserModel> = arrayListOf()
                    for (row in userList) {
                        if (row.login!!.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }
                    }

                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = userListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                userListFiltered = filterResults.values as ArrayList<UserModel>
                notifyDataSetChanged()
            }
        }
    }

}
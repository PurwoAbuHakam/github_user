package comtest.ct.cd.purwo.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import comtest.ct.cd.purwo.R
import comtest.ct.cd.purwo.rest.ApiServices  as Api
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import comtest.ct.cd.purwo.adapter.GHUserAdapter
import comtest.ct.cd.purwo.tools.InfiniteScrollListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created By Purwo on 12/09/20
 */
class MainActivity : BaseActivity() {

    private val user by lazy { Api.create() }
    private var adapter: GHUserAdapter? = null
    private val perPage: Int = 100
    private val delay: Long = 2000
    private var totalPage: Int = 0
    private var currentPage: Int = 1
    private var isSearch: Boolean = false
    private var nextPage: Boolean = false
    private var queryCurrent: String = ""
    private var orderBy: String = "desc"
    private var querySearched: Int = 0
    private var lastTextChanged: Long = 0

    private var userLinearLayout = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.subtitle = getString(R.string.ctcd)

        initView()
    }

    private fun initView() {
        rv_ghuser.layoutManager = userLinearLayout
        rv_ghuser.addOnScrollListener(object : InfiniteScrollListener(userLinearLayout) {
            override fun onLoadMore(current_page: Int) {
                if (nextPage) {
                    currentPage++
                    getQueryData(queryCurrent, currentPage, perPage, orderBy)
                }
            }

        })

        et_search_user.onChange()
    }

    private fun TextView.onChange() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty())
                    clearListUser()
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    if (querySearched > et_search_user.text.toString().length)
                        getSearchUser(handler, false)
                    else {
                        if (adapter != null && totalPage > 1) {
                            if (!nextPage)
                                adapter?.filter?.filter(et_search_user.text.toString())
                            else
                                getSearchUser(handler, true)
                        } else {
                            getSearchUser(handler, false)
                        }
                    }
                } else {
                    clearListUser()
                }

                querySearched = et_search_user.text.toString().length
            }
        })
    }

    private fun getSearchUser(handler: Handler?, filter: Boolean) {
        if (adapter != null && adapter?.itemCount!! > 0 && filter) {
            adapter?.filter?.filter(et_search_user.text.toString())
            if (adapter?.itemCount!! == 0) {
                clearListUser()
                dataNotFound(getString(R.string.code_error), getString(R.string.not_found))
            }
        } else {
            lastTextChanged = System.currentTimeMillis()
            handler?.postDelayed(handlerSearch, delay)
        }
    }

    private fun dataNotFound(string: String, string1: String) {
        ll_error.visibility = View.VISIBLE
        tv_code.text = string
        tv_message.text = string1
    }

    private val handlerSearch = Runnable {
        if (System.currentTimeMillis() > lastTextChanged + delay - 500 && et_search_user.text.toString()
                .isNotEmpty()
        ) {
            clearListUser()
            getQueryData(et_search_user.text.toString(), currentPage, perPage, orderBy)
        }
    }

    private fun clearListUser() {
        if (adapter != null) {
            ll_error.visibility = View.GONE
            queryCurrent = ""
            totalPage = 0
            currentPage = 1
            adapter!!.clear()
            adapter!!.notifyItemRangeChanged(0, 0)
        }
    }

    private fun getQueryData(q: String, page: Int, perPage: Int, orderBy: String) {
        et_search_user.isEnabled = true
        if (!isSearch) {
            loading(true)
            disposable = user.search(q, page, perPage, orderBy)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        loading(false)
                        val count = result.items.count()
                        if (count > 0) {
                            totalPage += count
                            ll_error.visibility = View.GONE

                            if (adapter == null) {
                                adapter = GHUserAdapter(
                                    result.items.toMutableList(),
                                    result.items.toMutableList()
                                )
                                rv_ghuser.adapter = adapter
                            } else {
                                if (queryCurrent != q || (result.items.count() == 1 && currentPage == 1))
                                    adapter!!.clear()

                                adapter!!.addAll(result.items)
                                adapter!!.notifyItemRangeChanged(0, adapter!!.itemCount)
                            }
                            nextPage =
                                (result.totalCount!! > totalPage && result.totalCount > perPage)

                        } else {
                            clearListUser()
                            dataNotFound(
                                getString(R.string.code_error),
                                getString(R.string.not_found)
                            )
                        }
                    },
                    { error ->
                        loading(false)
                        clearListUser()
                        dataNotFound(
                            getString(R.string.code_error),
                            getString(R.string.not_found)
                        )

                        Handler().postDelayed(
                            {
                                getQueryData(
                                    et_search_user.text.toString(),
                                    currentPage,
                                    perPage,
                                    orderBy
                                )
                            },
                            60000
                        )

                        delayed()
                    }
                )

            queryCurrent = q
        }
    }

    private fun delayed() {
        et_search_user.isEnabled = false
        Handler().postDelayed({
            val count = Integer.parseInt(et_search_user.text.toString()) - 1
            tv_code.text = count.toString()
            if (count > 0)
                delayed()

        }, 1000)
    }

    private fun loading(b: Boolean) {
        ll_error.visibility = View.GONE
        isSearch = b

        if (b)
            ll_loading.visibility = View.VISIBLE
        else
            ll_loading.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort -> {
                orderBy = "asc"
                getQueryData(queryCurrent, currentPage, perPage, orderBy)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
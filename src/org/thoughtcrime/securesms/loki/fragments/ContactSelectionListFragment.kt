package org.thoughtcrime.securesms.loki.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.contact_selection_list_fragment.*
import network.loki.messenger.R
import org.thoughtcrime.securesms.contacts.ContactsCursorLoader
import org.thoughtcrime.securesms.mms.GlideApp
import org.thoughtcrime.securesms.recipients.Recipient

class ContactSelectionListFragment : Fragment(), LoaderManager.LoaderCallbacks<List<ContactSelectionListItem>>, ContactClickListener {
    private var cursorFilter: String? = null
    var onContactSelectedListener: OnContactSelectedListener? = null

    val selectedContacts: List<String>
        get() = listAdapter.selectedContacts.map { it.address.serialize() }

    private val multiSelect: Boolean by lazy {
        activity!!.intent.getBooleanExtra(MULTI_SELECT, false)
    }

    private val listAdapter by lazy {
        val result = ContactSelectionListAdapter(activity!!, multiSelect)
        result.glide = GlideApp.with(this)
        result.contactClickListener = this
        result
    }

    companion object {
        @JvmField val DISPLAY_MODE = "display_mode"
        @JvmField val MULTI_SELECT = "multi_select"
        @JvmField val REFRESHABLE = "refreshable"
    }

    interface OnContactSelectedListener {
        fun onContactSelected(number: String?)
        fun onContactDeselected(number: String?)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = listAdapter
        swipeRefreshLayout.isEnabled = activity!!.intent.getBooleanExtra(REFRESHABLE, true)
    }

    override fun onStart() {
        super.onStart()
        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.contact_selection_list_fragment, container, false)
    }

    fun setQueryFilter(filter: String?) {
        cursorFilter = filter
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    fun resetQueryFilter() {
        setQueryFilter(null)
        swipeRefreshLayout.isRefreshing = false
    }

    fun setRefreshing(refreshing: Boolean) {
        swipeRefreshLayout.isRefreshing = refreshing
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener?) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ContactSelectionListItem>> {
        return ContactSelectionListLoader(activity!!,
            activity!!.intent.getIntExtra(DISPLAY_MODE, ContactsCursorLoader.DisplayMode.FLAG_ALL),
            cursorFilter)
    }

    override fun onLoadFinished(loader: Loader<List<ContactSelectionListItem>>, items: List<ContactSelectionListItem>) {
        update(items)
    }

    override fun onLoaderReset(loader: Loader<List<ContactSelectionListItem>>) {
        update(listOf())
    }

    private fun update(items: List<ContactSelectionListItem>) {
        listAdapter.items = items
        mainContentContainer.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        emptyStateContainer.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onContactClick(contact: Recipient) {
        listAdapter.onContactClick(contact)
    }

    override fun onContactSelected(contact: Recipient) {
        onContactSelectedListener?.onContactSelected(contact.address.serialize())
    }

    override fun onContactDeselected(contact: Recipient) {
        onContactSelectedListener?.onContactDeselected(contact.address.serialize())
    }
}
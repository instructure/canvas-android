/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.fragments.TableFragment
import com.instructure.androidfoosball.fragments.UserFragment
import com.instructure.androidfoosball.interfaces.TextEditCallback
import com.instructure.androidfoosball.models.User
import com.instructure.androidfoosball.utils.AnimUtils
import com.instructure.androidfoosball.utils.Const
import com.instructure.androidfoosball.utils.FireUtils
import com.instructure.androidfoosball.utils.Prefs
import kotlinx.android.synthetic.phone.activity_primary.*
import org.jetbrains.anko.startActivity
import java.util.*

class PrimaryActivity : BaseFireBaseActivity(), TextEditCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primary)
        setSupportActionBar(toolbar)
        initFireBase()
        setupUser()

        pager.offscreenPageLimit = 2
        pager.adapter = TabsPagerAdapter(supportFragmentManager)
        tabs.setupWithViewPager(pager)

        if (SignInActivity.ACTION_SHORTCUT_TABLES == intent.action)
            pager.currentItem = PAGE_TABLES

        phraseDone.setOnClickListener(mPhraseDoneClickListener)
    }

    private fun setupUser() {
        val user = intent.extras.getParcelable<User>(Const.USER)
        if (user == null) {
            startActivity<SignInActivity>()
            finish()
        } else {
            mUser = user
        }
    }

    override fun onAuthStateChange(firebaseAuth: FirebaseAuth) {
        if (firebaseAuth.currentUser == null) {
            Prefs(this).userId = ""
            startActivity<SignInActivity>()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            mAuth?.signOut()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class TabsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                PAGE_USER -> return UserFragment.newInstance()
                else -> return TableFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                PAGE_USER -> return mUser?.name?.toUpperCase() ?: ""
                else -> return getString(R.string.tables).toUpperCase(Locale.getDefault())
            }
        }
    }

    //region TextEditCallbacks

    override fun requestTextEdit(resId_requester: Int, text: String) {
        phraseDone.tag = resId_requester
        phraseEditText.setText(text)
        phraseEditText.setSelection(phraseEditText.text.length)
        if (phraseEditWrapper.visibility != View.VISIBLE) {
            showKeyboard()
        }
        AnimUtils.fadeIn(360, phraseEditWrapper)
    }

    private val mPhraseDoneClickListener = View.OnClickListener { v ->
        val resId_requester = v.tag as Int
        val fragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0)
        when (resId_requester) {
            R.id.startupPhrase -> {
                FireUtils.setStartupPhrase(mUser!!.id, mDatabase!!, phraseEditText.text.toString())
                if (fragment is UserFragment) {
                    fragment.updateStartupPhraseText(phraseEditText.text.toString())
                }
            }
            R.id.victoryPhrase -> {
                FireUtils.setVictoryPhrase(mUser!!.id, mDatabase!!, phraseEditText.text.toString())
                if (fragment is UserFragment) {
                    fragment.updateVictoryPhraseText(phraseEditText.text.toString())
                }
            }
        }
        hideKeyboard()
        AnimUtils.fadeOut(360, phraseEditWrapper)
    }

    //endregion

    override fun onBackPressed() {
        if (phraseEditWrapper.visibility == View.VISIBLE) {
            AnimUtils.fadeOut(360, phraseEditWrapper)
            return
        }
        super.onBackPressed()
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        val PAGE_USER = 0
        val PAGE_TABLES = 1

        fun createIntent(context: Context, user: User): Intent {
            val intent = Intent(context, PrimaryActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable(Const.USER, user)
            intent.putExtras(bundle)
            return intent
        }

        /**
         * Creates an intent with the data provided
         * @param context Where we are coming from
         * *
         * @param user User's info
         * *
         * @param action The action
         * *
         * @return A fully armed and operational intent
         */
        fun createIntent(context: Context, user: User, action: String): Intent {
            val intent = Intent(context, PrimaryActivity::class.java)
            intent.action = action
            val bundle = Bundle()
            bundle.putParcelable(Const.USER, user)
            intent.putExtras(bundle)
            return intent
        }
    }
}

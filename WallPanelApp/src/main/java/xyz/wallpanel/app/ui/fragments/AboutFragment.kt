/*
 * Copyright (c) 2022 WallPanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package xyz.wallpanel.app.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import xyz.wallpanel.app.R
import xyz.wallpanel.app.ui.activities.SettingsActivity
import xyz.wallpanel.app.databinding.FragmentAboutBinding

import timber.log.Timber

class AboutFragment : Fragment() {


    private lateinit var binding: FragmentAboutBinding
    private var versionNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set title bar
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.pref_about_title))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            view?.let { Navigation.findNavController(it).navigate(R.id.settings_action) }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            versionNumber = " v" + packageInfo.versionName
            binding.versionName.text = versionNumber
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e.message)
        }

        binding.sendFeedbackButton.setOnClickListener { feedback() }
        binding.rateApplicationButton.setOnClickListener { rate() }
        binding.githubButton.setOnClickListener { showGitHub() }
        binding.supportButton.setOnClickListener { showSupport() }
        binding.privacyPolicyButton.setOnClickListener { showPrivacyPolicy() }
    }

    private fun rate() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GOOGLE_PLAY_RATING)))
        } catch (ex: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + GOOGLE_PLAY_RATING)))
        }
    }

    private fun showSupport() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL)))
    }

    private fun showGitHub() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
    }

    private fun showPrivacyPolicy() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
    }

    private fun feedback() {
        val email = Intent(Intent.ACTION_SENDTO)
        email.type = "text/email"
        email.data = Uri.parse("mailto:" + EMAIL_ADDRESS)
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_to_subject_text) + " " + versionNumber)
        startActivity(Intent.createChooser(email, getString(R.string.mail_subject_text)))
    }

    companion object {
        const val SUPPORT_URL:String = "https://wallpanel.xyz"
        const val GOOGLE_PLAY_RATING = "xyz.wallpanel.app"
        const val GITHUB_URL = "https://github.com/TheTimeWalker/wallpanel-android"
        const val EMAIL_ADDRESS = "tony+wallpanel@stipanic.ch"
        const val PRIVACY_POLICY_URL = "https://wallpanel.xyz/privacy-policy"

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}
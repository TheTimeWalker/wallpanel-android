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

package xyz.wallpanel.app.ui.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import xyz.wallpanel.app.R
import xyz.wallpanel.app.persistence.Configuration
import xyz.wallpanel.app.utils.DialogUtils
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_settings)

        supportActionBar?.show()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = (getString(R.string.title_settings))

        lifecycle.addObserver(dialogUtils)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val PERMISSIONS_REQUEST_WRITE_SETTINGS = 200
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
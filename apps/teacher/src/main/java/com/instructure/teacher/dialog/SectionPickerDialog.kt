/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Section
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.teacher.R
import com.instructure.teacher.databinding.DialogSectionPickerBinding
import com.instructure.teacher.databinding.ViewSectionListItemBinding

class SectionPickerDialog : BaseCanvasDialogFragment() {

    private lateinit var binding: DialogSectionPickerBinding

    init {
        retainInstance = true
    }

    var sections: ArrayList<Section> by ParcelableArrayListArg(default = arrayListOf())

    // Callback takes a String, which is a comma separated list of section ids
    var callback: (String) -> Unit = {}

    var initialSelectedSections: ArrayList<Section> by ParcelableArrayListArg(default = arrayListOf()) // All sections selected by default
    private lateinit var updatedCallback: (MutableList<Section>) -> Unit

    private var mutableSelectedSections: MutableList<Section> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mutableSelectedSections = initialSelectedSections.toMutableList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        updatedCallback = {
            mutableSelectedSections = it
        } // Update the bundled arg for our initial sections; used on config changes

        // Add the "All sections" option
        val mutableSections = sections.toMutableList()
        mutableSections.add(0, Section().apply { name = getString(R.string.allSections) })

        // Setup the view and recycler adapter
        val sectionsAdapter = SectionRecyclerViewAdapter(mutableSections, mutableSelectedSections, updatedCallback)

        val inflater = requireActivity().layoutInflater
        binding = DialogSectionPickerBinding.inflate(inflater, null, false)
        binding.sectionRecyclerView.adapter = sectionsAdapter
        binding.sectionRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
            .apply { orientation = RecyclerView.VERTICAL }

        val dialog = AlertDialog.Builder(requireActivity())
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // Return the ids of the selected sections
                    callback(mutableSelectedSections.map { it.id }.joinToString(separator = ",") { it.toString() })
                }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                callback(initialSelectedSections.map { it.id }.joinToString(separator = ",") { it.toString() })
            }
            .create()

        return dialog.apply {
            setOnShowListener {
                // Style dialog
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, sections: List<Section>, selectedSections: List<Section>, callback: (String) -> Unit) = SectionPickerDialog().apply {
            fragmentManager.dismissExisting<SectionPickerDialog>()
            this.sections = sections as ArrayList<Section>
            this.callback = callback
            this.initialSelectedSections = selectedSections as ArrayList<Section>
            show(fragmentManager, SectionPickerDialog::class.java.simpleName)
        }
    }
}

class SectionRecyclerViewAdapter(
    val sections: List<Section>,
    private val selectedSections: MutableList<Section>,
    private val updatedCallback: (MutableList<Section>) -> Unit
) : RecyclerView.Adapter<SectionRecyclerViewAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = ViewSectionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.checkbox.applyTheme(ThemePrefs.brandColor)
        return SectionViewHolder(binding)
    }

    override fun getItemCount(): Int = sections.size

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val checked = if (selectedSections.isEmpty() && position == 0) true else selectedSections.contains(sections[position])
        holder.bind(sections[position].name, checked) { isChecked ->
            if (position == 0) {
                // 'All Sections' is selected, clear out all other selections and mark their positions
                sections.forEachIndexed { index, section -> if (selectedSections.contains(section)) notifyItemChanged(index) }
                selectedSections.apply { clear(); }
                notifyItemChanged(0)
            } else {
                if (isChecked) {
                    if (selectedSections.isEmpty()) {
                        // A specific section was selected; Uncheck 'all'
                        notifyItemChanged(0) // Allows checkbox animation to happen
                    }

                    selectedSections.add(sections[position]) // Add the newly selected section
                } else {
                    selectedSections.remove(sections[position]) // Section was deselected; remove it

                    if (selectedSections.isEmpty()) {
                        // Have to have at least one section selected... default to 'all'
                        notifyItemChanged(0)
                    }
                }

                updatedCallback(selectedSections)

                notifyItemChanged(position)
            }
        }
    }

    inner class SectionViewHolder(val binding: ViewSectionListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sectionName: String, checked: Boolean, callback: (Boolean) -> Unit) {
            binding.sectionName.text = sectionName

            if ((checked && !binding.checkbox.isChecked) || (!checked && binding.checkbox.isChecked)) {
                // Checked state changed; update the checkbox
                binding.checkbox.toggle()
            }

            binding.checkbox.setOnClickListener { callback(binding.checkbox.isChecked); (it as CheckBox).toggle() }
            binding.root.setOnClickListener { binding.checkbox.performClick() }
        }
    }
}

package com.instructure.student.mobius.settings.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.instructure.student.databinding.ViewHelpLinkBinding

@BindingAdapter(value = ["helpLinks", "viewModel"])
fun bindHelpLinks(container: ViewGroup, links: List<HelpLinkViewData>?, viewModel: HelpDialogViewModel) {
    links?.forEach {
        val binding = ViewHelpLinkBinding.inflate(LayoutInflater.from(container.context))
        binding.helpLink = it
        binding.viewModel = viewModel
        container.addView(binding.root)
    }
}
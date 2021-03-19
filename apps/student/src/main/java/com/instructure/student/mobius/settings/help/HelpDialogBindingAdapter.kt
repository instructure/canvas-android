package com.instructure.student.mobius.settings.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.instructure.student.databinding.ViewHelpLinkBinding

@BindingAdapter("helpLinks")
fun bindHelpLinks(container: ViewGroup, links: List<HelpLinkViewData>?) {
    links?.forEach {
        val binding = ViewHelpLinkBinding.inflate(LayoutInflater.from(container.context))
        binding.helpLink = it
        container.addView(binding.root)
    }
}
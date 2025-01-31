package org.koitharu.kotatsu.filter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.koitharu.kotatsu.core.ui.BaseFragment
import org.koitharu.kotatsu.core.ui.widgets.ChipsView
import org.koitharu.kotatsu.core.util.ext.isAnimationsEnabled
import org.koitharu.kotatsu.core.util.ext.observe
import org.koitharu.kotatsu.databinding.FragmentFilterHeaderBinding
import org.koitharu.kotatsu.filter.ui.model.FilterHeaderModel
import org.koitharu.kotatsu.filter.ui.tags.TagsCatalogSheet
import org.koitharu.kotatsu.parsers.model.MangaTag
import javax.inject.Inject

@AndroidEntryPoint
class FilterHeaderFragment : BaseFragment<FragmentFilterHeaderBinding>(), ChipsView.OnChipClickListener,
	ChipsView.OnChipCloseClickListener {

	@Inject
	lateinit var filterHeaderProducer: FilterHeaderProducer

	private val filter: FilterCoordinator
		get() = (requireActivity() as FilterCoordinator.Owner).filterCoordinator

	override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFilterHeaderBinding {
		return FragmentFilterHeaderBinding.inflate(inflater, container, false)
	}

	override fun onViewBindingCreated(binding: FragmentFilterHeaderBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		binding.chipsTags.onChipClickListener = this
		binding.chipsTags.onChipCloseClickListener = this
		filterHeaderProducer.observeHeader(filter)
			.flowOn(Dispatchers.Default)
			.observe(viewLifecycleOwner, ::onDataChanged)
	}

	override fun onWindowInsetsChanged(insets: Insets) = Unit

	override fun onChipClick(chip: Chip, data: Any?) {
		when (data) {
			is MangaTag -> filter.toggleTag(data, !chip.isChecked)
			is String -> Unit
			null -> TagsCatalogSheet.show(parentFragmentManager, isExcludeTag = false)
		}
	}

	override fun onChipCloseClick(chip: Chip, data: Any?) {
		when (data) {
			is String -> filter.setQuery(null)
		}
	}

	private fun onDataChanged(header: FilterHeaderModel) {
		val binding = viewBinding ?: return
		val chips = header.chips
		if (chips.isEmpty()) {
			binding.chipsTags.setChips(emptyList())
			binding.root.isVisible = false
			return
		}
		binding.chipsTags.setChips(header.chips)
		binding.root.isVisible = true
		if (binding.root.context.isAnimationsEnabled) {
			binding.scrollView.smoothScrollTo(0, 0)
		} else {
			binding.scrollView.scrollTo(0, 0)
		}
	}
}

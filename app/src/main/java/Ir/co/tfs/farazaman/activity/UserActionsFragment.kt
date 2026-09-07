package Ir.co.tfs.farazaman.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.databinding.FragmentUserActionsBinding
import Ir.co.tfs.farazaman.presentation.viewmodel.ActionsViewModel
import Ir.co.tfs.farazaman.supervisor.actions.UserActionCategory

class UserActionsFragment : Fragment() {
    private var _binding: FragmentUserActionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActionsViewModel by lazy {
        ViewModelProvider(this)[ActionsViewModel::class.java]
    }
    private lateinit var adapter: UserActionAdapter
    private var filtersCreated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserActionAdapter { item, operation ->
            (activity as? UserActionOperationHost)?.onActionOperation(item, operation)
        }
        binding.actionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.actionsRecycler.adapter = adapter
        binding.actionsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !recyclerView.canScrollVertically(1)) viewModel.loadNextPage()
            }
        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (!filtersCreated) {
                createFilters(state.categories)
                filtersCreated = true
            }
            updateSelectedFilter(state.selectedFilter)
            adapter.submitList(state.items)
            binding.loadingProgress.visibility =
                if (state.loading && state.items.isEmpty()) View.VISIBLE else View.GONE
            binding.emptyState.visibility =
                if (!state.loading && state.items.isEmpty()) View.VISIBLE else View.GONE
            binding.actionsRecycler.visibility = if (state.items.isEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) viewModel.refresh()
    }

    override fun onDestroyView() {
        binding.actionsRecycler.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun createFilters(categories: List<UserActionCategory>) {
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.iran_sans)
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                tag = category.key
                text = category.title
                isCheckable = true
                isCheckedIconVisible = false
                chipMinHeight = resources.getDimension(R.dimen.action_filter_chip_height)
                chipCornerRadius = resources.getDimension(R.dimen.action_filter_chip_radius)
                chipStrokeWidth = resources.getDimension(R.dimen.action_filter_chip_stroke)
                chipStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.app_outline),
                )
                chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.action_filter_chip_background,
                )
                setTextColor(
                    ContextCompat.getColorStateList(requireContext(), R.color.action_filter_chip_text),
                )
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.NORMAL)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) viewModel.selectFilter(category.key)
                }
            }
            binding.actionFilters.addView(chip)
        }
    }

    private fun updateSelectedFilter(selectedKey: String) {
        for (index in 0 until binding.actionFilters.childCount) {
            val chip = binding.actionFilters.getChildAt(index) as? Chip ?: continue
            chip.isChecked = chip.tag == selectedKey
        }
    }
}

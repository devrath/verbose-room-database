package com.example.code.ui.reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.code.App
import com.example.code.R
import com.example.code.databinding.FragmentReviewsBinding
import com.example.code.model.relations.BookReview
import com.example.code.ui.addReview.AddBookReviewActivity
import com.example.code.ui.bookReviewDetails.BookReviewDetailsActivity
import com.example.code.utils.createAndShowDialog
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Fetches and displays notes from the API.
 */
private const val REQUEST_CODE_ADD_REVIEW = 102

class BookReviewsFragment : Fragment() {

  private var _binding: FragmentReviewsBinding? = null
  private val binding get() = _binding!!


  private val adapter by lazy { BookReviewAdapter(::onItemSelected, ::onItemLongTapped) }
  private val repository by lazy { App.repository }
  private val bookReviewsFlow by lazy { repository.getReviewsFlow() }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = FragmentReviewsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initListeners()
    initUi()
    loadBookReviews()
  }

  private fun initUi() {
    binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
    binding.reviewsRecyclerView.adapter = adapter
    binding.pullToRefresh.isEnabled = false
  }

  private fun initListeners() {
    binding.addBookReview.setOnClickListener {
      startActivityForResult(
          AddBookReviewActivity.getIntent(requireContext()), REQUEST_CODE_ADD_REVIEW
      )
    }
  }

  private fun onItemSelected(item: BookReview) {
    startActivity(BookReviewDetailsActivity.getIntent(requireContext(), item))
  }

  private fun onItemLongTapped(item: BookReview) {
    createAndShowDialog(requireContext(),
        getString(R.string.delete_title),
        getString(R.string.delete_review_message, item.book.name),
        onPositiveAction = { removeReviewFromRepo(item) })
  }

  private fun removeReviewFromRepo(item: BookReview) {
    lifecycleScope.launch {
      repository.removeReview(item.review)
      loadBookReviews()
    }
  }

  private fun loadBookReviews() = lifecycleScope.launch {
    bookReviewsFlow.catch { error ->
      error.printStackTrace()
    }.collect { bookReviews ->
      adapter.setData(bookReviews)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
package com.istudio.code.presentation.modules.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istudio.code.core.platform.functional.UseCaseResult
import com.istudio.code.core.platform.uiEvent.UiText
import com.istudio.code.domain.database.models.Book
import com.istudio.code.domain.database.models.Review
import com.istudio.code.domain.usecases.useCaseMain.AddBookUseCases
import com.istudio.code.presentation.modules.home.states.myBooks.MyBooksEvent
import com.istudio.code.presentation.modules.home.states.myBooks.MyBooksUiEvent
import com.istudio.code.presentation.modules.home.states.myBooks.MyBooksUiState
import com.istudio.code.presentation.modules.home.states.myReviews.MyReviewsEvent
import com.istudio.code.presentation.modules.home.states.myReviews.MyReviewsUIState
import com.istudio.code.presentation.modules.home.states.myReviews.MyReviewsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeVm @Inject constructor(
    private val addBookUseCases: AddBookUseCases,
) : ViewModel() {

    /** ****** Data states from Presentation layer attached to VM ****** **/
    /** DATA STATE: <--------> My Books <--------> **/
    var viewStateMyBooks by mutableStateOf(MyBooksUiState())
        private set
    /** DATA STATE: <--------> My Reviews <--------> **/
    var viewStateMyReviews by mutableStateOf(MyReviewsUIState())
        private set
    /** ****** Data states from Presentation layer attached to VM ****** **/



    /** ****** View model sets this state from inside VM, Composable observes this state ****** **/
    /** UI EVENT: <--------> My Books <--------> **/
    private val _uiEventMyBooks = Channel<MyBooksEvent>()
    val uiEventMyBooks = _uiEventMyBooks.receiveAsFlow()
    /** UI EVENT: <--------> My Reviews <--------> **/
    private val _uiEventMyReviews = Channel<MyReviewsEvent>()
    val uiEventMyReviews = _uiEventMyReviews.receiveAsFlow()
    /** ****** View model sets this state from inside VM, Composable observes this state ****** **/


    /** ****** VM observes the changes and Composable sets the changes form composable ****** **/
    /** ON EVENT: <--------> My Books <--------> **/
    fun onEvent(event: MyBooksUiEvent) {
        viewModelScope.launch {
            when (event) {
                is MyBooksUiEvent.GetMyBooks -> {
                    // Retrieve books from Database
                    retrieveAllBooks()
                }
                is MyBooksUiEvent.DeleteBook -> {
                    // Deleting the book from database
                    deleteBook(event.book)
                }

                is MyBooksUiEvent.ConfirmDeleteBook -> {
                    viewStateMyBooks = viewStateMyBooks.copy(book = event.book)
                }
            }
        }
    }
    /** ON EVENT: <--------> My Reviews <--------> **/
    fun onEvent(event: MyReviewsUiEvent){
        viewModelScope.launch {
            when (event) {
                is MyReviewsUiEvent.GetMyReviews -> {
                    // Retrieve reviews from Database
                    retrieveAllReviews()
                }
                is MyReviewsUiEvent.DeleteReview -> {
                    // Deleting the book from database
                    deleteReview(event.review)
                }
                is MyReviewsUiEvent.ConfirmDeleteReview -> {
                    viewStateMyReviews = viewStateMyReviews.copy(review = event.review)
                }
            }
        }
    }
    /** ****** VM observes the changes and Composable sets the changes form composable ****** **/


    /** <*********************> Use case invocations <*******************> **/

    /** <*******> Use case <My Books> <*******> **/
    /**
     * USE-CASE: Retrieving all books from database
     */
    private fun retrieveAllBooks() {
        addBookUseCases.getBooksAndGenreUseCase.invoke()
            .onSuccess {
                viewStateMyBooks = viewStateMyBooks.copy(books = it)
            }.onFailure {
                viewModelScope.launch {
                    useCaseError(UseCaseResult.Error(Exception(it)))
                }
            }
    }
    /**
     * USE-CASE: Deleting book from database
     */
    private fun deleteBook(book: Book) {
        viewModelScope.launch {
            addBookUseCases.deleteBookUseCase
                .invoke(book).onSuccess {
                    _uiEventMyBooks.send(MyBooksEvent.RefreshData)
                    //retrieveAllBooks()
                }.onFailure {
                    viewModelScope.launch {
                        useCaseError(UseCaseResult.Error(Exception(it)))
                    }
                }
        }
    }
    /** <*******> Use case <My Books> <*******> **/

    /** <*******> Use case <Reviews> <********> **/
    /**
     * USE-CASE: Retrieving all reviews of all books
     */
    private fun retrieveAllReviews() {
        TODO("Not yet implemented")
    }
    /**
     * USE-CASE: Deleting review from database
     */
    private fun deleteReview(review: Review) {

    }
    /** <*******> Use case <Reviews> <********> **/

    /** <*********************> Use case invocations <*******************> **/




    /** ********************************* DISPLAY MESSAGES ****************************************/
    /**
     * ERROR HANDLING:
     * Displaying messages to the snack-bar
     */
    private suspend fun useCaseErrorMessage(result: UiText?) {
        result?.let { _uiEventMyBooks.send(MyBooksEvent.ShowSnackBar(it.toString())) }
    }

    /**
     * ERROR HANDLING:
     * For the Use cases
     */
    private suspend fun useCaseError(result: UseCaseResult.Error) {
        val uiEvent = UiText.DynamicString(result.exception.message.toString())
        _uiEventMyBooks.send(MyBooksEvent.ShowSnackBar(uiEvent.text))
    }
    /** ********************************* DISPLAY MESSAGES ****************************************/



}
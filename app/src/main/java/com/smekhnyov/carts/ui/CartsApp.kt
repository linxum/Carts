package com.smekhnyov.carts.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smekhnyov.carts.data.CardDraft
import com.smekhnyov.carts.ui.add.AddCardScreen
import com.smekhnyov.carts.ui.detail.CardDetailScreen
import com.smekhnyov.carts.ui.wallet.WalletScreen
import kotlinx.coroutines.launch

private const val ROUTE_WALLET = "wallet"
private const val ROUTE_DETAIL = "detail"
private const val ROUTE_ADD = "add"

private sealed interface Route {
    data object Wallet : Route
    data class Detail(val cardId: Long) : Route
    data class Add(val cardId: Long?) : Route
}

@Composable
fun CartsApp(viewModel: CardsViewModel = viewModel()) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var routeKey by rememberSaveable { mutableStateOf(ROUTE_WALLET) }
    var selectedCardId by rememberSaveable { mutableLongStateOf(-1L) }
    var editingCardId by rememberSaveable { mutableLongStateOf(-1L) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(text: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(text)
        }
    }

    fun goToWallet() {
        routeKey = ROUTE_WALLET
        selectedCardId = -1L
        editingCardId = -1L
    }

    val route: Route = when (routeKey) {
        ROUTE_DETAIL -> Route.Detail(selectedCardId)
        ROUTE_ADD -> Route.Add(editingCardId.takeIf { it > 0L })
        else -> Route.Wallet
    }

    BackHandler(enabled = route !is Route.Wallet) {
        goToWallet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AnimatedContent(
            targetState = route,
            transitionSpec = {
                when {
                    targetState is Route.Add -> {
                        (
                            slideInVertically(
                                animationSpec = tween(360),
                                initialOffsetY = { it },
                            ) + fadeIn(tween(220))
                            ) togetherWith (
                            slideOutVertically(
                                animationSpec = tween(300),
                                targetOffsetY = { -it / 8 },
                            ) + fadeOut(tween(180))
                            )
                    }

                    initialState is Route.Add -> {
                        (
                            slideInVertically(
                                animationSpec = tween(320),
                                initialOffsetY = { -it / 10 },
                            ) + fadeIn(tween(220))
                            ) togetherWith (
                            slideOutVertically(
                                animationSpec = tween(320),
                                targetOffsetY = { it },
                            ) + fadeOut(tween(180))
                            )
                    }

                    targetState is Route.Detail -> {
                        (
                            fadeIn(tween(230)) + scaleIn(
                            animationSpec = tween(300),
                            initialScale = 0.94f,
                        )
                            ) togetherWith (
                            fadeOut(tween(160)) + scaleOut(
                            animationSpec = tween(260),
                            targetScale = 1.015f,
                        )
                            )
                    }

                    initialState is Route.Detail -> {
                        (
                            fadeIn(tween(240)) + scaleIn(
                            animationSpec = tween(300),
                            initialScale = 1.015f,
                        )
                            ) togetherWith (
                            fadeOut(tween(160)) + scaleOut(
                            animationSpec = tween(260),
                            targetScale = 0.985f,
                        )
                            )
                    }

                    else -> fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                }
            },
            label = "appRoute",
        ) { target ->
            when (target) {
                Route.Wallet -> {
                    WalletScreen(
                        cards = cards,
                        isLoading = isLoading,
                        onCardClick = { card ->
                            selectedCardId = card.id
                            routeKey = ROUTE_DETAIL
                        },
                        onAddCard = {
                            editingCardId = -1L
                            routeKey = ROUTE_ADD
                        },
                    )
                }

                is Route.Detail -> {
                    val card = cards.firstOrNull { it.id == target.cardId }
                    if (card == null) {
                        MissingCard(
                            onBack = ::goToWallet,
                        )
                    } else {
                        CardDetailScreen(
                            card = card,
                            onBack = ::goToWallet,
                            onEdit = {
                                editingCardId = card.id
                                routeKey = ROUTE_ADD
                            },
                            onDelete = {
                                viewModel.deleteCard(card) { success ->
                                    if (success) {
                                        goToWallet()
                                        showMessage("Карта удалена")
                                    } else {
                                        showMessage("Не удалось удалить карту")
                                    }
                                }
                            },
                            onMessage = ::showMessage,
                        )
                    }
                }

                is Route.Add -> {
                    val initialCard = target.cardId?.let { id ->
                        cards.firstOrNull { it.id == id }
                    }
                    AddCardScreen(
                        initialCard = initialCard,
                        onClose = ::goToWallet,
                        onSave = { draft: CardDraft, onResult: (Boolean) -> Unit ->
                            viewModel.saveCard(draft) { success ->
                                onResult(success)
                                if (success) {
                                    val message = if (draft.id == null) {
                                        "Карта добавлена в кошелёк"
                                    } else {
                                        "Изменения сохранены"
                                    }
                                    goToWallet()
                                    showMessage(message)
                                }
                            }
                        },
                        onMessage = ::showMessage,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 96.dp),
        )
    }
}

@Composable
private fun MissingCard(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

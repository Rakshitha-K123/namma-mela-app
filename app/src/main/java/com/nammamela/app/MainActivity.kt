package com.nammamela.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nammamela.app.ui.theme.NammaMelaTheme
import com.nammamela.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NammaMelaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NammaMelaApp()
                }
            }
        }
    }
}

data class DramaShow(
    val id: Int,
    val title: String,
    val troupe: String,
    val village: String,
    val date: String,
    val language: String,
    val price: Int,
    val description: String,
    val bookedSeats: Set<String>
)

data class ApplauseComment(
    val author: String,
    val village: String,
    val message: String,
    val rating: Int
)

data class Ticket(
    val id: String,
    val show: DramaShow,
    val seats: Set<String>,
    val totalAmount: Int,
    val bookingTime: String
)

data class Offer(
    val title: String,
    val description: String,
    val color: Color
)

data class NammaMelaUiState(
    val shows: List<DramaShow> = emptyList(),
    val selectedShowId: Int = 1,
    val selectedSeats: Set<String> = emptySet(),
    val applause: List<ApplauseComment> = emptyList(),
    val searchQuery: String = "",
    val selectedVillageFilter: String? = null,
    val myTickets: List<Ticket> = emptyList()
) {
    val selectedShow: DramaShow
        get() = shows.first { it.id == selectedShowId }

    val filteredShows: List<DramaShow>
        get() = shows.filter { show ->
            val matchesSearch = show.title.contains(searchQuery, ignoreCase = true) ||
                    show.village.contains(searchQuery, ignoreCase = true)
            val matchesVillage = selectedVillageFilter == null || show.village == selectedVillageFilter
            matchesSearch && matchesVillage
        }
    
    val availableVillages: List<String>
        get() = shows.map { it.village }.distinct().sorted()
}

class NammaMelaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        NammaMelaUiState(
            shows = sampleShows,
            applause = sampleApplause
        )
    )

    val uiState: StateFlow<NammaMelaUiState> = _uiState

    fun selectShow(showId: Int) {
        _uiState.update { state ->
            state.copy(selectedShowId = showId, selectedSeats = emptySet())
        }
    }

    fun toggleSeat(seat: String) {
        _uiState.update { state ->
            if (seat in state.selectedShow.bookedSeats) {
                state
            } else {
                val selected = state.selectedSeats
                state.copy(
                    selectedSeats = if (seat in selected) selected - seat else selected + seat
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectVillageFilter(village: String?) {
        _uiState.update { it.copy(selectedVillageFilter = village) }
    }

    fun completeBooking() {
        _uiState.update { state ->
            val newTicket = Ticket(
                id = "NM-${(1000..9999).random()}",
                show = state.selectedShow,
                seats = state.selectedSeats,
                totalAmount = state.selectedSeats.size * state.selectedShow.price,
                bookingTime = "Just now"
            )
            state.copy(
                myTickets = listOf(newTicket) + state.myTickets,
                selectedSeats = emptySet()
            )
        }
    }
}

private enum class AppScreen {
    Home,
    Shows,
    Booking,
    Applause,
    Checkout,
    Success,
    Tickets
}

private enum class AppTab(val screen: AppScreen, val label: String, val icon: ImageVector) {
    Home(AppScreen.Home, "Home", Icons.Default.Home),
    Shows(AppScreen.Shows, "Explore", Icons.AutoMirrored.Filled.EventNote),
    Tickets(AppScreen.Tickets, "My Tickets", Icons.Default.ConfirmationNumber),
    Applause(AppScreen.Applause, "Applause", Icons.Default.Campaign)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NammaMelaApp(viewModel: NammaMelaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (currentScreen != AppScreen.Checkout && currentScreen != AppScreen.Success) {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentScreen == tab.screen,
                            onClick = { currentScreen = tab.screen },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (currentScreen) {
            AppScreen.Home -> HomeScreen(uiState, padding) {
                currentScreen = AppScreen.Shows
            }

            AppScreen.Shows -> ShowsScreen(
                uiState = uiState,
                paddingValues = padding,
                onSelectShow = viewModel::selectShow,
                onBookShow = { currentScreen = AppScreen.Booking },
                onQueryChange = viewModel::updateSearchQuery,
                onVillageSelect = viewModel::selectVillageFilter
            )

            AppScreen.Booking -> BookingScreen(
                uiState = uiState,
                paddingValues = padding,
                onToggleSeat = viewModel::toggleSeat,
                onProceedToCheckout = { currentScreen = AppScreen.Checkout }
            )

            AppScreen.Applause -> ApplauseScreen(uiState, padding)

            AppScreen.Checkout -> CheckoutScreen(
                uiState = uiState,
                paddingValues = padding,
                onBack = { currentScreen = AppScreen.Booking },
                onPaymentComplete = {
                    viewModel.completeBooking()
                    currentScreen = AppScreen.Success
                }
            )

            AppScreen.Success -> BookingSuccessScreen(
                uiState = uiState,
                paddingValues = padding,
                onDone = { currentScreen = AppScreen.Tickets }
            )

            AppScreen.Tickets -> MyTicketsScreen(uiState, padding)
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: NammaMelaUiState,
    paddingValues: PaddingValues,
    onExplore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            OfferCarousel()
        }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                HeroPanel(show = uiState.selectedShow, onBookNow = onExplore)
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionTitle("Tonight's Highlights")
            }
        }
        items(uiState.shows.take(3)) { show ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CompactShowCard(show = show)
            }
        }
    }
}

@Composable
private fun OfferCarousel() {
    val offers = listOf(
        Offer("Summer Special", "Get 20% off on all weekend shows!", Color(0xFF1E3A8A)),
        Offer("Early Bird", "Book 5 days in advance for extra snacks voucher.", Color(0xFF064E3B)),
        Offer("Group Booking", "Buy 10 tickets, get 2 free for your troupe!", Color(0xFF4C1D95)),
        Offer("Festival Dhamaka", "Celebrate Ugadi with special folk performances.", Color(0xFF78350F))
    )
    val pagerState = rememberPagerState(pageCount = { offers.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % offers.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 12.dp
    ) { page ->
        val offer = offers[page]
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = offer.color),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = offer.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = offer.description,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HeroPanel(show: DramaShow, onBookNow: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF7C2D12), Color(0xFFBE123C), Color(0xFFF59E0B))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("Featured rural drama") },
                    leadingIcon = { Icon(Icons.Default.TheaterComedy, contentDescription = null) }
                )
                Text(
                    text = show.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = show.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFF7ED)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(show.village, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(show.date, color = Color(0xFFFFEDD5))
                    }
                    Button(onClick = onBookNow) {
                        Text("Explore Shows")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowsScreen(
    uiState: NammaMelaUiState,
    paddingValues: PaddingValues,
    onSelectShow: (Int) -> Unit,
    onBookShow: () -> Unit,
    onQueryChange: (String) -> Unit,
    onVillageSelect: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        SearchAndFilterHeader(
            query = uiState.searchQuery,
            onQueryChange = onQueryChange,
            villages = uiState.availableVillages,
            selectedVillage = uiState.selectedVillageFilter,
            onVillageSelect = onVillageSelect
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.filteredShows.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No shows found for your search.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(uiState.filteredShows) { show ->
                    ShowCard(
                        show = show,
                        selected = show.id == uiState.selectedShowId,
                        onSelect = { onSelectShow(show.id) },
                        onBookShow = onBookShow
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    villages: List<String>,
    selectedVillage: String?,
    onVillageSelect: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search by show or village...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedVillage == null,
                onClick = { onVillageSelect(null) },
                label = { Text("All") }
            )
            villages.forEach { village ->
                FilterChip(
                    selected = selectedVillage == village,
                    onClick = { onVillageSelect(village) },
                    label = { Text(village) }
                )
            }
        }
    }
}

@Composable
private fun ShowCard(
    show: DramaShow,
    selected: Boolean,
    onSelect: () -> Unit,
    onBookShow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(show.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(show.description, style = MaterialTheme.typography.bodyMedium)
            InfoRow(Icons.Default.TheaterComedy, "${show.troupe} • ${show.language}")
            InfoRow(Icons.Default.LocationOn, "${show.village} • ${show.date}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoRow(Icons.Default.Payments, "Rs ${show.price} per seat")
                Button(
                    onClick = {
                        onSelect()
                        onBookShow()
                    }
                ) {
                    Text("Select Seats")
                }
            }
        }
    }
}

@Composable
private fun BookingScreen(
    uiState: NammaMelaUiState,
    paddingValues: PaddingValues,
    onToggleSeat: (String) -> Unit,
    onProceedToCheckout: () -> Unit
) {
    val show = uiState.selectedShow
    val seatRows = listOf("A", "B", "C", "D", "E")
    val seats = seatRows.flatMap { row -> (1..8).map { number -> "$row$number" } }
    val total = uiState.selectedSeats.size * show.price

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Seat Selection")
        CompactShowCard(show = show)
        StageLabel()
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(seats) { seat ->
                SeatButton(
                    seat = seat,
                    isBooked = seat in show.bookedSeats,
                    isSelected = seat in uiState.selectedSeats,
                    onToggleSeat = onToggleSeat
                )
            }
        }
        BookingSummary(
            seatCount = uiState.selectedSeats.size,
            total = total,
            onProceed = onProceedToCheckout
        )
    }
}

@Composable
private fun SeatButton(
    seat: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onToggleSeat: (String) -> Unit
) {
    val background = when {
        isBooked -> Color(0xFFE5E7EB)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val foreground = when {
        isBooked -> Color(0xFF6B7280)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .clickable(enabled = !isBooked) { onToggleSeat(seat) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat,
            color = foreground,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BookingSummary(seatCount: Int, total: Int, onProceed: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Selected seats", style = MaterialTheme.typography.labelLarge)
                    Text("$seatCount seats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", style = MaterialTheme.typography.labelLarge)
                    Text("Rs $total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
                enabled = seatCount > 0
            ) {
                Text("Proceed to Checkout")
            }
        }
    }
}

@Composable
private fun CheckoutScreen(
    uiState: NammaMelaUiState,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    val show = uiState.selectedShow
    val total = uiState.selectedSeats.size * show.price

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionTitle("Confirm Payment")
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Show Summary", fontWeight = FontWeight.Bold)
                Text(show.title, style = MaterialTheme.typography.titleMedium)
                Text("${show.village} • ${show.date}")
                Text("Seats: ${uiState.selectedSeats.joinToString(", ")}")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Bold)
                    Text("Rs $total", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text("Select Payment Method", fontWeight = FontWeight.Bold)
        
        listOf("UPI (PhonePe/Google Pay)", "Credit/Debit Card", "Net Banking", "Cash at Counter").forEach { method ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Text(method)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Back")
            }
            Button(
                onClick = onPaymentComplete,
                modifier = Modifier.weight(1f)
            ) {
                Text("Confirm & Pay Rs $total")
            }
        }
    }
}

@Composable
private fun BookingSuccessScreen(
    uiState: NammaMelaUiState,
    paddingValues: PaddingValues,
    onDone: () -> Unit
) {
    val latestTicket = uiState.myTickets.firstOrNull() ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF166534)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Booking Confirmed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF166534)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Your seats for ${latestTicket.show.title} are secured.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        TicketCard(ticket = latestTicket)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View My Tickets")
        }
    }
}

@Composable
private fun MyTicketsScreen(uiState: NammaMelaUiState, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle("My Tickets")
        }
        
        if (uiState.myTickets.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("No tickets yet. Book a show to see it here!", color = Color.Gray)
                    }
                }
            }
        } else {
            items(uiState.myTickets) { ticket ->
                TicketCard(ticket = ticket)
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ID: ${ticket.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    ticket.bookingTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))

            Text(ticket.show.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${ticket.show.village} • ${ticket.show.date}", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Seats", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(ticket.seats.joinToString(", "), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR Code", modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ApplauseScreen(uiState: NammaMelaUiState, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Applause Wall")
        }
        items(uiState.applause) { comment ->
            ApplauseCard(comment)
        }
    }
}

@Composable
private fun ApplauseCard(comment: ApplauseComment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(comment.author, fontWeight = FontWeight.Bold)
                    Text(comment.village, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(comment.message, style = MaterialTheme.typography.bodyLarge)
            Text("Rating: ${"*".repeat(comment.rating)}", color = Color(0xFFD97706))
        }
    }
}

@Composable
private fun CompactShowCard(show: DramaShow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(show.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${show.troupe} • ${show.village}", style = MaterialTheme.typography.bodyMedium)
            Text("${show.date} • Rs ${show.price}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StageLabel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111827)),
        contentAlignment = Alignment.Center
    ) {
        Text("STAGE", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private val sampleShows = listOf(
    DramaShow(
        id = 1,
        title = "Kereya Kanasu",
        troupe = "Malenadu Rangatanda",
        village = "Hosanagara",
        date = "May 10, 7:30 PM",
        language = "Kannada",
        price = 120,
        description = "A family drama about water, land, and the courage of a village that chooses hope.",
        bookedSeats = setOf("A1", "A2", "B4", "C3", "D7", "E8")
    ),
    DramaShow(
        id = 2,
        title = "Therina Belaku",
        troupe = "Janapada Kala Balaga",
        village = "Mandya",
        date = "May 12, 8:00 PM",
        language = "Kannada",
        price = 100,
        description = "Folk music and sharp comedy meet in a lively play about a traveling theater cart.",
        bookedSeats = setOf("A5", "B2", "B3", "C6", "E1")
    ),
    DramaShow(
        id = 3,
        title = "Mannina Gejje",
        troupe = "Namma Ooru Nataka Sangha",
        village = "Dharwad",
        date = "May 15, 7:00 PM",
        language = "Kannada",
        price = 90,
        description = "A village festival story with songs, satire, and a warm look at changing traditions.",
        bookedSeats = setOf("A8", "B8", "C1", "C2", "D4")
    ),
    DramaShow(
        id = 4,
        title = "Raja Vikrama",
        troupe = "Kalpatharu Kala Tanda",
        village = "Tumakuru",
        date = "May 18, 7:30 PM",
        language = "Kannada",
        price = 150,
        description = "A historical epic depicting the life and struggles of a legendary local ruler.",
        bookedSeats = setOf("A1", "B1", "C1")
    ),
    DramaShow(
        id = 5,
        title = "Samsara Sagara",
        troupe = "Ranga Belaku",
        village = "Shivamogga",
        date = "May 20, 8:00 PM",
        language = "Kannada",
        price = 110,
        description = "A poignant exploration of urban-rural migration and its impact on traditional families.",
        bookedSeats = setOf("D1", "D2", "E3")
    ),
    DramaShow(
        id = 6,
        title = "Halli Chitra",
        troupe = "Grama Samskruti Balaga",
        village = "Hassan",
        date = "May 22, 7:00 PM",
        language = "Kannada",
        price = 80,
        description = "A collection of short stories brought to life through folk dance and storytelling.",
        bookedSeats = setOf("B5", "C5")
    ),
    DramaShow(
        id = 7,
        title = "Bhoomi Taayi",
        troupe = "Raitha Dhwani",
        village = "Haveri",
        date = "May 25, 7:30 PM",
        language = "Kannada",
        price = 100,
        description = "A powerful drama centered around sustainable farming and rural empowerment.",
        bookedSeats = setOf("A2", "A3")
    ),
    DramaShow(
        id = 8,
        title = "Neralu Belaku",
        troupe = "Chaya Nataka Tanda",
        village = "Udupi",
        date = "May 28, 8:00 PM",
        language = "Kannada",
        price = 130,
        description = "Shadow puppetry combined with live acting, telling ancient coastal legends.",
        bookedSeats = setOf("E5", "E6")
    )
)

private val sampleApplause = listOf(
    ApplauseComment(
        author = "Shanthi",
        village = "Hosanagara",
        message = "The actors made the whole village feel seen. Beautiful dialogues and strong music.",
        rating = 5
    ),
    ApplauseComment(
        author = "Ramesh",
        village = "Mandya",
        message = "Easy booking, clear seats, and a wonderful evening with family.",
        rating = 4
    ),
    ApplauseComment(
        author = "Nazia",
        village = "Dharwad",
        message = "Loved the folk songs. Please bring this troupe again next month.",
        rating = 5
    )
)

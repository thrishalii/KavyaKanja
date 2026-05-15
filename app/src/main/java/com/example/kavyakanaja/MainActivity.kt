package com.example.kavyakanaja

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kavyakanaja.data.Poem
import com.example.kavyakanaja.data.PoetBio
import com.example.kavyakanaja.data.poetBios
import com.example.kavyakanaja.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: KavyaViewModel = viewModel()
            KavyaKanajaTheme(darkTheme = viewModel.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigator(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigator(viewModel: KavyaViewModel) {
    var screenState by rememberSaveable { mutableStateOf(if (viewModel.currentUser != null) "HOME" else "LOGIN") }

    Crossfade(targetState = screenState, label = "ScreenTransition") { state ->
        when (state) {
            "LOGIN" -> LoginScreen(
                onLoginSuccess = { 
                    viewModel.updateCurrentUser()
                    screenState = "HOME" 
                },
                onNavigateToSignUp = { screenState = "SIGNUP" }
            )
            "SIGNUP" -> SignUpScreen(
                onSignUpSuccess = { screenState = "LOGIN" },
                onNavigateToLogin = { screenState = "LOGIN" }
            )
            "HOME" -> KavyaApp(
                viewModel = viewModel,
                onLogout = { 
                    viewModel.signOut()
                    screenState = "LOGIN" 
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KavyaApp(viewModel: KavyaViewModel, onLogout: () -> Unit) {
    var selectedPoem by remember { mutableStateOf<Poem?>(null) }
    var selectedPoetName by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (selectedPoetName != null) "Poet Profile" else if(selectedPoem != null) "Reading" else "Kavya Kanaja",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    if (selectedPoem != null || selectedPoetName != null) {
                        IconButton(onClick = { 
                            if (selectedPoem != null) selectedPoem = null else selectedPoetName = null 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(if (viewModel.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                    }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = CrimsonRed) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (selectedPoem == null && selectedPoetName == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0, 
                        onClick = { currentTab = 0 }, 
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) }, 
                        label = { Text("Library") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1, 
                        onClick = { currentTab = 1 }, 
                        icon = { Icon(Icons.Default.HistoryEdu, null) }, 
                        label = { Text("Masters") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2, 
                        onClick = { currentTab = 2 }, 
                        icon = { Icon(Icons.Default.Favorite, null) }, 
                        label = { Text("Favorites") }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(
                targetState = Triple(selectedPoem, selectedPoetName, currentTab),
                transitionSpec = { 
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f)) togetherWith 
                    fadeOut(animationSpec = tween(300)) 
                },
                label = "ContentChange"
            ) { (poem, poet, tab) ->
                if (poem != null) {
                    PoemDetailScreen(poem, viewModel)
                } else if (poet != null) {
                    val bio = poetBios[poet] ?: PoetBio(poet, poet, "N/A", emptyList(), "Biography coming soon.", emptyList(), emptyList())
                    PoetProfileScreen(bio, viewModel) { selectedPoem = it }
                } else {
                    when (tab) {
                        0 -> PoemListScreen(viewModel.filteredPoems, viewModel) { selectedPoem = it }
                        1 -> PoetsScreen(viewModel.poems) { selectedPoetName = it }
                        2 -> PoemListScreen(viewModel.favoritePoems, viewModel) { selectedPoem = it }
                    }
                }
            }
        }
    }
}

@Composable
fun PoemListScreen(poems: List<Poem>, viewModel: KavyaViewModel, onClick: (Poem) -> Unit) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), Color.Transparent))
        ).padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.searchQuery, onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.fillMaxWidth(), 
                placeholder = { Text("Search by title, poet or category...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) }, 
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        
        if (poems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoStories, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("No poems found", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(poems) { poem ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).clickable { onClick(poem) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(poem.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(poem.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(8.dp))
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(poem.category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(poem) }) {
                            Icon(if (viewModel.isFavorite(poem)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = CrimsonRed, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PoetsScreen(poems: List<Poem>, onPoetClick: (String) -> Unit) {
    val poets = poems.map { it.author }.distinct()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), 
        contentPadding = PaddingValues(16.dp), 
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        items(poets) { poet ->
            ElevatedCard(
                modifier = Modifier.padding(8.dp).aspectRatio(1f).clickable { onPoetClick(poet) }, 
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = poet, 
                            fontWeight = FontWeight.ExtraBold, 
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoetProfileScreen(bio: PoetBio, viewModel: KavyaViewModel, onPoemClick: (Poem) -> Unit) {
    val collectedWorks = remember(bio.name, viewModel.poems) {
        viewModel.poems.filter { it.author.contains(bio.name, ignoreCase = true) || bio.name.contains(it.author, ignoreCase = true) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            Column(modifier = Modifier.fillMaxWidth().background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent))
            ).padding(24.dp)) {
                Text(bio.canonicalName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(bio.lifespan, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(24.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("BIOGRAPHY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Text(bio.bio, textAlign = TextAlign.Justify, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    }
                }
                
                if (bio.famousWorks.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("CELEBRATED WORKS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text(bio.famousWorks.joinToString(", "), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                }

                if (bio.awards.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("HONORS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    FlowRow(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bio.awards.forEach { award -> 
                            SuggestionChip(onClick = {}, label = { Text(award) }, shape = RoundedCornerShape(12.dp)) 
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text("COLLECTED WORKS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        if (collectedWorks.isEmpty()) {
            item {
                Text("No collected works available.", modifier = Modifier.padding(horizontal = 24.dp), color = Color.Gray)
            }
        } else {
            items(collectedWorks) { poem ->
                ListItem(
                    headlineContent = { Text(poem.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(poem.category, style = MaterialTheme.typography.bodySmall) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onPoemClick(poem) }.padding(horizontal = 8.dp),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
fun PoemDetailScreen(poem: Poem, viewModel: KavyaViewModel) {
    var showMeaning by remember { mutableStateOf(false) }
    val isDark = viewModel.isDarkTheme

    Column(modifier = Modifier.fillMaxSize().background(if(isDark) Color.Black else AntiquePaper)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(20.dp).shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(poem.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("By ${poem.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showMeaning = !showMeaning }, 
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { 
                        Text(if (showMeaning) "Hide Meaning" else "Show Meaning", fontWeight = FontWeight.Bold) 
                    }
                    Spacer(Modifier.weight(1f))
                    FloatingActionButton(
                        onClick = { if (viewModel.isPlaying) viewModel.stopSpeaking() else viewModel.speakPoem(poem, showMeaning) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(if (viewModel.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, null, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            itemsIndexed(poem.lines) { index, line ->
                if (line.text.isEmpty()) {
                    Spacer(Modifier.height(32.dp))
                } else {
                    val isHighlighted = viewModel.currentSpokenLineIndex == index
                    val scale by animateFloatAsState(if (isHighlighted) 1.05f else 1f, label = "Pulse")
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if(isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = line.text, 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = if(isHighlighted) FontWeight.Bold else FontWeight.Medium, 
                            fontFamily = FontFamily.Serif, 
                            color = if(isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            lineHeight = 32.sp
                        )
                        AnimatedVisibility(visible = showMeaning && line.meaning.isNotEmpty()) {
                            Text(
                                text = line.meaning, 
                                style = MaterialTheme.typography.bodyLarge, 
                                color = if(isHighlighted) MaterialTheme.colorScheme.secondary else Color.Gray, 
                                modifier = Modifier.padding(top = 10.dp),
                                fontStyle = FontStyle.Italic,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(120.dp)) }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    fun isValidEmail(target: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(DeepIndigo, RoyalPurple))
        ), 
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp).shadow(24.dp, RoundedCornerShape(32.dp)), 
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.padding(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Text("Kavya Kanaja", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("Digital Treasury of Kannada Poetry", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                Spacer(Modifier.height(40.dp))
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { 
                        email = it
                        emailError = if (it.isEmpty() || isValidEmail(it)) null else "Please enter a valid email."
                    },
                    label = { Text("Email Address") },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (isValidEmail(email) && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { 
                                if (it.isSuccessful) onLoginSuccess() 
                                else Toast.makeText(context, "Login Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else if (!isValidEmail(email)) {
                            emailError = "Please enter a valid email address."
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { 
                    Text("LOGIN", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) 
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onNavigateToSignUp) { 
                    Text("New here? Join the Treasury", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium) 
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    fun isValidEmail(target: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(RoyalPurple, DeepTeal))
        ), 
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp).shadow(24.dp, RoundedCornerShape(32.dp)), 
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Join the Treasury", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("Protect and preserve our heritage", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { 
                        email = it
                        emailError = if (it.isEmpty() || isValidEmail(it)) null else "Please enter a valid email."
                    },
                    label = { Text("Email Address") },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password (Min 6 chars)") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.LockClock, null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (isValidEmail(email) && password == confirmPassword && password.length >= 6) {
                            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    auth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })
                                    onSignUpSuccess()
                                } else Toast.makeText(context, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "Password is too short", Toast.LENGTH_SHORT).show()
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { 
                    Text("SIGN UP", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) 
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onNavigateToLogin) { 
                    Text("Already a member? Sign in", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

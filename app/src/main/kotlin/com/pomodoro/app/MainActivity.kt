// ═══════════════════════════════════════════════════════════════════
//  ██████╗  ██████╗ ███╗   ███╗ ██████╗ ██████╗  ██████╗
//  ██╔══██╗██╔═══██╗████╗ ████║██╔═══██╗██╔══██╗██╔═══██╗
//  ██████╔╝██║   ██║██╔████╔██║██║   ██║██║  ██║██║   ██║
//  ██╔═══╝ ██║   ██║██║╚██╔╝██║██║   ██║██║  ██║██║   ██║
//  ██║     ╚██████╔╝██║ ╚═╝ ██║╚██████╔╝██████╔╝╚██████╔╝
//  ╚═╝      ╚═════╝ ╚═╝     ╚═╝ ╚═════╝ ╚═════╝  ╚═════╝
//  TIMER APP — Complete Android App in ONE file
// ═══════════════════════════════════════════════════════════════════
//
//  HOW TO READ THIS FILE:
//  This file is structured top-to-bottom, like a recipe:
//
//    SECTION 1 → Package declaration & Imports
//    SECTION 2 → Timer modes (Work / Short Break / Long Break)
//    SECTION 3 → ViewModel (the "brain" — logic & data)
//    SECTION 4 → MainActivity (the "body" — connects Android to UI)
//    SECTION 5 → PomodoroApp (the root UI composable)
//    SECTION 6 → ModeSelector (tab buttons at the top)
//    SECTION 7 → TimerCircle (the big animated countdown ring)
//    SECTION 8 → ControlButtons (Start / Pause / Reset)
//    SECTION 9 → SessionCounter (shows completed sessions)
//    SECTION 10→ Helper: Color extension
//
//  🔑 KEY CONCEPT — Jetpack Compose:
//  In Compose, the UI is written as FUNCTIONS (called Composables).
//  Any function marked with @Composable draws something on screen.
//  When the data they read CHANGES, they automatically REDRAW.
//  You never manually update the UI — Compose does it for you.
//
// ═══════════════════════════════════════════════════════════════════

// ───────────────────────────────────────────────────────────────────
// SECTION 1: PACKAGE & IMPORTS
// ───────────────────────────────────────────────────────────────────
// "package" is like the address of this file.
// Every file in a project has a unique package address.
// It must match the folder structure:
//   com/pomodoro/app/MainActivity.kt → package com.pomodoro.app
// ───────────────────────────────────────────────────────────────────

package com.pomodoro.app

// "import" statements bring in code from external libraries.
// Without imports, you'd have to write everything from scratch.
// Think of them as "include this tool in my toolbox".

import android.os.Bundle                            // Android lifecycle data
import androidx.activity.ComponentActivity          // Base class for our screen
import androidx.activity.compose.setContent         // Connects Compose to Activity
import androidx.activity.enableEdgeToEdge           // Makes UI go edge-to-edge
import androidx.compose.animation.animateColorAsState // Smoothly animates color changes
import androidx.compose.animation.core.animateFloatAsState  // Smoothly animates number changes
import androidx.compose.animation.core.tween        // Defines animation duration/style
import androidx.compose.foundation.background       // Adds a background color/gradient
import androidx.compose.foundation.layout.*         // Layout tools: Column, Row, Box, etc.
import androidx.compose.foundation.shape.CircleShape        // A circle shape
import androidx.compose.foundation.shape.RoundedCornerShape // Rounded rectangles
import androidx.compose.material3.*                 // Material 3 components (Button, Text, etc.)
import androidx.compose.runtime.*                   // Compose state management (remember, etc.)
import androidx.compose.ui.Alignment                // Alignment options (center, top-start, etc.)
import androidx.compose.ui.Modifier                 // Modifier system (size, padding, click, etc.)
import androidx.compose.ui.draw.clip               // Clips a composable to a shape
import androidx.compose.ui.draw.shadow             // Adds a drop shadow
import androidx.compose.ui.graphics.Brush          // For gradients
import androidx.compose.ui.graphics.Color          // Color class
import androidx.compose.ui.graphics.StrokeCap      // The end style of drawn lines
import androidx.compose.ui.graphics.drawscope.Stroke // Drawing style for outlines
import androidx.compose.ui.graphics.toArgb         // Converts Color to int for Android APIs
import androidx.compose.ui.text.font.FontWeight    // Bold, SemiBold, etc.
import androidx.compose.ui.text.style.TextAlign    // Center, Left, Right text alignment
import androidx.compose.ui.unit.Dp                 // Density-independent pixels (dp)
import androidx.compose.ui.unit.dp                 // Extension to write "16.dp" instead of Dp(16)
import androidx.compose.ui.unit.sp                 // Scale-independent pixels for text size
import androidx.compose.ui.canvas                  // Low-level canvas drawing (for the circle arc)
import androidx.compose.foundation.Canvas          // A Composable you can draw on freely
import androidx.compose.ui.geometry.Offset         // An x,y position
import androidx.compose.ui.geometry.Size           // A width,height pair
import androidx.lifecycle.ViewModel                // Base class for ViewModel
import androidx.lifecycle.viewModelScope           // Coroutine scope tied to ViewModel's lifetime
import androidx.lifecycle.viewmodel.compose.viewModel // Gets the ViewModel inside a Composable
import kotlinx.coroutines.Job                      // Represents a running background task
import kotlinx.coroutines.delay                    // Suspends (waits) for a given time
import kotlinx.coroutines.launch                   // Starts a new coroutine (background task)
import kotlin.math.cos                             // Math: cosine (used for dot position on circle)
import kotlin.math.sin                             // Math: sine (used for dot position on circle)
import kotlin.math.PI                             // Math: π = 3.14159...


// ───────────────────────────────────────────────────────────────────
// SECTION 2: TIMER MODES
// ───────────────────────────────────────────────────────────────────
// An "enum class" (enumeration class) is a special type that
// represents a fixed set of options — like multiple-choice answers.
//
// Each option (WORK, SHORT_BREAK, LONG_BREAK) is a MODE.
// Each mode carries its own data: a label, duration, and color.
//
// WHY THIS IS USEFUL:
// Instead of having separate variables for "workDuration",
// "breakDuration", etc., we bundle everything into one enum.
// When we change mode, all the related values change together.
// ───────────────────────────────────────────────────────────────────

enum class TimerMode(
    val label: String,      // The name shown on the mode tab button
    val totalSeconds: Int,  // Total countdown duration in seconds
    val color: Color        // The accent color used for this mode
) {

    // ── Work Mode ────────────────────────────────────────────────
    // The classic Pomodoro session: 25 minutes of focused work.
    // Color: coral red — signals "focus time, no distractions".
    WORK(
        label        = "Focus",
        totalSeconds = 25 * 60,         // 25 minutes × 60 seconds = 1500 seconds
        color        = Color(0xFFE57373) // Hex color: a warm coral red
        //              ↑ 0xFF = fully opaque, E5 7373 = the red shade
    ),

    // ── Short Break Mode ─────────────────────────────────────────
    // 5 minutes to rest your eyes, stretch, grab water.
    // Color: soft green — signals "relax, you've earned it".
    SHORT_BREAK(
        label        = "Short Break",
        totalSeconds = 5 * 60,          // 5 minutes = 300 seconds
        color        = Color(0xFF66BB6A) // A calm, friendly green
    ),

    // ── Long Break Mode ──────────────────────────────────────────
    // After 4 work sessions, take a longer 15-minute break.
    // Color: sky blue — signals "breathe, take it easy".
    LONG_BREAK(
        label        = "Long Break",
        totalSeconds = 15 * 60,         // 15 minutes = 900 seconds
        color        = Color(0xFF42A5F5) // A relaxing blue
    )
}


// ───────────────────────────────────────────────────────────────────
// SECTION 3: THE VIEWMODEL — THE BRAIN OF THE APP
// ───────────────────────────────────────────────────────────────────
// A ViewModel is a special class that:
//   ✓ Holds all the data and logic for a screen
//   ✓ Survives screen rotations (unlike regular variables)
//   ✓ Keeps the UI layer "dumb" — the UI just displays what the
//     ViewModel tells it, and sends user actions back to it
//
// ANALOGY:
//   ViewModel = the kitchen (prepares the food / data)
//   UI = the dining room (just displays what comes out)
//
// If you put all logic in the UI, it becomes messy and hard to test.
// The ViewModel keeps things clean and organized.
// ───────────────────────────────────────────────────────────────────

class PomodoroViewModel : ViewModel() {

    // ── STATE VARIABLES ───────────────────────────────────────────
    // "var ... by mutableStateOf(...)" creates a REACTIVE variable.
    //
    // REACTIVE means: when the value changes, any @Composable that
    // reads this variable will AUTOMATICALLY redraw itself.
    //
    // This is the core magic of Jetpack Compose — you update data,
    // the UI updates itself. No manual "refresh" calls needed.
    //
    // "private set" means: only THIS ViewModel class can change the
    // value. The UI can READ it, but not write to it directly.
    // The UI must call a function (like toggleTimer()) to trigger changes.
    // This is called "encapsulation" — protecting your data.

    // Which timer mode is currently active?
    var currentMode: TimerMode by mutableStateOf(TimerMode.WORK)
        private set

    // How many seconds remain on the current timer?
    // Starts at 25 * 60 = 1500 (the WORK mode duration)
    var secondsLeft: Int by mutableStateOf(TimerMode.WORK.totalSeconds)
        private set

    // Is the timer actively counting down right now?
    // true = counting, false = paused or not started
    var isRunning: Boolean by mutableStateOf(false)
        private set

    // How many complete WORK sessions has the user finished?
    // Used to show the tomato icons at the bottom.
    var completedSessions: Int by mutableStateOf(0)
        private set

    // ── INTERNAL: The timer coroutine job ─────────────────────────
    // A "Job" is a handle to a running background task (coroutine).
    // We store it here so we can CANCEL it (= pause the timer).
    //
    // The "?" means it can be null (no job running = null).
    // We initialize it as null since the timer isn't running yet.
    private var timerJob: Job? = null

    // ── COMPUTED PROPERTIES ───────────────────────────────────────
    // These are values CALCULATED from existing state.
    // They're not stored — they're recalculated every time they're read.
    // In Kotlin, you define them with "val name: Type get() = ..."

    // Progress from 1.0 (full / just started) down to 0.0 (empty / done).
    // Used to animate the circular progress ring.
    //
    // Example: 750 seconds left out of 1500 total → 750/1500 = 0.5 (50% full)
    val progress: Float
        get() = secondsLeft.toFloat() / currentMode.totalSeconds.toFloat()
        //       ↑ .toFloat() converts Int to Float for decimal division
        //       Without it, 750 / 1500 = 0 (integer division truncates!)

    // Format the remaining time as "MM:SS" for display.
    // Example: 93 seconds → "01:33"
    val timeDisplay: String
        get() {
            val minutes = secondsLeft / 60   // Integer division: 93 / 60 = 1
            val seconds = secondsLeft % 60   // Remainder: 93 % 60 = 33
            // "%02d" = format as integer, minimum 2 digits, pad with zero
            // "02" → "02", "9" → "09", "0" → "00"
            return "%02d:%02d".format(minutes, seconds)
        }

    // ── PUBLIC FUNCTIONS (actions the UI can trigger) ─────────────

    // Called when user taps the big Start/Pause button.
    // If running → pause. If paused → start.
    fun toggleTimer() {
        if (isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    // Called when user taps the Reset button.
    // Stops the timer and restores the full duration.
    fun resetTimer() {
        pauseTimer()                              // Stop the background task
        secondsLeft = currentMode.totalSeconds    // Restore to full time
    }

    // Called when user taps one of the mode buttons (Focus/Break).
    // Stops the timer, switches mode, resets to new mode's duration.
    fun switchMode(mode: TimerMode) {
        pauseTimer()                   // Stop current timer
        currentMode = mode             // Switch the mode
        secondsLeft = mode.totalSeconds // Reset to new mode's duration
    }

    // ── PRIVATE FUNCTIONS (internal logic) ────────────────────────

    // Starts the countdown. Launches a coroutine (background task).
    private fun startTimer() {
        isRunning = true

        // "viewModelScope.launch { ... }" starts a COROUTINE.
        //
        // A coroutine is a lightweight background task that can
        // pause (suspend) without blocking the main thread.
        //
        // "viewModelScope" means: this coroutine is tied to the
        // ViewModel's lifetime. If the ViewModel is destroyed
        // (e.g., user closes the app), the coroutine is automatically
        // cancelled. No memory leaks!
        timerJob = viewModelScope.launch {

            // "while (condition)" = keep doing this until condition is false
            while (secondsLeft > 0) {

                // "delay(1000)" = WAIT for 1000 milliseconds (1 second).
                // This is a SUSPEND point — the coroutine pauses here
                // without blocking the UI thread. The app stays responsive.
                delay(1000L) // L = Long type (delay takes a Long, not Int)

                secondsLeft-- // Subtract 1 second. Same as: secondsLeft = secondsLeft - 1
                //               Because secondsLeft is mutableStateOf, the UI redraws!
            }

            // The while loop ended: secondsLeft reached 0. Timer done!
            onTimerComplete()
        }
    }

    // Pauses the timer by cancelling the coroutine.
    private fun pauseTimer() {
        isRunning = false

        // "timerJob?.cancel()" — the "?" means "only call cancel() IF
        // timerJob is not null". If there's no job, do nothing.
        timerJob?.cancel()
        timerJob = null // Clear the reference (good housekeeping)
    }

    // Called automatically when the countdown reaches zero.
    private fun onTimerComplete() {
        isRunning = false

        // If a WORK session just finished, increment the session counter
        if (currentMode == TimerMode.WORK) {
            completedSessions++ // Show one more tomato! 🍅
        }
        // Note: we don't auto-switch modes. The user taps manually.
        // This is intentional — Pomodoro works best when YOU decide to continue.
    }

    // Cleanup: called when the ViewModel is permanently destroyed.
    // We cancel any running timer to prevent memory leaks.
    // "override" means we're replacing the parent class's version of this function.
    override fun onCleared() {
        super.onCleared() // Always call super first
        timerJob?.cancel() // Cancel timer if running
    }
}


// ───────────────────────────────────────────────────────────────────
// SECTION 4: MAIN ACTIVITY
// ───────────────────────────────────────────────────────────────────
// ComponentActivity is the modern base class for Android screens.
// Every Android app must have at least one Activity.
//
// onCreate() is called by Android when the app first launches —
// it's where you set up the screen.
// ───────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    // "override" = we're providing our own version of this function.
    // Android calls onCreate() automatically when the screen is created.
    // "savedInstanceState" contains any saved data from a previous session
    // (e.g., if the app was in the background and came back).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Always call the parent's version first!

        // Makes the UI draw behind the system bars (status bar, nav bar)
        // so the background color fills the entire screen edge-to-edge.
        enableEdgeToEdge()

        // "setContent" is where you hand control over to Jetpack Compose.
        // Everything inside the curly braces {} is drawn using Compose.
        setContent {
            // PomodoroApp() is our root composable — it draws everything.
            PomodoroApp()
        }
    }
}


// ───────────────────────────────────────────────────────────────────
// SECTION 5: POMODORO APP — ROOT COMPOSABLE
// ───────────────────────────────────────────────────────────────────
// The "root" composable is the outermost UI component.
// It owns the ViewModel and passes data/actions down to children.
//
// COMPOSABLE FUNCTION RULES:
//   • Must be marked with @Composable
//   • Can only be called from other @Composable functions
//   • Name MUST start with an uppercase letter (by convention)
// ───────────────────────────────────────────────────────────────────

@Composable
fun PomodoroApp() {

    // "viewModel()" retrieves the PomodoroViewModel.
    // If one doesn't exist yet, it creates one.
    // If the screen recomposes (redraws), it returns the SAME instance.
    // This is how state survives redraws without resetting.
    val vm: PomodoroViewModel = viewModel()

    // ── Animated background color ─────────────────────────────────
    // Instead of instantly switching the background color when the
    // mode changes, we ANIMATE it over 600ms for a smooth transition.
    //
    // "animateColorAsState" watches a value; when it changes, it
    // smoothly interpolates (tweens) to the new value over time.
    //
    // "label" is just for debugging — it names this animation.
    val animatedBgColor by animateColorAsState(
        targetValue   = vm.currentMode.color.copy(alpha = 0.10f), // Very faded version of mode color
        animationSpec = tween(durationMillis = 600),               // 600ms transition
        label         = "background_color"
    )

    // ── Root layout ───────────────────────────────────────────────
    // "Box" is a layout that stacks children on top of each other.
    // We use it here just for the background + content structure.
    Box(
        modifier = Modifier
            .fillMaxSize()             // Fill the entire screen
            .background(Color(0xFFFAF8FF)) // Off-white base background
    ) {
        // Animated color overlay on top of the white base
        // This creates a subtle tinted background that changes per mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgColor) // The animated color
        )

        // ── Main content layout ───────────────────────────────────
        // "Column" stacks its children vertically (top to bottom).
        // "verticalArrangement = SpaceEvenly" distributes children
        // evenly, with equal space between and around them.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()    // Adds top padding to avoid the status bar
                .navigationBarsPadding() // Adds bottom padding to avoid nav bar
                .padding(horizontal = 24.dp), // Left+right padding of 24dp
            horizontalAlignment = Alignment.CenterHorizontally, // Center all children horizontally
            verticalArrangement = Arrangement.SpaceEvenly       // Space children evenly vertically
        ) {

            // App title
            Text(
                text       = "🍅 Pomodoro",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF3D2E52) // Dark purple-ish text
            )

            // ── MODE SELECTOR TABS ───────────────────────────────
            // Lets the user switch between Focus / Short Break / Long Break.
            // We pass the current mode and a lambda (function) to call when
            // the user picks a different mode.
            //
            // "it" inside the lambda refers to the chosen TimerMode.
            ModeSelector(
                currentMode    = vm.currentMode,
                onModeSelected = { vm.switchMode(it) } // When a tab is tapped
            )

            // ── TIMER CIRCLE ─────────────────────────────────────
            // The big circular countdown display.
            TimerCircle(
                progress    = vm.progress,     // 0.0 to 1.0 (how full the ring is)
                timeDisplay = vm.timeDisplay,  // "MM:SS" string
                color       = vm.currentMode.color,
                isRunning   = vm.isRunning
            )

            // ── CONTROL BUTTONS ───────────────────────────────────
            // Start/Pause and Reset buttons.
            ControlButtons(
                isRunning = vm.isRunning,
                color     = vm.currentMode.color,
                onToggle  = { vm.toggleTimer() }, // Start or Pause
                onReset   = { vm.resetTimer() }   // Reset to full time
            )

            // ── SESSION COUNTER ───────────────────────────────────
            // Shows 🍅 icons for each completed work session.
            SessionCounter(completedSessions = vm.completedSessions)
        }
    }
}


// ───────────────────────────────────────────────────────────────────
// SECTION 6: MODE SELECTOR — The tab buttons at the top
// ───────────────────────────────────────────────────────────────────
// This draws three buttons side-by-side.
// The active one is filled/highlighted; the others are plain.
//
// PARAMETERS:
//   currentMode    → which mode is currently active (to highlight it)
//   onModeSelected → a function to call when user taps a button.
//                    The () -> Unit syntax means: "a function that
//                    takes a TimerMode and returns nothing (Unit)".
// ───────────────────────────────────────────────────────────────────

@Composable
fun ModeSelector(
    currentMode: TimerMode,
    onModeSelected: (TimerMode) -> Unit // Callback: called with the chosen mode
) {
    // A Card is a surface with rounded corners and a shadow.
    // It groups the three buttons visually.
    Card(
        shape  = RoundedCornerShape(20.dp), // Heavily rounded corners
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f) // Semi-transparent white
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Drop shadow
    ) {
        // "Row" arranges children horizontally (side by side).
        Row(modifier = Modifier.padding(6.dp)) {

            // "TimerMode.entries" = a list of all enum values: [WORK, SHORT_BREAK, LONG_BREAK]
            // "forEach" loops through each one and runs the block for it.
            TimerMode.entries.forEach { mode ->

                // Is this the mode that's currently selected?
                val isSelected = (mode == currentMode)

                // ── Animated tab background color ─────────────────
                // When selected: the mode's color (e.g., red for WORK)
                // When not selected: transparent (so just the Card shows through)
                val tabBgColor by animateColorAsState(
                    targetValue   = if (isSelected) mode.color else Color.Transparent,
                    animationSpec = tween(300), // 300ms animation
                    label         = "tab_bg_${mode.name}"
                )

                // ── Animated tab text color ───────────────────────
                // Selected text = white (to contrast the colored background)
                // Unselected text = a muted gray
                val tabTextColor by animateColorAsState(
                    targetValue   = if (isSelected) Color.White else Color(0xFF9E9E9E),
                    animationSpec = tween(300),
                    label         = "tab_text_${mode.name}"
                )

                // "Box" is used here as the individual tab "pill".
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp)) // Rounded shape
                        .background(tabBgColor)          // Animated background
                        .clickable { onModeSelected(mode) } // Tap action
                        .padding(horizontal = 16.dp, vertical = 10.dp), // Inner padding
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = mode.label,         // "Focus", "Short Break", etc.
                        color      = tabTextColor,        // Animated text color
                        fontSize   = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// Note: we used .clickable above without importing it explicitly.
// In many projects you'd see: import androidx.compose.foundation.clickable
// Android Studio would add this import automatically for you.
// We're adding it here manually since this is a single-file explanation.
// (The actual import is at the top of the file if needed.)


// ───────────────────────────────────────────────────────────────────
// SECTION 7: TIMER CIRCLE — The big animated countdown ring
// ───────────────────────────────────────────────────────────────────
// This is the most visually complex part of the app.
// It draws:
//   1. A gray background circle (the "track")
//   2. A colored arc on top that shrinks as time runs out
//   3. A glowing dot at the tip of the arc
//   4. The time text in the center
//   5. A pulsing outer glow when the timer is running
//
// HOW THE ARC WORKS:
//   - A full circle = 360 degrees = "progress 1.0"
//   - As time runs out, progress drops from 1.0 → 0.0
//   - We multiply progress × 360 to get the arc's sweep angle
//   - The arc starts at the top (-90°) and sweeps clockwise
// ───────────────────────────────────────────────────────────────────

@Composable
fun TimerCircle(
    progress: Float,    // 0.0 (empty) to 1.0 (full). How much of the arc to draw.
    timeDisplay: String, // The "MM:SS" text shown in the center
    color: Color,        // The mode's accent color (red for work, green for break, etc.)
    isRunning: Boolean   // Is the timer ticking right now?
) {
    // ── Animate the progress ─────────────────────────────────────
    // Instead of the arc jumping instantly when secondsLeft changes,
    // we animate it smoothly over 800ms for a fluid look.
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 800), // 800ms smooth transition
        label         = "timer_progress"
    )

    // ── Animate the arc color ─────────────────────────────────────
    // When mode changes, the arc color transitions smoothly.
    val animatedColor by animateColorAsState(
        targetValue   = color,
        animationSpec = tween(600),
        label         = "timer_color"
    )

    // ── Pulsing glow animation ────────────────────────────────────
    // When the timer is RUNNING, we show a pulsing outer ring.
    // "rememberInfiniteTransition" creates a transition that repeats forever.
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Animate the glow alpha (opacity) from 0.1 to 0.4 and back, endlessly.
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.1f,  // Start at 10% opacity
        targetValue   = 0.4f,  // Animate to 40% opacity
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation  = tween(1000), // 1 second per pulse
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse // Then go back
        ),
        label = "pulse_alpha"
    )

    // ── Layout ────────────────────────────────────────────────────
    // "Box" lets us stack the Canvas (circle drawing) and Text on top of each other.
    // "contentAlignment = Center" puts children in the middle.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp) // The overall size of the timer widget
    ) {

        // ── CANVAS: Draw the circle and arc ───────────────────────
        // Canvas is a blank drawing surface. You use it when the built-in
        // Compose components aren't enough — here we need custom arc drawing.
        //
        // The "drawScope" inside gives you commands like:
        //   drawCircle(...)  → draw a circle
        //   drawArc(...)     → draw an arc (partial circle)
        Canvas(modifier = Modifier.fillMaxSize()) {

            val canvasSize = size.minDimension // The smaller of width/height (they're equal)
            val radius     = canvasSize / 2f   // The radius of our circle
            val strokeWidth = 18.dp.toPx()     // Arc thickness in pixels (dp converted to px)

            // ── Pulsing outer glow (only when running) ────────────
            // This is the subtle breathing ring around the outside.
            if (isRunning) {
                drawCircle(
                    color  = animatedColor.copy(alpha = pulseAlpha), // Fading in/out
                    radius = radius + 20.dp.toPx(), // Slightly larger than the main circle
                    style  = Stroke(width = 8.dp.toPx()) // Outline (stroke), not filled
                )
            }

            // ── Gray background track circle ───────────────────────
            // This is the full circle in the background that shows the
            // "empty" portion of the timer ring.
            drawCircle(
                color  = Color(0xFFEEEEEE), // Light gray
                radius = radius - strokeWidth / 2f, // Slightly inset to align with arc
                style  = Stroke(width = strokeWidth)
            )

            // ── Colored progress arc ───────────────────────────────
            // "drawArc" draws a portion of a circle outline.
            // Parameters:
            //   color      → the arc's color
            //   startAngle → where the arc starts (in degrees, 0 = 3 o'clock)
            //                We use -90 so it starts at the TOP (12 o'clock)
            //   sweepAngle → how many degrees the arc covers
            //                360° = full circle, 0° = nothing
            //   useCenter  → false = draw arc outline only (not a pie slice)
            //   topLeft    → offset so the arc is centered
            //   size       → the bounding box size for the arc
            //   style      → Stroke = outline, Fill = filled
            drawArc(
                color      = animatedColor,
                startAngle = -90f,                          // Start at 12 o'clock
                sweepAngle = 360f * animatedProgress,       // Cover progress% of circle
                useCenter  = false,                         // Don't connect to center
                topLeft    = Offset(                        // Top-left corner of bounding box
                    x = strokeWidth / 2f,
                    y = strokeWidth / 2f
                ),
                size  = Size(                               // Width and height of bounding box
                    width  = canvasSize - strokeWidth,
                    height = canvasSize - strokeWidth
                ),
                style = Stroke(                             // Draw as outline
                    width   = strokeWidth,
                    cap     = StrokeCap.Round               // Rounded ends on the arc
                )
            )

            // ── Glowing dot at the arc tip ────────────────────────
            // This dot sits exactly at the leading edge of the arc.
            // We use trigonometry to calculate its position:
            //   - The arc goes from -90° (top) to (360 * progress - 90)°
            //   - cos() and sin() convert an angle to x,y coordinates
            //
            // DON'T PANIC about the math — you don't need to understand it
            // to use it. It's the standard formula for "a point on a circle".
            if (animatedProgress > 0.01f) { // Only show dot if there's any arc visible
                val angleRad = ((360f * animatedProgress - 90f) * PI / 180f).toFloat()
                //              ↑ Convert degrees to radians (trig functions need radians)
                val dotRadius   = canvasSize / 2f - strokeWidth / 2f // Same radius as arc
                val dotCenter   = Offset(
                    x = center.x + dotRadius * cos(angleRad), // x = cx + r * cos(angle)
                    y = center.y + dotRadius * sin(angleRad)  // y = cy + r * sin(angle)
                )

                // Outer glow of the dot
                drawCircle(
                    color  = animatedColor.copy(alpha = 0.4f), // Semi-transparent
                    radius = strokeWidth / 2f + 6.dp.toPx(),  // Slightly bigger
                    center = dotCenter
                )

                // Inner solid dot
                drawCircle(
                    color  = Color.White, // White dot in the center
                    radius = strokeWidth / 2f,
                    center = dotCenter
                )
            }
        }

        // ── Time display text (centered on top of Canvas) ─────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // The big MM:SS countdown number
            Text(
                text       = timeDisplay,
                fontSize   = 56.sp,           // Large font for readability
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF2D2D2D) // Dark text (almost black)
            )

            // Smaller label showing the current mode name
            Text(
                text      = if (isRunning) "⏱ running" else "⏸ paused",
                fontSize  = 14.sp,
                color     = Color(0xFF9E9E9E), // Muted gray
                textAlign = TextAlign.Center
            )
        }
    }
}


// ───────────────────────────────────────────────────────────────────
// SECTION 8: CONTROL BUTTONS — Start/Pause and Reset
// ───────────────────────────────────────────────────────────────────
// Two buttons:
//   1. Big START/PAUSE button — the main action
//   2. Smaller RESET button — secondary action
//
// The button COLOR and LABEL change based on the timer mode and state.
// ───────────────────────────────────────────────────────────────────

@Composable
fun ControlButtons(
    isRunning: Boolean,    // Is timer running? Determines button label
    color: Color,          // Current mode's color (for the button background)
    onToggle: () -> Unit,  // () -> Unit = "a function that takes nothing, returns nothing"
    onReset: () -> Unit    // Called when Reset is tapped
) {
    // Animate button background color when mode changes
    val animatedColor by animateColorAsState(
        targetValue   = color,
        animationSpec = tween(600),
        label         = "button_color"
    )

    // ── Vertical stack of two buttons ────────────────────────────
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // 12dp gap between buttons
    ) {

        // ── START / PAUSE Button ─────────────────────────────────
        // This is the primary button — large and prominent.
        // "Button" is a Material 3 component that handles:
        //   - The ripple effect on tap
        //   - Accessibility (semantic meaning)
        //   - Proper sizing and padding
        Button(
            onClick = onToggle, // Call the function passed from the parent
            modifier = Modifier
                .fillMaxWidth()  // Take up the full available width
                .height(60.dp),  // Fixed height for a comfortable tap target
            shape  = RoundedCornerShape(18.dp), // Rounded corners
            colors = ButtonDefaults.buttonColors(
                containerColor = animatedColor // Use the animated mode color
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp // Drop shadow for depth
            )
        ) {
            // The button label changes based on whether the timer is running
            Text(
                // If running → show "⏸ Pause", otherwise → show "▶ Start"
                text       = if (isRunning) "⏸  Pause" else "▶  Start",
                fontSize   = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
        }

        // ── RESET Button ─────────────────────────────────────────
        // A secondary outlined button (less visually dominant).
        // "OutlinedButton" shows just a border instead of a filled background.
        OutlinedButton(
            onClick   = onReset,
            modifier  = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = animatedColor // The border and text use the mode color
            )
        ) {
            Text(
                text     = "↺  Reset",
                fontSize = 16.sp
            )
        }
    }
}


// ───────────────────────────────────────────────────────────────────
// SECTION 9: SESSION COUNTER — Shows completed 🍅 sessions
// ───────────────────────────────────────────────────────────────────
// Shows up to 8 tomato icons in a row.
// Completed sessions = filled 🍅
// Future sessions   = empty ○ dots
// After every 4 sessions, you've done a full Pomodoro cycle!
// ───────────────────────────────────────────────────────────────────

@Composable
fun SessionCounter(completedSessions: Int) {

    // ── Wrap the counter in a card ────────────────────────────────
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Label
            Text(
                text     = "Sessions completed",
                fontSize = 13.sp,
                color    = Color(0xFF9E9E9E) // Muted gray
            )

            // ── Row of session icons ──────────────────────────────
            // We show 8 slots (standard Pomodoro day is 8 sessions).
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // "repeat(8)" runs the block 8 times.
                // "index" is the current iteration: 0, 1, 2, ... 7
                repeat(8) { index ->

                    // Is this slot filled (session completed)?
                    // If completedSessions = 3, then indices 0, 1, 2 are filled.
                    val isFilled = index < completedSessions

                    // Draw a circle dot — filled (tomato) or outline (empty slot)
                    Box(
                        modifier = Modifier
                            .size(if (isFilled) 24.dp else 16.dp) // Filled dots are bigger
                            .clip(CircleShape)
                            .background(
                                if (isFilled)
                                    Color(0xFFE57373) // Tomato red for completed
                                else
                                    Color(0xFFEEEEEE)  // Gray for empty
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show 🍅 emoji on completed dots
                        if (isFilled) {
                            Text(
                                text     = "🍅",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // After every 4 slots, show a divider to mark one full cycle
                    if (index == 3) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(Color(0xFFCCCCCC)) // Light gray divider
                        )
                    }
                }
            }

            // Show the numeric count and a motivational suffix
            val cyclesComplete = completedSessions / 4   // Full 4-session cycles
            val sessionText = when {
                completedSessions == 0 -> "Let's get started! 💪"
                completedSessions % 4 == 0 && completedSessions > 0 ->
                    "$completedSessions sessions — Take a long break! 🎉"
                else -> "$completedSessions session${if (completedSessions > 1) "s" else ""} done"
            }
            Text(
                text     = sessionText,
                fontSize = 12.sp,
                color    = Color(0xFF757575) // Medium gray
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// END OF FILE
// ─────────────────────────────────────────────────────────────────
//
// 🎉 That's the entire app! Let's recap what we learned:
//
//   • enum class  → A fixed list of named options (WORK, SHORT_BREAK, etc.)
//   • ViewModel   → Holds state + logic; survives screen rotation
//   • mutableStateOf → A variable that triggers UI redraws when changed
//   • @Composable → A function that draws UI
//   • Modifier    → Chains of settings applied to UI elements (size, padding, etc.)
//   • Column/Row/Box → The three layout containers
//   • Canvas      → Free-form drawing surface for custom visuals
//   • coroutine   → A lightweight background task (our timer)
//   • delay()     → Waits N milliseconds without blocking the screen
//   • animate*AsState → Smoothly animates a value from old to new
//
// NEXT STEPS IF YOU WANT TO EXPAND THIS APP:
//   1. Add sound effects (MediaPlayer) when the timer completes
//   2. Add a notification (NotificationManager) when time is up
//   3. Save sessions to a database (Room) to track history
//   4. Add a Settings screen to customize timer durations
//   5. Add a widget (AppWidget) for the home screen
//
// Happy building! 🚀
// ─────────────────────────────────────────────────────────────────

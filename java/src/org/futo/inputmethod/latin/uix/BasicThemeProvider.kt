package org.futo.inputmethod.latin.uix

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import org.futo.inputmethod.keyboard.internal.KeyboardIconsSet
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.actions.AllActions
import org.futo.inputmethod.latin.uix.actions.AllActionsMap
import org.futo.inputmethod.v2keyboard.KeyVisualStyle

val KeyBordersSetting = SettingsKey(booleanPreferencesKey("keyBorders"), true)
val HiddenKeysSetting = SettingsKey(booleanPreferencesKey("hiddenKeys1"), false)
val KeyHintsSetting   = SettingsKey(booleanPreferencesKey("keyHints"), false)

fun<T> Preferences.get(key: SettingsKey<T>): T {
    return this[key.key] ?: key.default
}

class BasicThemeProvider(val context: Context, val colorScheme: KeyboardColorScheme) :
    DynamicThemeProvider {
    override val keyBorders: Boolean

    override val keyboardColor: Int
    override val actionBarColor: Color
    override val keyColor: Int

    override val keyboardBackground: Drawable
    override val keyBackground: Drawable

    override val keyFeedback: Drawable

    override val moreKeysTextColor: Int
    override val moreKeysKeyboardBackground: Drawable

    override val displayDpi: Int

    override val hintColor: Int?
    override val hintHiVis: Boolean

    override var typefaceOverride: Typeface? = null
    override val themeTypeface: Typeface? = null

    private val colors: HashMap<Int, Int> = HashMap()
    override fun getColor(i: Int): Int? {
        return colors[i]
    }


    private val drawables: HashMap<Int, Drawable> = HashMap()
    override fun getDrawable(i: Int): Drawable? {
        return drawables[i]
    }

    val icons: HashMap<String, Drawable?> = hashMapOf()
    override fun getIcon(iconName: String): Drawable? {
        if(iconName == KeyboardIconsSet.ICON_UNDEFINED) return null

        if(!icons.containsKey(iconName)) {
            Log.e("BasicThemeProvider", "Unknown icon $iconName")
        }

        return icons[iconName]
    }

    val keyStyles: Map<KeyVisualStyle, VisualStyleDescriptor>
    override fun getKeyStyleDescriptor(visualStyle: KeyVisualStyle): VisualStyleDescriptor {
        return keyStyles[visualStyle]!!
    }

    private fun dp(dp: Dp): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.value,
            context.resources.displayMetrics
        );
    }

    private fun coloredRectangle(@ColorInt color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
        }
    }

    private fun coloredRoundedRectangle(@ColorInt color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }

    private fun coloredOval(@ColorInt color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = Float.MAX_VALUE
            setColor(color)
        }
    }

    private fun StateListDrawable.addStateWithHighlightLayerOnPressed(@ColorInt highlight: Int, stateSet: IntArray, drawable: Drawable, cornerRadius: Dp = 8.dp) {
        addState(intArrayOf(android.R.attr.state_pressed) + stateSet, LayerDrawable(
            arrayOf(
                drawable,
                coloredRoundedRectangle(highlight, dp(cornerRadius))
            )
        )
        )
        addState(stateSet, drawable)
    }

    private fun makeVisualStyle(background: Int, foreground: Int, highlight: Int, foregroundPressed: Int, roundedness: Dp): VisualStyleDescriptor {
        val bg = coloredRoundedRectangle(background, dp(roundedness))
        val bgPressed = coloredRoundedRectangle(Color(highlight).compositeOver(Color(background)).toArgb(), dp(roundedness))
        val fgPressed = Color(foregroundPressed).compositeOver(Color(foreground)).toArgb()
        return VisualStyleDescriptor(
            backgroundDrawable = bg,
            foregroundColor    = foreground,

            backgroundDrawablePressed = bgPressed,
            foregroundColorPressed = fgPressed
        )
    }

    val expertMode: Boolean
    val showKeyHints: Boolean

    fun hasUpdated(np: Preferences): Boolean {
        return np.get(HiddenKeysSetting) != expertMode
                || np.get(KeyBordersSetting) != keyBorders
                || np.get(KeyHintsSetting) != showKeyHints
    }


    private fun addIcon(iconName: String, drawableIntResId: Int, tint: Int) {
        addIcon(iconName, AppCompatResources.getDrawable(
            context,
            drawableIntResId
        ), tint)
    }

    private fun addIcon(iconName: String, drawable: Drawable?, tint: Int) {
        icons[iconName] = drawable?.apply {
            setTint(tint)
        }
    }

    init {
        displayDpi = context.resources.displayMetrics.densityDpi

        expertMode = context.getSettingBlocking(HiddenKeysSetting)
        keyBorders = context.getSettingBlocking(KeyBordersSetting)
        showKeyHints = context.getSettingBlocking(KeyHintsSetting)

        hintColor = colorScheme.hintColor?.toArgb()
        hintHiVis = colorScheme.hintHiVis

        val primary = colorScheme.primary.toArgb()
        val secondary = colorScheme.secondary.toArgb()
        val highlight = colorScheme.keyboardContainerPressed.toArgb()
        val highlightForeground = colorScheme.onKeyboardContainerPressed.toArgb()

        val background = colorScheme.surface.toArgb()
        val surface = colorScheme.background.toArgb()
        val outline = colorScheme.outline.toArgb()

        val primaryContainer = colorScheme.primaryContainer.toArgb()
        val onPrimaryContainer = colorScheme.onPrimaryContainer.toArgb()

        val onPrimary = colorScheme.onPrimary.toArgb()
        val onPrimaryThird = colorScheme.onPrimary.copy(alpha = 0.33f).toArgb()
        val onSecondary = colorScheme.onSecondary.toArgb()
        val onBackground = colorScheme.onBackground.toArgb()
        val onBackgroundHalf = colorScheme.onBackground.copy(alpha = 0.5f).toArgb()
        val onBackgroundThird = colorScheme.onBackground.copy(alpha = 0.33f).toArgb()

        val transparent = Color.Transparent.toArgb()
        keyboardColor = colorScheme.keyboardSurface.toArgb()
        actionBarColor = if(keyBorders) {
            colorScheme.keyboardSurface
        } else {
            colorScheme.keyboardSurfaceDim
        }

        val keyColor = if(keyBorders) {
            colorScheme.keyboardContainer.toArgb()
        } else {
            transparent
        }

        this.keyColor = keyColor

        val functionalKeyColor = if(keyBorders) {
            colorScheme.keyboardContainerVariant.toArgb()
        } else {
            transparent
        }

        val onKeyColor = if(keyBorders) {
            colorScheme.onKeyboardContainer.toArgb()
        } else {
            onBackground
        }

        val onKeyColorHalf = Color(onKeyColor).copy(alpha = 0.5f).toArgb()
        val onKeyColorThird = Color(onKeyColor).copy(alpha = 0.33f).toArgb()

        val enterKeyBackground = if(expertMode) { functionalKeyColor } else { primary }
        val enterKeyForeground = if(expertMode) { onBackgroundThird } else { onPrimary }

        colors[R.styleable.Keyboard_Key_keyTextColor] = onKeyColor
        colors[R.styleable.Keyboard_Key_keyTextInactivatedColor] = onKeyColorHalf
        colors[R.styleable.Keyboard_Key_keyPressedTextColor] = onPrimary
        colors[R.styleable.Keyboard_Key_keyTextShadowColor] = 0
        colors[R.styleable.Keyboard_Key_actionKeyTextColor] = enterKeyForeground
        colors[R.styleable.Keyboard_Key_functionalTextColor] = onKeyColor
        colors[R.styleable.Keyboard_Key_keyHintLetterColor] = onKeyColorHalf
        colors[R.styleable.Keyboard_Key_keyHintLabelColor] = onKeyColorHalf
        colors[R.styleable.Keyboard_Key_keyShiftedLetterHintInactivatedColor] = onKeyColorHalf
        colors[R.styleable.Keyboard_Key_keyShiftedLetterHintActivatedColor] = onKeyColorHalf
        colors[R.styleable.MainKeyboardView_languageOnSpacebarTextColor] = onKeyColorHalf
        colors[R.styleable.MainKeyboardView_gestureTrailColor] = primary
        colors[R.styleable.MainKeyboardView_slidingKeyInputPreviewColor] = primary

        addIcon(KeyboardIconsSet.NAME_SHIFT_KEY, R.drawable.shift, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_SHIFT_KEY_SHIFTED, R.drawable.shiftshifted, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_DELETE_KEY, R.drawable.delete, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_SETTINGS_KEY, R.drawable.settings, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_SPACE_KEY, null, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_SPACE_KEY_FOR_NUMBER_LAYOUT, R.drawable.space, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_ENTER_KEY, R.drawable.sym_keyboard_return_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_GO_KEY, R.drawable.sym_keyboard_go_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_SEARCH_KEY, R.drawable.sym_keyboard_search_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_SEND_KEY, R.drawable.sym_keyboard_send_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_NEXT_KEY, R.drawable.sym_keyboard_next_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_DONE_KEY, R.drawable.sym_keyboard_done_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_PREVIOUS_KEY, R.drawable.sym_keyboard_previous_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_TAB_KEY, R.drawable.sym_keyboard_tab_holo_dark, onKeyColor) // TODO: Correct tint
        addIcon(KeyboardIconsSet.NAME_ZWNJ_KEY, R.drawable.sym_keyboard_zwnj_lxx_dark, onKeyColor)
        addIcon(KeyboardIconsSet.NAME_ZWJ_KEY, R.drawable.sym_keyboard_zwj_lxx_dark, onPrimary)
        addIcon(KeyboardIconsSet.NAME_NUMPAD, R.drawable.numpad, onKeyColor)

        addIcon(KeyboardIconsSet.NAME_EMOJI_ACTION_KEY, R.drawable.smile, onPrimary)
        addIcon(KeyboardIconsSet.NAME_EMOJI_NORMAL_KEY, R.drawable.smile, onKeyColor)

        // Add by name (action_emoji)
        AllActionsMap.forEach { (i, it) ->
            addIcon("action_${i}", it.icon, onKeyColor)
        }

        // Add by id (action_0)
        AllActions.forEachIndexed { i, it ->
            addIcon("action_${i}", it.icon, onKeyColor)
        }

        if(!showKeyHints) {
            colors[R.styleable.Keyboard_Key_keyHintLetterColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLabelColor] = transparent
        }

        if(expertMode) {
            colors[R.styleable.Keyboard_Key_keyTextColor] = transparent
            colors[R.styleable.Keyboard_Key_keyTextInactivatedColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLetterColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLabelColor] = transparent


            // Note: We don't fully hide some things, but fade them away as they may be important landmarks
            colors[R.styleable.Keyboard_Key_functionalTextColor] = onKeyColorThird
        }

        keyboardBackground = coloredRectangle(0x00000000)

        val keyCornerRadius = 9.dp

        val spaceCornerRadius = if(keyBorders) {
            keyCornerRadius
        } else {
            48.dp
        }

        keyStyles = mapOf(
            KeyVisualStyle.Action to if(expertMode) {
                VisualStyleDescriptor(
                    backgroundDrawable = coloredRoundedRectangle(colorScheme.outline.copy(alpha = 0.1f).toArgb(), dp(128.dp)),
                    foregroundColor    = colorScheme.onSurface.copy(alpha = 0.6f).toArgb(),

                    backgroundDrawablePressed = coloredRoundedRectangle(colorScheme.outline.copy(alpha = 0.6f).toArgb(), dp(128.dp)),
                    foregroundColorPressed    = colorScheme.onSurface.toArgb()
                )
            } else {
                VisualStyleDescriptor(
                    backgroundDrawable = coloredRoundedRectangle(colorScheme.primary.toArgb(), dp(128.dp)),
                    foregroundColor    = colorScheme.onPrimary.toArgb(),

                    backgroundDrawablePressed = coloredRoundedRectangle(colorScheme.secondaryContainer.toArgb(), dp(128.dp)),
                    foregroundColorPressed    = colorScheme.onSecondaryContainer.toArgb()
                )
            },

            KeyVisualStyle.Normal to if(keyBorders) {
                makeVisualStyle(
                    keyColor,
                    if(expertMode) transparent else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius,
                )
            } else {
                makeVisualStyle(
                    transparent,
                    if(expertMode) transparent else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius
                )
            },

            KeyVisualStyle.MoreKey to VisualStyleDescriptor(
                backgroundDrawable = coloredRoundedRectangle(colorScheme.keyboardPress.toArgb(), dp(keyCornerRadius)),
                foregroundColor = colorScheme.onKeyboardContainer.toArgb(),

                backgroundDrawablePressed = coloredRoundedRectangle(primary, dp(keyCornerRadius)),
                foregroundColorPressed = onPrimary
            ),

            KeyVisualStyle.Functional to if(keyBorders) {
                makeVisualStyle(
                    functionalKeyColor,
                    if(expertMode) Color(onKeyColor).copy(alpha = 0.2f).toArgb() else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius
                )
            } else {
                makeVisualStyle(
                    transparent,
                    if(expertMode) Color(onKeyColor).copy(alpha = 0.2f).toArgb() else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius
                )
            },

            KeyVisualStyle.StickyOff to if(keyBorders) {
                makeVisualStyle(
                    keyColor,
                    if(expertMode) Color(onKeyColor).copy(alpha = 0.2f).toArgb() else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius
                )
            } else {
                makeVisualStyle(
                    transparent,
                    if(expertMode) Color(onKeyColor).copy(alpha = 0.2f).toArgb() else onKeyColor,
                    highlight, highlightForeground,
                    keyCornerRadius
                )
            },

            KeyVisualStyle.NoBackground to makeVisualStyle(transparent, onBackground, highlight, highlightForeground, keyCornerRadius),

            KeyVisualStyle.StickyOn to makeVisualStyle(
                colorScheme.secondary.toArgb(),
                colorScheme.onSecondary.toArgb(),
                highlight, highlightForeground,
                keyCornerRadius
            ),

            KeyVisualStyle.Spacebar to when {
                keyBorders -> makeVisualStyle(keyColor, onKeyColor, highlight, highlightForeground, spaceCornerRadius)
                expertMode -> makeVisualStyle(
                    colorScheme.outline.copy(alpha = 0.1f).toArgb(),
                    onKeyColor,
                    highlight, highlightForeground,
                    spaceCornerRadius
                )
                else -> makeVisualStyle(
                    highlight,
                    onKeyColor,
                    background, onBackground,
                    spaceCornerRadius
                )
            }
        )

        keyBackground = keyStyles[KeyVisualStyle.Normal]!!.backgroundDrawable!!

        keyFeedback = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(colorScheme.keyboardPress.toArgb(), colorScheme.keyboardPress.toArgb()),
        ).apply {
            cornerRadius = dp(keyCornerRadius)
        }

        colors[R.styleable.Keyboard_Key_keyPreviewTextColor] = colorScheme.onKeyboardContainer.toArgb()

        moreKeysTextColor = colorScheme.onKeyboardContainer.toArgb()
        moreKeysKeyboardBackground = coloredRoundedRectangle(colorScheme.keyboardPress.toArgb(), dp(keyCornerRadius))

        assert(icons.keys == KeyboardIconsSet.validIcons) {
            "Icons differ. Missing: ${KeyboardIconsSet.validIcons - icons.keys}, extraneous: ${icons.keys - KeyboardIconsSet.validIcons}"
        }
    }
}
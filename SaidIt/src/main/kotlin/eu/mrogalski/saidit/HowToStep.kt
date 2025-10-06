package eu.mrogalski.saidit

import androidx.annotation.StringRes
import com.siya.epistemophile.R

internal enum class HowToStep(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    OVERVIEW(R.string.how_to_title_1, R.string.how_to_desc_1),
    SAVE_CLIP(R.string.how_to_title_2, R.string.how_to_desc_2),
    MANAGE_RECORDINGS(R.string.how_to_title_3, R.string.how_to_desc_3);

    companion object {
        fun fromIndex(index: Int): HowToStep {
            require(index in entries.indices) {
                "Step index $index is out of bounds"
            }
            return entries[index]
        }
    }
}

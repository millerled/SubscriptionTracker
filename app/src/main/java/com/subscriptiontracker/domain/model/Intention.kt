package com.subscriptiontracker.domain.model

enum class Intention(val displayName: String) {
    CONSIDERING("考虑续订"),
    QUITTING("预计退订"),
    UNDECIDED("暂不确定");

    companion object {
        fun fromString(value: String?): Intention =
            entries.firstOrNull { it.name == value } ?: UNDECIDED
    }
}

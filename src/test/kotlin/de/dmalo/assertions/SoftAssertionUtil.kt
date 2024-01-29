package de.dmalo.assertions

import org.assertj.core.api.SoftAssertions

inline fun softAssert(assertBlock: SoftAssertions.() -> Unit) =
    SoftAssertions()
        .apply(assertBlock)
        .assertAll()

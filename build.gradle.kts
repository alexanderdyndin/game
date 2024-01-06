@Suppress("DSL_SCOPE_VIOLATION")

plugins {
	alias(libs.plugins.korge) version "4.0.0-rc"
}

korge {
	id = "com.example.example"
	targetJvm()
    targetJs() // comment this to not build for javascript
    targetDesktop()
    targetIos()
    targetAndroidIndirect()
}

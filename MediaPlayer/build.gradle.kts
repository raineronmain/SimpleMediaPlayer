//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.23"
	application
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.sqr"

version = "0.01"

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
	maven { url = uri("https://maven.scijava.org/content/groups/public/") }
	maven { url = uri("https://maven.scijava.org/content/repositories/public/")}
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("com.guardsquare:proguard-gradle:7.7.0") {
			exclude("com.android.tools.build")
		}
	}
}

dependencies {
	// https://mvnrepository.com/artifact/org.openjfx/javafx-media
	// implementation("org.openjfx:javafx-media:24")
	// https://mvnrepository.com/artifact/no.tornado/tornadofx
	implementation("no.tornado:tornadofx:1.7.20")
	// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.jar {

	manifest.attributes["Main-Class"] = "me.example.MainApp"

	val dependencies = configurations
		.runtimeClasspath
		.get()
		.map(::zipTree) // OR .map { zipTree(it) }
	from(dependencies)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {

	// here is where you configure your Proguard stuff, you can include libraries directly
	// through here, or in the configuration file, I usually just use the configuration file
	// to do everything (it can be any name and extension you want, just using .pro here cause
	// that's what Android uses)

	// using a file
	//configuration("proguard-rules.pro")

	injars("build/libs/MediaPlayer-0.01.jar")
	outjars("build/libs/MediaPlayer.jar")

	libraryjars("/usr/lib/jvm/bellsoft-java11-full-amd64/jmods/")

	keepattributes("Signature")
	keep ("class me.example.MainApp")

	keepclassmembers("class * {	void on*(...);}")

	dontwarn("tornadofx.**")
	dontwarn("kotlinx.**")
}

application {
	mainClass.set("me.example.MainApp")
}

kotlin {
    jvmToolchain(21)
}

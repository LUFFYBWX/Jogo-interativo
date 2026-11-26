plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace="com.livecraft"; compileSdk=35
 defaultConfig { applicationId="com.luffybw.animepushbattle"; minSdk=26; targetSdk=35; versionCode=3; versionName="0.2.0" }
 sourceSets["main"].java.srcDirs("src/main/java","../android")
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
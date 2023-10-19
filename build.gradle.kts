buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.android.tools.build:gradle:7.0.2")
    }
}
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false

}
plugins {
    id("application")
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.7"
}

application {
    mainClass.set("eu.mihosoft.vrl.v3d.Main")
    applicationDefaultJvmArgs = listOf("-Xss515m")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

javafx {
    modules = listOf("javafx.graphics", "javafx.fxml")
}

repositories {
    mavenCentral()
    jcenter()
    
    mavenLocal()
}

dependencies {
    
    testCompile(group= "junit", name= "junit", version= "4.+")

   // compile group: "eu.mihosoft.ext.org.fxyz", name: "extfxyz", version: "0.4"
    //compile group: "eu.mihosoft.ext.org.fxyz", name: "extfxyz", version: "0.4", classifier: "sources"
    compile(group= "eu.mihosoft.vvecmath", name= "vvecmath", version= "0.3.8")
    compile(group= "eu.mihosoft.vvecmath", name= "vvecmath", version= "0.3.8", classifier= "sources")
    compile("org.slf4j:slf4j-simple:1.6.1")
}


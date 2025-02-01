import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.17"
}

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.google.protobuf:protobuf-java:3.25.6")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.protobuf:protobuf-java:3.25.6")
    compileOnly("com.sourcegraph", "semanticdb-javac", "0.8.23")
}


afterEvaluate {
    tasks.processResources {
        dependsOn(tasks.getByName("generateProto"))
    }

    tasks.compileKotlin {
        dependsOn(tasks.getByName("generateProto"))
    }

    tasks.withType<JavaCompile> {
        val sourceroot = rootDir.path
        val targetroot = this.project.buildDir.resolve( "semanticdb-targetroot")
        options.compilerArgs = options.compilerArgs + listOf(
            "-Xplugin:semanticdb -sourceroot:$sourceroot -targetroot:$targetroot"
        )
    }
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.6"
    }

    generatedFilesBaseDir = kotlin.sourceSets.main.get().kotlin.srcDirs.first().path.split(":")[0].removeSuffix("main/kotlin")

    plugins {
        kotlin { }
    }

}
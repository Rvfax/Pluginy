repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.reposilite.com/maven-central")
}

dependencies {
    compileOnly(project(":plugin-core:nms:api"))

    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
}
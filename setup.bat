@echo off
cd libs

echo Installing dependencies...
mvn install:install-file -Dfile=Towny.jar -DgroupId=com.palmergames -DartifactId=bukkit.towny -Dversion=0.91.1.0 -Dpackaging=jar
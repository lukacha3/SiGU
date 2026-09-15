#!/bin/bash
set -e

echo "Starting rename script..."

# 1. Replace com.is1.proyecto -> com.sigu (files only, excluding context and git/target/node_modules)
find . -type f -not -path "*/\.*" -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/context/*" -not -name "rename_script.sh" -exec grep -l "com.is1.proyecto" {} \; | xargs sed -i 's/com\.is1\.proyecto/com.sigu/g'
echo "Replaced com.is1.proyecto -> com.sigu"

# 2. Replace com/is1/proyecto -> com/sigu
find . -type f -not -path "*/\.*" -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/context/*" -not -name "rename_script.sh" -exec grep -l "com/is1/proyecto" {} \; | xargs sed -i 's/com\/is1\/proyecto/com\/sigu/g'
echo "Replaced com/is1/proyecto -> com/sigu"

# 3. Replace proyecto_is_ii -> sigu
find . -type f -not -path "*/\.*" -not -path "*/target/*" -not -path "*/node_modules/*" -not -path "*/context/*" -not -name "rename_script.sh" -exec grep -l "proyecto_is_ii" {} \; | xargs sed -i 's/proyecto_is_ii/sigu/g'
echo "Replaced proyecto_is_ii -> sigu"

# 4. Replace proye-is -> sigu in pom.xml and dependency-reduced-pom.xml
sed -i 's/proye-is/sigu/g' pom.xml
sed -i 's/proye-is/sigu/g' dependency-reduced-pom.xml
echo "Replaced proye-is -> sigu in poms"

# 5. Delete dummy_test
rm -f dummy_test
echo "Deleted dummy_test"

# 6. Physical folder moves
mkdir -p src/main/java/com/sigu
mkdir -p src/test/java/com/sigu

if [ -d "src/main/java/com/is1/proyecto" ]; then
    cp -r src/main/java/com/is1/proyecto/* src/main/java/com/sigu/
    rm -rf src/main/java/com/is1
fi

if [ -d "src/test/java/com/is1/proyecto" ]; then
    cp -r src/test/java/com/is1/proyecto/* src/test/java/com/sigu/
    rm -rf src/test/java/com/is1
fi

echo "Done moving directories."

find ./src -name *.java > sources_list.txt
javac -classpath "${CLASSPATH}" @sources_list.txt
rm sources_list.txt
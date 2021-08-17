rm -rf *out*
/usr/local/jdk/bin/javac OSFileIO.java
strace -ff -o out /usr/local/jdk/bin/java OSFileIO $1
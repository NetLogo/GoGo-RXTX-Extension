ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

JAVAC=$(JAVA_HOME)/bin/javac
SRCS=$(wildcard src/*.java)

gogo.jar: $(SRCS) manifest.txt Makefile RXTXcomm.jar
	mkdir -p classes
	$(JAVAC) -g -deprecation -Xlint:all -Xlint:-serial -Xlint:-path -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogoLite.jar:RXTXcomm.jar -d classes $(SRCS)
	jar cmf manifest.txt gogo.jar -C classes .

gogo.zip: gogo.jar
	rm -rf gogo
	mkdir gogo
	cp -rp gogo.jar README.md Makefile src manifest.txt fix-permissions.command gogo
	zip -rv gogo.zip gogo
	rm -rf gogo

RXTXcomm.jar:
	mkdir -p lib/Mac\ OS\ X lib/Windows lib/Linux-x86 lib/Linux-amd64
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/RXTXcomm.jar' -o RXTXcomm.jar
	curl -s 'http://blog.iharder.net/wp-content/uploads/2009/08/librxtxSerial.jnilib' -o lib/Mac\ OS\ X/librxtxSerial.jnilib
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Windows/i368-mingw32/rxtxSerial.dll' -o lib/Windows/rxtxSerial.dll
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Windows/i368-mingw32/rxtxParallel.dll' -o lib/Windows/rxtxParallel.dll
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/i686-unknown-linux-gnu/librxtxParallel.so' -o lib/Linux-x86/librxtxParallel.so
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/i686-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-x86/librxtxSerial.so
	curl -s 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/x86_64-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-amd64/librxtxSerial.so

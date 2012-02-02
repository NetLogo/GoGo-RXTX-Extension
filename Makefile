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
	cp -rp gogo.jar README.md Makefile src manifest.txt gogo
	zip -rv gogo.zip gogo
	rm -rf gogo

RXTXcomm.jar:
	mkdir -p lib/Mac\ OS\ X lib/Windows lib/Linux-x86 lib/Linux-amd64
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/RXTXcomm.jar' -o RXTXcomm.jar
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/mac-10.5/librxtxSerial.jnilib' -o lib/Mac\ OS\ X/librxtxSerial.jnilib
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/win32/rxtxSerial.dll' -o lib/Windows/rxtxSerial.dll
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/i686-pc-linux-gnu/librxtxParallel.so' -o lib/Linux-x86/librxtxParallel.so
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/i686-pc-linux-gnu/librxtxSerial.so' -o lib/Linux-x86/librxtxSerial.so
	curl -s 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/x86_64-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-amd64/librxtxSerial.so
